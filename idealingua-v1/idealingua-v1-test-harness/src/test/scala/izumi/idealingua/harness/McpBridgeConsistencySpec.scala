package izumi.idealingua.harness

import io.circe.parser.parse
import izumi.idealingua.model.il.ast.typed.DefMethod
import izumi.idealingua.translator.toscala.domain.OutputWrapPolicy
import izumi.idealingua.typer.ir.TypeDef
import org.scalatest.funsuite.AnyFunSuite

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import scala.collection.mutable

/** Consistency property — for every `(service, method)` pair in the corpus, the
  * per-tool wrap flag baked into the generated `mcp/<Svc>.mcp.json` must equal
  * `OutputWrapPolicy.isWrapped(method.signature.output)`.
  *
  * The generated `<Svc>Mcp.scala` is now just an `McpServiceResource` pointer;
  * the wrap decision lives in the `.mcp.json` envelope, where the runtime reads
  * it: `McpServiceLoader` derives `McpToolMeta.wrap` from each tool's
  * `outputSchema."x-idealingua-wrapped"` (absent ⇒ false). This spec reads the
  * SAME field, so it guards the end-to-end path the runtime depends on
  * (renderer → `.mcp.json` → loader) against `OutputWrapPolicy`.
  *
  * Why it is non-tautological: the emit side encodes the wrap decision as a
  * schema annotation while building the envelope; this spec recompiles the
  * corpus and re-derives the decision straight from the IR via
  * `OutputWrapPolicy.isWrapped`, then asserts the two agree per method. Drift on
  * either side (a renderer change, a policy change) is caught.
  *
  * Hand-written services in `idealingua-v1-test-defs` (e.g. `GreeterService`)
  * are not covered: they have no IDL definition for `OutputWrapPolicy` to apply
  * to, and the harness emits no bridge envelope for them.
  */
final class McpBridgeConsistencySpec extends AnyFunSuite {

  test("bridge wrap flags in generated .mcp.json match OutputWrapPolicy.isWrapped for every method") {
    val repoRoot   = HarnessCorpus.repoRootForTests()
    val corpusRoot = HarnessCorpus.corpusRoot(repoRoot)
    val loaded     = HarnessCorpus.loadCorpus(corpusRoot)

    val mcpResourceRoot = HarnessCorpus.harnessGenRoot(repoRoot).resolve("scala-mcp-resources").resolve("mcp")
    assert(Files.isDirectory(mcpResourceRoot), s"generated MCP-bridge resources missing: $mcpResourceRoot")

    val envelopes: Seq[Path] = {
      val buf = mutable.ArrayBuffer.empty[Path]
      val it  = Files.walk(mcpResourceRoot).iterator()
      try while (it.hasNext) {
        val p = it.next()
        if (Files.isRegularFile(p) && p.getFileName.toString.endsWith(".mcp.json")) buf += p
      } finally ()
      buf.toSeq.sortBy(_.toString)
    }
    assert(envelopes.nonEmpty, s"no <Service>.mcp.json envelopes under $mcpResourceRoot")

    // Parsed: serviceName -> (methodName -> wrap), read from the generated
    // envelopes. The wrap flag is read exactly as `McpServiceLoader` reads it —
    // `tools[].outputSchema."x-idealingua-wrapped"`, absent ⇒ false — and the
    // `(service, method)` key is recovered from the fully-qualified tool name
    // `<pkg>.<Svc>.<method>` the renderer emits.
    val parsedFlags: Map[String, Map[String, Boolean]] = {
      val perService = mutable.LinkedHashMap.empty[String, mutable.LinkedHashMap[String, Boolean]]
      envelopes.foreach { p =>
        val text  = new String(Files.readAllBytes(p), StandardCharsets.UTF_8)
        val json  = parse(text).fold(e => fail(s"failed to parse $p: ${e.message}"), identity)
        val tools = json.hcursor.downField("tools").focus.flatMap(_.asArray).getOrElse(Vector.empty)
        assert(tools.nonEmpty, s"$p: envelope has no tools[]")
        tools.foreach { t =>
          val c        = t.hcursor
          val toolName = c.get[String]("name").toOption.getOrElse(fail(s"$p: tool with no name"))
          val segs     = toolName.split('.')
          assert(segs.length >= 2, s"$p: tool name not fully-qualified: $toolName")
          val svc    = segs(segs.length - 2)
          val method = segs(segs.length - 1)
          val wrap   = c.downField("outputSchema").get[Boolean]("x-idealingua-wrapped").toOption.getOrElse(false)
          val m      = perService.getOrElseUpdate(svc, mutable.LinkedHashMap.empty)
          assert(!m.contains(method), s"$p: duplicate tool for $svc.$method")
          m(method) = wrap
        }
      }
      perService.iterator.map { case (k, v) => k -> v.toMap }.toMap
    }

    // Computed: walk every compiled service in the corpus, apply
    // `OutputWrapPolicy.isWrapped` per method.
    val computedFlags: Map[String, Map[String, Boolean]] = {
      val perService = mutable.LinkedHashMap.empty[String, Map[String, Boolean]]
      loaded.foreach { ld =>
        ld.domain.userTypes.values.foreach {
          case s: TypeDef.Service =>
            val rpcs = s.methods.collect { case rpc: DefMethod.RPCMethod => rpc }
            val map  = rpcs.map(m => m.name -> OutputWrapPolicy.isWrapped(m.signature.output)).toMap
            // The corpus has no duplicate service names across domains, but be
            // defensive — last write would silently mask drift.
            assert(!perService.contains(s.id.name), s"duplicate service name across domains: ${s.id.name}")
            perService(s.id.name) = map
          case _ => ()
        }
      }
      perService.toMap
    }

    // Cross-check sets first so a missing-service error is clearer than the
    // per-method diff below.
    val parsedSvcs   = parsedFlags.keySet
    val computedSvcs = computedFlags.keySet
    assert(
      parsedSvcs.subsetOf(computedSvcs),
      s"generated envelopes reference services not in the compiled corpus: ${parsedSvcs &~ computedSvcs}",
    )
    assert(
      computedSvcs.subsetOf(parsedSvcs),
      s"corpus has services with no generated bridge envelope: ${computedSvcs &~ parsedSvcs}",
    )

    // Pair-wise compare. Collect ALL divergences then report — surfacing the
    // first failure only hides systematic drift.
    val divergences = mutable.ArrayBuffer.empty[String]
    var pairCount   = 0
    parsedFlags.foreach { case (svc, parsedMethods) =>
      val computedMethods = computedFlags(svc)
      (parsedMethods.keySet ++ computedMethods.keySet).foreach { m =>
        pairCount += 1
        (parsedMethods.get(m), computedMethods.get(m)) match {
          case (Some(p), Some(c)) if p == c => ()
          case (Some(p), Some(c)) =>
            divergences += s"$svc.$m: generated wrap=$p, computed wrap=$c"
          case (Some(_), None) =>
            divergences += s"$svc.$m: present in generated envelope, missing from corpus method list"
          case (None, Some(_)) =>
            divergences += s"$svc.$m: present in corpus, missing from generated envelope"
          case (None, None) => ()
        }
      }
    }

    assert(divergences.isEmpty, s"wrap-flag drift:\n${divergences.mkString("\n")}")
    // Loud success signal so a future zero-method corpus regression is visible.
    info(s"validated $pairCount (service, method) pairs across ${parsedFlags.size} services")
  }
}
