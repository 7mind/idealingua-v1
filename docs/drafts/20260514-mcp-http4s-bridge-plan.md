# MCP / HTTP4s Bridge Codegen Plan (idealingua-v1)

**Date:** 2026-05-14
**Branch:** wip/necromancy
**Author:** planning subagent
**Status:** locked — all user-decision items resolved 2026-05-14

## 1. Goal & non-goals

### Goal
Emit per-service Scala source files that expose the existing IRT server multiplexor (already produced by the `:scala` translator at M5) as **MCP 2025-06-18 HTTP endpoints**, so an LLM client can call generated services via `POST /mcp/tools/call` and discover them via `GET /mcp/tools/list`. The bridge code:
- reuses generated codecs and the existing `IRTServerMultiplexor.invokeMethod(IRTMethodId)(ctx, Json) : F[Throwable, Json]` dispatch primitive — no new codec emission;
- honours the M5.5 `x-idealingua-wrapped` convention by wrapping non-object responses in `{"result": <bare>}` before placing into `structuredContent` + `content[0].text`;
- handles all five `Output` variants and buzzers;
- emits MCP-spec-compliant error envelopes (`isError: true`), never HTTP 5xx for in-band failures.

### Non-goals
- No new MCP wire model — we are an HTTP transport adapter, not a JSON-RPC stdio server.
- No re-emission of the `tools/list` payload at runtime: the M3+M4 `*.mcp.json` artefact is embedded verbatim.
- No SSE / streaming MCP — out of scope (aligned with streams-skip).
- No auth / rate-limiting / CORS — the bridge sits behind the user's existing http4s middleware.
- No new effect typeclass cell — ride on the existing `F[+_, +_]: IO2` bifunctor signature already shipped by `idealingua-v1-runtime-rpc-http4s`.
- No client-side MCP code.

## 2. Pre-locked decisions

| # | Decision | Rationale |
|---|----------|-----------|
| **D1** | Extend the existing `:scala` Scala translator (NOT a new role); gate via manifest flag `emitMcpBridge=true` (default `false`) | One compiler invocation, same compiler that knows the schema, no second IR walk. Schema-vs-reality consistency by construction. |
| **D2** | http4s 0.23.33 + http4s-circe 0.23.33 (already pinned in `project/Versions.scala`) | Existing `idealingua-v1-runtime-rpc-http4s` runs on this; no new cross-build cell. |
| **D3** | Effect signature: `F[+_, +_]: IO2: Error2` — match the existing IRT runtime, NOT `cats.effect.Async` | The runtime currently emits `IRTServerMultiplexor[F[+_,+_], C]`. Forcing `Async` would duplicate the dispatch surface. Convert at the http4s edge using the same `dsl.Http4sDsl[F[Throwable, _]]` pattern `HttpServer.scala` already uses. |
| **D4** | Per-service file `<ServiceName>Mcp.scala` co-located with the service trait under the same package | Tight coupling, no awkward import paths, mirrors the existing pattern for `<Service>Wrapped.scala` / `<Service>Codecs.scala`. |
| **D5** | Dispatch via `IRTServerMultiplexor.invokeMethod(methodId)(ctx, parsedBody)` rather than re-decoding through generated case classes | The mux already performs Circe decode → handler call → encode. Bridge becomes thin envelope-translation. No second codec layer. |
| **D6** | Wrap-on-response: per-method static `wrap: Boolean` flag — `true` iff the method's `outputSchema` (computed at codegen time by `SchemaMethodOutput.wrapIfNonObject`) carries `x-idealingua-wrapped:true`. The bridge emits `Json.obj("result" -> rawResponse)` when `wrap`. | Compile-time decision; zero runtime introspection. Same predicate applied in both schema renderer and bridge renderer — guaranteed consistency. Factor into shared `OutputWrapPolicy`. |
| **D7** | Errors: MCP `CallToolResult { isError: true, content: [{type:"text", text:"<msg>"}], structuredContent: {error:{code,message}} }`. HTTP 4xx/5xx reserved for transport-layer failures only. Mapping mirrors `HttpServer.handleHttpResult`. | MCP spec semantics: tool failures are in-band; HTTP failures are transport-layer. |
| **D8** | **LOCKED (b):** read from classpath resource at `mcp/<ServiceName>.mcp.json`. `:scala` translator emits both the routes file AND the resource JSON when `emitMcpBridge=true`. Bridge reads via `getClass.getClassLoader.getResourceAsStream` at request time. | Handles arbitrary corpus sizes without `.class` constant-pool concerns; resource lives at a predictable location. Wrap-policy drift mitigated by emitting the resource from the same `:scala` invocation that emits the bridge code; the resource is documented as compile-time companion, not for out-of-band edit. |
| **D9** | Bridge entrypoint: `object <ServiceName>McpRoutes { def routes[F[+_,+_]: IO2: Error2, C](mux: IRTServerMultiplexor[F, C], extractCtx: Request[F[Throwable, _]] => F[Throwable, C], dsl: Http4sDsl[F[Throwable, _]]): HttpRoutes[F[Throwable, _]] }`. | Composable with existing `HttpServer`; users mount at `/mcp` or anywhere. `extractCtx` mirrors `HttpContextExtractor.extract`. |
| **D10** | Tool name dispatch: pattern-match on the string `<pkg>.<ServiceName>.<method>` (D22 from schema plan); per-service routes handle only their own service's names; unknown name in own service prefix → `isError:true { code: -32601, message: "Method not found" }`. Names outside own prefix → fall through. | Single-route-per-service avoids one giant generated dispatch table. |
| **D11** | **LOCKED (b):** skip buzzers entirely in the bridge codegen. No `<BuzzerName>Mcp.scala` emission; no buzzer tool entries in the per-service `mcp.json` resource. Schema-side `SchemaBuzzerRenderer` (M3+M4) continues to emit the standalone `*.mcp.json` for tooling that wants buzzer discoverability — bridge just doesn't serve it. | Cleaner RPC semantics; eliminates fire-and-forget race + auth/rate-limiting concerns; reduces codegen scope. |
| **D12** | Streams: skip silently in bridge codegen | Consistent with M3+M4 streams-skip. |
| **D13** | Manifest flag wire-up: `BuildManifest.scala`'s `LanguageManifest` extension fields gain `emitMcpBridge: Boolean = false`. | Backwards-compatible; opt-in. |
| **D14** | Bridge code lives in `idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/toscala/domain/DomainServiceMcpRenderer.scala`; wired into `DomainServiceRenderer.renderService/renderBuzzer` as an additional artefact, gated by manifest flag. | Schema-derived wrap policy reachable in same renderer pass. |

## 3. Generated-code shape

### 3.1 Singular-output method (wraps)

```scala
package idltest.services

import io.circe.{Json, parser}
import izumi.functional.bio.{Error2, IO2, F}
import izumi.idealingua.runtime.rpc.*
import org.http4s.{HttpRoutes, Request, Response, Status}
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl

object GreetServiceMcp {
  // D8(b): tools/list loaded from classpath resource at first access.
  private lazy val toolsListJson: Json = {
    val stream = getClass.getClassLoader.getResourceAsStream("mcp/GreetService.mcp.json")
    require(stream != null, "Missing classpath resource: mcp/GreetService.mcp.json")
    try parser.parse(scala.io.Source.fromInputStream(stream, "UTF-8").mkString).toTry.get
    finally stream.close()
  }

  private val toolName_simple = "idltest.services.GreetService.simple"
  private val methodId_simple =
    IRTMethodId(IRTServiceId("GreetService"), IRTMethodName("simple"))

  def routes[F[+_, +_]: IO2: Error2, C](
    mux: IRTServerMultiplexor[F, C],
    extractCtx: Request[F[Throwable, _]] => F[Throwable, C],
    dsl: Http4sDsl[F[Throwable, _]],
  ): HttpRoutes[F[Throwable, _]] = {
    import dsl.*
    HttpRoutes.of[F[Throwable, _]] {
      case GET -> Root / "mcp" / "tools" / "list" =>
        Ok(toolsListJson)

      case req @ POST -> Root / "mcp" / "tools" / "call" =>
        req.as[Json].flatMap { body =>
          val nameOpt = body.hcursor.downField("name").as[String].toOption
          val argsJson = body.hcursor.downField("arguments").as[Json].getOrElse(Json.obj())
          nameOpt match {
            case Some(`toolName_simple`) =>
              call(req, argsJson, methodId_simple, wrap = true, extractCtx, mux, dsl)
            case Some(other) if other.startsWith("idltest.services.GreetService.") =>
              Ok(mcpError(-32601, s"Method not found: $other"))
            case _ =>
              Pass.pure[F[Throwable, _]]
          }
        }
    }
  }

  private def call[F[+_, +_]: IO2: Error2, C](
    req: Request[F[Throwable, _]],
    args: Json,
    methodId: IRTMethodId,
    wrap: Boolean,
    extractCtx: Request[F[Throwable, _]] => F[Throwable, C],
    mux: IRTServerMultiplexor[F, C],
    dsl: Http4sDsl[F[Throwable, _]],
  ): F[Throwable, Response[F[Throwable, _]]] = {
    import dsl.*
    (for {
      ctx <- extractCtx(req)
      raw <- mux.invokeMethod(methodId)(ctx, args)
      shaped = if (wrap) Json.obj("result" -> raw) else raw
      result = Json.obj(
        "content" -> Json.arr(Json.obj("type" -> Json.fromString("text"), "text" -> Json.fromString(shaped.noSpaces))),
        "structuredContent" -> shaped,
        "isError" -> Json.False,
      )
    } yield result).sandboxExit.flatMap {
      case Exit.Success(j) => Ok(j)
      case Exit.Error(_: IRTMissingHandlerException, _) => Ok(mcpError(-32601, "Method not found"))
      case Exit.Error(_: IRTDecodingException, _)       => Ok(mcpError(-32602, "Invalid arguments"))
      case Exit.Error(_: io.circe.Error, _)             => Ok(mcpError(-32602, "Invalid arguments"))
      case Exit.Error(_: IRTUnathorizedRequestContextException, _) =>
        F.pure(Response(status = Status.Unauthorized))
      case _ => Ok(mcpError(-32603, "Internal error"))
    }
  }

  private def mcpError(code: Int, msg: String): Json = Json.obj(
    "content"           -> Json.arr(Json.obj("type" -> Json.fromString("text"), "text" -> Json.fromString(msg))),
    "structuredContent" -> Json.obj("error" -> Json.obj("code" -> Json.fromInt(code), "message" -> Json.fromString(msg))),
    "isError"           -> Json.True,
  )
}
```

### 3.2 Wrap flag table

| Variant | wrap | structuredContent shape |
|---------|------|-------------------------|
| `Void` | `true` | `{"result": null}` |
| `Singular(DTO)` | `true` | `{"result": <dto>}` |
| `Singular(primitive/generic)` | `true` | `{"result": <prim>}` |
| `Struct(s)` | `false` | bare `{...}` |
| `Algebraic` | `true` | `{"result": {"<branch>": ...}}` |
| `Alternative` | `true` | `{"result": {"Success": ...} | {"Failure": ...}}` |
| `Buzzer` | `true` | `{"result": null}` |

## 4. Handler interface contract

User provides:
- An `IRTServerMultiplexor[F, C]` already populated with their handler (same object the existing http4s server consumes).
- A `Request => F[Throwable, C]` context extractor.
- An `Http4sDsl` instance.

No new handler trait. No new wiring beyond `<ServiceName>McpRoutes.routes(...)` + composing with existing routes.

## 5. CLI surface

No new CLI flag. Manifest override:
```yaml
language: scala
emitMcpBridge: true  # NEW; default false
```
Wires through `ScalaTranslatorOptions`.

## 6. Architecture / file layout

```
idealingua-v1-transpilers/src/main/scala/izumi/idealingua/translator/toscala/
  domain/
    DomainServiceMcpRenderer.scala       // NEW — primary emitter
    DomainServiceMcpHelpers.scala        // NEW — wrap-flag predicate (shared with toschema)
  ScalaTranslatorOptions.scala           // +1 field: emitMcpBridge
```

Hook point in `DomainServiceRenderer.renderImpl`: after emitting existing outputs, if `ctx.options.emitMcpBridge`, append `<ServiceName>Mcp.scala`. Same for `renderBuzzer`.

The `*.mcp.json` literal-embed sources from `SchemaServiceRenderer.render(svc)` output (invoked in-process) — guaranteeing the embedded tools-list and the wrap policy come from the SAME compiler pass.

## 7. Codec reuse strategy

The bridge has zero per-method codec references — it only needs:
- the constant `IRTMethodId` for each method,
- the static wrap flag,
- the tool name string.

`IRTServerMultiplexor.invokeMethod` does the codec work.

Net codegen per method: ~3 lines. Per service: ~30 LOC scaffolding.

## 8. Buzzer policy (recommended D11(a))

Buzzers expose as MCP tools whose `outputSchema` is the wrapped `{result: null}` per M5.5. Bridge dispatches into the buzzer mux (unified into `IRTWrappedService` per F16), returns `{result: null}`. Open caveat: fire-and-forget semantics mean the response races the side-effect.

## 9. Error model

| In-band (CallToolResult.isError=true, HTTP 200) | Out-of-band (HTTP non-2xx) |
|---|---|
| unknown tool name within service prefix | malformed JSON body → 400 |
| arg-decode failure | tools/list/call path mismatch → 404 |
| handler exception (sandboxed) | unhandled http4s middleware exception → 500 |
| unauth context | n/a |

## 10. Verification plan

### Layer A — golden sources
Add `idealingua-v1-test-defs/golden/scala-mcp/<package>/<ServiceName>Mcp.scala`. `verifyGoldens` extended for `emitMcpBridge=true` corpus subset.

### Layer B — round-trip MCP request smoke
Spec under `idealingua-v1-runtime-rpc-http4s/src/test/`:
1. Compile a corpus domain with `emitMcpBridge=true`.
2. Mount generated routes against existing `TestServices` mux.
3. Hit `GET /mcp/tools/list` — assert JSON equals the M3+M4 `*.mcp.json`.
4. For each `Output` variant, hit `POST /mcp/tools/call` — assert `structuredContent` validates against declared `outputSchema`.
5. Error cases — assert `isError:true`.

### Layer C — wrap-policy consistency
Property test: for every method, `bridge-wrap-flag == SchemaMethodOutput.wrapIfNonObject-applied`.

## 11. Milestone breakdown

| M | Scope | Est. |
|---|-------|------|
| **Mb1** | Scaffold `DomainServiceMcpRenderer`, manifest flag, `<Service>Mcp.scala` with tools/list + Singular match arm; Layer A golden for 1 fixture; manual test. ~150 LOC. | 1 day |
| **Mb2** | All 5 Output variants; wrap-flag derivation; per-method match arms; Layer A goldens extended. | 1 day |
| **Mb3** | Buzzers (D11); error envelope (D7) — all 4 IRT exception classes mapped. | 1 day |
| **Mb4** | Layer B integration spec; Layer C consistency assertion; regression-harness adapter. | 2 days |

Total ~5 days.

## 12. Risks

| # | Risk | Mitigation |
|---|------|------------|
| **R1** | http4s 0.23 surface drift across Scala 2.13 + 3.x | Reuse exact patterns from `HttpServer.scala`. |
| **R2** | Wrap-flag derivation duplicated | Shared `OutputWrapPolicy.isWrapped(Output): Boolean` helper. |
| **R3** | `tools/list` literal grows large | Acceptable; D8(b) classpath fallback as escape. |
| **R4** | Buzzer dispatch requires merged mux at user wiring time | Document. Stretch: `<Domain>McpAggregator.scala`. |
| **R5** | Multiple services' MCP routes mounted naively → duplicate `tools/list` responses | Document one-service-per-mount. |
| **R6** | Embedded `*.mcp.json` goes stale if `:schema` runs without `:scala` | Schema + bridge both come from `:scala` when `emitMcpBridge=true`. |
| **R7** | LLM args omit optional fields → `IRTDecodingException` | In-band error with `inputSchema.required` in message. |
| **R8** | `extractCtx` exceptions leak outside `sandboxExit` | Wrap in `F.syncThrowable(...).flatten`. |
