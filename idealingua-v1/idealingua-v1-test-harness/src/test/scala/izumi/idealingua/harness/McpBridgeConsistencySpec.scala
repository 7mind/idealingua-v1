package izumi.idealingua.harness

import izumi.idealingua.model.il.ast.typed.DefMethod
import izumi.idealingua.translator.toscala.domain.OutputWrapPolicy
import izumi.idealingua.typer.ir.TypeDef
import org.scalatest.funsuite.AnyFunSuite

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import scala.collection.mutable
import scala.meta.*

/** Mb4-B: Layer C consistency property — for every `(service, method)` pair in
  * the corpus, the `wrap = true|false` literal baked into the generated
  * `<Service>McpRoutes` `call(...)` invocation must equal
  * `OutputWrapPolicy.isWrapped(method.signature.output)`.
  *
  * Why this test is non-tautological:
  *
  * The generator (`DomainServiceMcpRenderer.renderMatchArm`) invokes
  * `OutputWrapPolicy.isWrapped` directly when emitting the wrap literal — so
  * a fresh emission is consistent by construction. The value of this spec is
  * **drift detection on the golden artefacts**: the
  * `idealingua-v1-test-defs/golden/scala-mcp/` files are committed copies of
  * the generator output, NOT regenerated on every test run. If someone
  * refactors `OutputWrapPolicy.isWrapped` (e.g. flips the `Struct` arm to
  * `true`) without re-running `regenerateGoldens`, the goldens stay stale and
  * this test flags the inconsistency.
  *
  * Mechanics:
  *   1. Parse each `<Service>Mcp.scala` golden via scala.meta. Extract every
  *      `(methodName, wrapFlag)` pair from `call(req, argsJson,
  *      methodId_<m>, wrap = <bool>, ...)` invocations.
  *   2. Compile the corpus IDL. Walk every `TypeDef.Service`, then every
  *      `DefMethod.RPCMethod`. Apply `OutputWrapPolicy.isWrapped` to the
  *      output.
  *   3. Assert: for every `(serviceName, methodName)` key, the parsed
  *      goldenflag equals the computed flag.
  *
  * Hand-written services in `idealingua-v1-test-defs/src/main/scala/...`
  * (e.g. `GreeterService`) are NOT covered — they have no IDL definition, so
  * `OutputWrapPolicy.isWrapped` has nothing to apply to. They appear only as
  * test fixtures, not as goldens.
  */
final class McpBridgeConsistencySpec extends AnyFunSuite {

  test("bridge wrap-flag literals match OutputWrapPolicy.isWrapped for every method") {
    val repoRoot   = HarnessCorpus.repoRootForTests()
    val corpusRoot = HarnessCorpus.corpusRoot(repoRoot)
    val loaded     = HarnessCorpus.loadCorpus(corpusRoot)

    val goldenRoot = repoRoot.resolve("idealingua-v1/idealingua-v1-test-defs/golden/scala-mcp")
    assert(Files.isDirectory(goldenRoot), s"golden tree missing: $goldenRoot")

    val goldenSources: Seq[Path] = {
      val buf = mutable.ArrayBuffer.empty[Path]
      val it  = Files.walk(goldenRoot).iterator()
      try while (it.hasNext) {
        val p = it.next()
        if (Files.isRegularFile(p) && p.getFileName.toString.endsWith("Mcp.scala")) buf += p
      } finally ()
      buf.toSeq.sortBy(_.toString)
    }
    assert(goldenSources.nonEmpty, s"no <Service>Mcp.scala goldens under $goldenRoot")

    // Parsed: map from serviceName -> map[methodName -> wrapFlag-from-golden].
    val parsedFlags: Map[String, Map[String, Boolean]] = goldenSources.iterator.map { p =>
      val text   = new String(Files.readAllBytes(p), StandardCharsets.UTF_8)
      val source = dialects.Scala213(text).parse[Source] match {
        case parsers.Parsed.Success(tree) => tree
        case parsers.Parsed.Error(_, msg, _) =>
          fail(s"failed to parse golden $p: $msg")
      }

      // Service name lives in `object <Name>McpRoutes` — strip the trailing
      // `McpRoutes` to recover the bare service name.
      //
      // Use a manual depth-first walk via `Tree.children` rather than the
      // scala.meta `Tree.collect`/`Tree.traverse` extension methods, which
      // pre-4.16 are 2.13-only (the `transversers` package on Scala 3 is not
      // wired through the same XtensionStringInterpolators path). Recursion
      // depth across the corpus stays well below JVM stack limits.
      def walk(t: Tree, visit: Tree => Unit): Unit = {
        visit(t)
        t.children.foreach(walk(_, visit))
      }

      var svcNameOpt: Option[String] = None
      walk(
        source,
        {
          case Defn.Object(_, name, _) if name.value.endsWith("McpRoutes") && svcNameOpt.isEmpty =>
            svcNameOpt = Some(name.value.stripSuffix("McpRoutes"))
          case _ => ()
        },
      )
      val svcName = svcNameOpt.getOrElse(fail(s"$p: no McpRoutes object found"))

      // Walk every `call(req, argsJson, methodId_<m>, wrap = <bool>, ...)`
      // invocation. The generator's structure (see
      // `DomainServiceMcpRenderer.renderMatchArm`) guarantees the literal
      // `wrap = true|false` form, so we pattern-match it directly. The
      // `methodId_<m>` prefix gates out the `call` private-method signature
      // itself.
      val pairs = mutable.ArrayBuffer.empty[(String, Boolean)]
      walk(
        source,
        {
          case Term.Apply.After_4_6_0(Term.Name("call"), Term.ArgClause(args, _)) =>
            val methodNameOpt = args.collectFirst {
              case Term.Name(n) if n.startsWith("methodId_") => n.stripPrefix("methodId_")
            }
            val wrapValueOpt = args.collectFirst {
              case Term.Assign(Term.Name("wrap"), Lit.Boolean(b)) => b
            }
            (methodNameOpt, wrapValueOpt) match {
              case (Some(m), Some(w)) => pairs += ((m, w))
              case _                  => ()
            }
          case _ => ()
        },
      )
      assert(pairs.nonEmpty, s"$p: no `call(..., methodId_<m>, wrap = <bool>, ...)` invocations found")
      // No duplicate method arms per service.
      val grouped = pairs.groupBy(_._1)
      grouped.foreach { case (m, ms) =>
        assert(ms.size == 1, s"$p: method $m has ${ms.size} match arms (expected 1)")
      }
      svcName -> pairs.iterator.toMap
    }.toMap

    // Computed: walk every compiled service in the corpus, apply
    // `OutputWrapPolicy.isWrapped` per method.
    val computedFlags: Map[String, Map[String, Boolean]] = {
      val perService = mutable.LinkedHashMap.empty[String, Map[String, Boolean]]
      loaded.foreach { ld =>
        ld.domain.userTypes.values.foreach {
          case s: TypeDef.Service =>
            val rpcs = s.methods.collect { case rpc: DefMethod.RPCMethod => rpc }
            val map  = rpcs.map(m => m.name -> OutputWrapPolicy.isWrapped(m.signature.output)).toMap
            // The corpus has no duplicate service names across domains, but
            // be defensive — last write would silently mask drift.
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
      s"goldens reference services not in the compiled corpus: ${parsedSvcs &~ computedSvcs}",
    )
    assert(
      computedSvcs.subsetOf(parsedSvcs),
      s"corpus has services with no golden bridge: ${computedSvcs &~ parsedSvcs}",
    )

    // Pair-wise compare. Collect ALL divergences then report — surfacing the
    // first failure only hides systematic drift.
    val divergences = mutable.ArrayBuffer.empty[String]
    var pairCount   = 0
    parsedFlags.foreach { case (svc, parsedMethods) =>
      val computedMethods = computedFlags(svc)
      val allMethods      = parsedMethods.keySet ++ computedMethods.keySet
      allMethods.foreach { m =>
        pairCount += 1
        (parsedMethods.get(m), computedMethods.get(m)) match {
          case (Some(p), Some(c)) if p == c => ()
          case (Some(p), Some(c)) =>
            divergences += s"$svc.$m: golden wrap=$p, computed wrap=$c"
          case (Some(_), None) =>
            divergences += s"$svc.$m: present in golden, missing from corpus method list"
          case (None, Some(_)) =>
            divergences += s"$svc.$m: present in corpus, missing match arm in golden"
          case (None, None) => ()
        }
      }
    }

    assert(divergences.isEmpty, s"wrap-flag drift:\n${divergences.mkString("\n")}")
    // Loud success signal so a future zero-method corpus regression is visible.
    info(s"Mb4-B: validated $pairCount (service, method) pairs across ${parsedFlags.size} services")
  }
}
