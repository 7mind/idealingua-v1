package izumi.idealingua.harness

import izumi.fundamentals.platform.strings.IzString.*
import izumi.idealingua.model.common.TypeId.{BuzzerId, ServiceId}
import izumi.idealingua.model.common.{DomainId, Primitive}
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.il.ast.raw.defns.{RawNodeMeta, RawTopLevelDefn}
import izumi.idealingua.model.il.ast.raw.domains.{DomainMeshResolved, Import => RawImport}
import izumi.idealingua.model.il.ast.raw.models.{Inclusion => RawInclusion}
import izumi.idealingua.model.il.ast.typed.DefMethod.Output.{Singular, Void}
import izumi.idealingua.model.il.ast.typed.DefMethod.{RPCMethod, Signature}
import izumi.idealingua.model.il.ast.typed.{Buzzer => LegacyBuzzer, Service => LegacyService}
import izumi.idealingua.model.il.ast.typed.{DefMethod, DomainDefinition, DomainMetadata, Field, NodeMeta, SimpleStructure}
import izumi.idealingua.model.common.Package
import izumi.idealingua.model.loader.FSPath
import izumi.idealingua.model.publishing.manifests.{TypeScriptBuildManifest, TypeScriptProjectLayout}
import izumi.idealingua.model.typespace.{Typespace, TypespaceImpl}
import izumi.idealingua.translator.CompilerOptions
import izumi.idealingua.translator.IDLLanguage
import izumi.idealingua.translator.totypescript.TypeScriptImports
import izumi.idealingua.translator.totypescript.domain.DomainTSContext
import izumi.idealingua.translator.totypescript.extensions.TypeScriptTranslatorExtension
import izumi.idealingua.translator.totypescript.products.CogenProduct.{BuzzerProduct, ServiceProduct}
import izumi.idealingua.typer.ir.{Domain, Fingerprint, TypeDef => NewTypeDef}
import org.scalatest.funsuite.AnyFunSuite
import scodec.bits.ByteVector

/** PR-02 IMPL-7b Phase B M3: byte-parity unit test for `DomainTSServiceRenderer`.
  *
  * Two cases:
  *   - Service with one Singular-output method: emits the
  *     Models / Client / Dispatcher / Server quadruple and matches the
  *     legacy `TypeScriptTranslator.renderService` mirror byte-for-byte.
  *   - Buzzer with one Void-output event: emits the same quadruple shape
  *     (BuzzerHandlers / ServerSocketTransport / handlers wiring) and
  *     matches the legacy `renderBuzzer` mirror byte-for-byte.
  *
  * The legacy mirrors are simplified copies of
  * `TypeScriptTranslator.renderService` (`scala:961-988`) and
  * `renderBuzzer` (`scala:1084-1121`) which delegate to the same shared
  * helpers — so the spec asserts string equality on the full product
  * (client body + header + preamble).
  */
final class DomainTSServiceRendererSpec extends AnyFunSuite {

  private val domainId   = DomainId(Seq("idltest"), "ts_service_render_spec")
  private val emptyMeta  = NodeMeta.empty
  private val rawMeta    = RawNodeMeta(None, Seq.empty, InputPosition.Undefined)
  private val tsManifest = TypeScriptBuildManifest.example
  private val emptyExts: Seq[TypeScriptTranslatorExtension] = Seq.empty
  private val options    = CompilerOptions[TypeScriptTranslatorExtension, TypeScriptBuildManifest](IDLLanguage.Typescript, emptyExts, tsManifest)
  private val conv       = new izumi.idealingua.translator.totypescript.types.TypeScriptTypeConverter()

  // -------------------- Legacy mirror helpers --------------------

  private def importFromIRT(names: List[String], pkg: Package): String = {
    var importOffset = ""
    (1 to pkg.length).foreach(_ => importOffset += "../")
    if (tsManifest.layout == TypeScriptProjectLayout.YARN) {
      importOffset = tsManifest.yarn.scope + "/"
    }
    s"""import {
       |${names.map(n => s"    $n").mkString(",\n")}
       |} from '${importOffset}irt'
     """.stripMargin
  }

  private def renderRuntimeNames(s: ServiceId, holderName: String): String = {
    val pkg = s.domain.toPackage.mkString(".")
    s"""// Runtime identification methods
       |public static readonly PackageName = '$pkg';
       |public static readonly ClassName = '${s.name}';
       |public static readonly FullClassName = '$pkg.${s.name}';
       |
       |public getPackageName(): string { return $holderName.PackageName; }
       |public getClassName(): string { return $holderName.ClassName; }
       |public getFullClassName(): string { return $holderName.FullClassName; }
       """.stripMargin
  }

  private def renderRuntimeNamesBuzzer(i: BuzzerId, holderName: String): String = {
    val pkg = i.domain.toPackage.mkString(".")
    s"""// Runtime identification methods
       |public static readonly PackageName = '$pkg';
       |public static readonly ClassName = '${i.name}';
       |public static readonly FullClassName = '$pkg.${i.name}';
       |
       |public getPackageName(): string { return $holderName.PackageName; }
       |public getClassName(): string { return $holderName.ClassName; }
       |public getFullClassName(): string { return $holderName.FullClassName; }
       """.stripMargin
  }

  private def renderSerializedObject(fields: List[Field], ts: Typespace): String = {
    val serialized = fields.map(f => conv.serializeField(f, ts))
    val it         = serialized.iterator
    it.map(m => s"$m${if (it.hasNext) "," else ""}").mkString("\n")
  }

  private def renderServiceMethodOutputSignature(method: DefMethod.RPCMethod, ts: Typespace): String =
    renderServiceMethodOutputType(method.signature.output, method, ts)

  private def renderServiceMethodOutputType(output: DefMethod.Output, method: DefMethod.RPCMethod, ts: Typespace): String = output match {
    case si: Singular => conv.toNativeType(si.typeId, ts)
    case _: Void      => "void"
    case _            => throw new Exception("non-singular non-void out unsupported in spec mirror")
  }

  private def renderRPCMethodSignature(method: DefMethod, ts: Typespace, spread: Boolean = false, forClient: Boolean = true): String = method match {
    case m: DefMethod.RPCMethod =>
      if (spread) {
        val fields = m.signature.input.fields.map(f => conv.safeName(f.name) + s": ${conv.toNativeType(f.typeId, ts)}").mkString(", ")
        if (forClient)
          s"""${m.name}($fields): Promise<${renderServiceMethodOutputSignature(m, ts)}>"""
        else
          s"""${m.name}(context: C${if (m.signature.input.fields.nonEmpty) ", " else ""}$fields): Promise<${renderServiceMethodOutputSignature(m, ts)}>"""
      } else {
        s"""${m.name}(input: In${m.name.capitalize}): Promise<${renderServiceMethodOutputSignature(m, ts)}>"""
      }
  }

  private def renderServiceMethodInModel(name: String, implements: String, structure: SimpleStructure, ts: Typespace, exported: Boolean): String = {
    s"""${if (exported) "export " else ""}class $name implements $implements {
       |${structure.fields.map(f => conv.toFieldMember(f, ts)).mkString("\n").shift(4)}
       |${structure.fields.map(f => conv.toFieldMethods(f, ts)).mkString("\n").shift(4)}
       |    constructor(data: ${name}Serialized = undefined) {
       |        if (typeof data === 'undefined' || data === null) {
       |            return;
       |        }
       |
       |${structure.fields
        .map(f => s"${conv.deserializeName("this." + conv.safeName(f.name), f.typeId)} = ${conv.deserializeType("data." + f.name, f.typeId, ts)};").mkString(
          "\n"
        ).shift(8)}
       |    }
       |
       |    public serialize(): ${name}Serialized {
       |        return {
       |${renderSerializedObject(structure.fields, ts).shift(12)}
       |        };
       |    }
       |}
       |
       |${if (exported) "export " else ""}interface ${name}Serialized {
       |${structure.fields.map(f => s"${conv.toNativeTypeName(f.name, f.typeId)}: ${conv.toNativeType(f.typeId, ts, forSerialized = true)};").mkString("\n").shift(4)}
       |}
     """.stripMargin
  }

  private def renderServiceMethodOutModel(name: String, implements: String, out: DefMethod.Output, ts: Typespace): String = out match {
    case _: Singular => ""
    case _: Void     => ""
    case _           => throw new Exception("non-singular non-void out unsupported")
  }

  private def renderRPCMethodModels(method: DefMethod, ts: Typespace): String = method match {
    case m: DefMethod.RPCMethod =>
      s"""${renderServiceMethodInModel(s"In${m.name.capitalize}", "IncomingData", m.signature.input, ts, exported = false)}
         |${renderServiceMethodOutModel(s"Out${m.name.capitalize}", "OutgoingData", m.signature.output, ts)}
       """.stripMargin
  }

  private def renderRPCClientMethod(service: String, method: DefMethod, ts: Typespace): String = method match {
    case m: DefMethod.RPCMethod =>
      m.signature.output match {
        case si: Singular =>
          s"""public ${renderRPCMethodSignature(method, ts, spread = true)} {
             |    const __data = new In${m.name.capitalize}();
             |${m.signature.input.fields.map(f => s"__data.${conv.safeName(f.name)} = ${conv.safeName(f.name)};").mkString("\n").shift(4)}
             |    return new Promise((resolve, reject) => {
             |        this._transport.send(${service}Client.ClassName, '${m.name}', __data)
             |            .then((data: any) => {
             |                try {
             |                    const output = ${conv.deserializeType("data", si.typeId, ts, asAny = true)};
             |                    resolve(output);
             |                }
             |                catch(err) {
             |                    reject(err);
             |                }
             |            })
             |            .catch((err: any) => {
             |                reject(err);
             |            });
             |        });
             |}
         """.stripMargin

        case _: Void =>
          s"""public ${renderRPCMethodSignature(method, ts, spread = true)} {
             |    const __data = new In${m.name.capitalize}();
             |${m.signature.input.fields.map(f => s"__data.${conv.safeName(f.name)} = ${conv.safeName(f.name)};").mkString("\n").shift(4)}
             |    return new Promise((resolve, reject) => {
             |        this._transport.send(${service}Client.ClassName, '${m.name}', __data)
             |            .then(() => {
             |              resolve();
             |            })
             |            .catch((err: any) => {
             |                reject(err);
             |            });
             |        });
             |}
         """.stripMargin

        case _ => throw new Exception("non-singular non-void out unsupported")
      }
  }

  private def isServiceMethodReturnExistent(method: DefMethod.RPCMethod): Boolean = method.signature.output match {
    case _: Void => false
    case _       => true
  }

  private def renderServiceReturnSerialization(method: DefMethod.RPCMethod, ts: Typespace, useRawMarshaller: Boolean = false): String = {
    val useRawParam = if (useRawMarshaller) ", true" else ""
    s"const serialized = this.marshaller.Marshal<${renderServiceMethodOutputSignature(method, ts)}>(res$useRawParam);"
  }

  private def renderServiceDispatcherHandler(method: DefMethod, impl: String, ts: Typespace, useRawMarshaller: Boolean = false): String = {
    val useRawParam = if (useRawMarshaller) ", true" else ""
    method match {
      case m: DefMethod.RPCMethod =>
        val resolveCode =
          if (isServiceMethodReturnExistent(m))
            s"""${renderServiceReturnSerialization(m, ts, useRawMarshaller = useRawMarshaller).shift(20)}
               |                    resolve(serialized);""".stripMargin
          else
            s"                    resolve(this.marshaller.Marshal<Void>(Void.instance$useRawParam));"

        s"""case "${m.name}": {
           |    ${
            if (m.signature.input.fields.isEmpty) "// No input params for this method"
            else
              s"const obj = ${if (m.signature.input.fields.nonEmpty) s"new In${m.name.capitalize}(" else ""}this.marshaller.Unmarshal<${
                  if (m.signature.input.fields.nonEmpty) s"In${m.name.capitalize}Serialized" else "object"
                }>(data$useRawParam)${if (m.signature.input.fields.nonEmpty) ")" else ""};"
          }
           |    return new Promise((resolve, reject) => {
           |        try {
           |            this.$impl.${m.name}(context${if (m.signature.input.fields.isEmpty) "" else ", "}${m.signature.input.fields
            .map(f => s"obj.${conv.safeName(f.name)}").mkString(", ")})
           |                .then((res: ${renderServiceMethodOutputSignature(m, ts)}) => {
           |$resolveCode
           |                })
           |                .catch((err) => {
           |                    reject(err);
           |                });
           |        } catch (err) {
           |            reject(err);
           |        }
           |    });
           |}
         """.stripMargin
    }
  }

  private def renderServiceClient(i: LegacyService, ts: Typespace): String = {
    s"""export interface I${i.id.name}Client {
       |${i.methods.map(me => renderRPCMethodSignature(me, ts, spread = true)).mkString("\n").shift(4)}
       |}
       |
       |export class ${i.id.name}Client implements I${i.id.name}Client {
       |${renderRuntimeNames(i.id, s"${i.id.name}Client").shift(4)}
       |    protected _transport: ClientTransport;
       |
       |    constructor(transport: ClientTransport) {
       |        this._transport = transport;
       |    }
       |
       |    private send<I extends IncomingData, O extends OutgoingData>(method: string, data: I, inputType: {new(): I}, outputType: {new(data: any): O} ): Promise<O> {
       |        return new Promise((resolve, reject) => {
       |            this._transport.send(${i.id.name}Client.ClassName, method, data)
       |                .then((data: any) => {
       |                    try {
       |                        const output = new outputType(data);
       |                        resolve(output);
       |                    }
       |                    catch (err) {
       |                        reject(err);
       |                    }
       |                })
       |                .catch((err: any) => {
       |                    reject(err);
       |                });
       |            });
       |    }
       |${i.methods.map(me => renderRPCClientMethod(i.id.name, me, ts)).mkString("\n").shift(4)}
       |}
     """.stripMargin
  }

  private def renderServiceDispatcher(i: LegacyService, ts: Typespace): String = {
    s"""export interface I${i.id.name}Server<C> {
       |${i.methods.map(me => renderRPCMethodSignature(me, ts, spread = true, forClient = false)).mkString("\n").shift(4)}
       |}
       |
       |export class ${i.id.name}Dispatcher<C, D> implements ServiceDispatcher<C, D> {
       |    private static readonly methods: string[] = [
       |${i.methods.map(m => if (m.isInstanceOf[DefMethod.RPCMethod]) "        \"" + m.asInstanceOf[DefMethod.RPCMethod].name + "\"" else "").mkString(",\n")}\n    ];
       |    protected marshaller: Marshaller<D>;
       |    protected server: I${i.id.name}Server<C>;
       |
       |    constructor(marshaller: Marshaller<D>, server: I${i.id.name}Server<C>) {
       |        this.marshaller = marshaller;
       |        this.server = server;
       |    }
       |
       |    public getSupportedService(): string {
       |        return '${i.id.name}';
       |    }
       |
       |    public getSupportedMethods(): string[] {
       |        return  ${i.id.name}Dispatcher.methods;
       |    }
       |
       |    public dispatch(context: C, method: string, data: D | undefined): Promise<D> {
       |        switch (method) {
       |${i.methods.map(m => renderServiceDispatcherHandler(m, "server", ts)).mkString("\n").shift(12)}
       |            default:
       |                throw new Error(`Method $${method} is not supported by ${i.id.name}Dispatcher.`);
       |        }
       |    }
       |}
     """
  }

  private def renderServiceServerDummyMethod(member: DefMethod, ts: Typespace): String = {
    s"""public ${renderRPCMethodSignature(member, ts, spread = true, forClient = false)} {
       |    throw new Error('Not implemented.');
       |}
     """.stripMargin
  }

  private def renderServiceServer(i: LegacyService, ts: Typespace): String = {
    val name = s"${i.id.name}Server"
    s"""export abstract class $name<C, D> extends ${i.id.name}Dispatcher<C, D> implements I${i.id.name}Server<C> {
       |    constructor(marshaller: Marshaller<D>) {
       |        super(marshaller, null);
       |        this.server = this;
       |    }
       |
       |${i.methods.map(m => renderServiceServerDummyMethod(m, ts)).mkString("\n").shift(4)}
       |}
     """.stripMargin
  }

  private def legacyRenderService(i: LegacyService, ts: Typespace): ServiceProduct = {
    val imports = TypeScriptImports(ts, i, i.id.domain.toPackage, List.empty, tsManifest)

    val svc =
      s"""// Models
         |${i.methods.map(me => renderRPCMethodModels(me, ts)).mkString("\n")}
         |
         |// Client
         |${renderServiceClient(i, ts)}
         |
         |// Dispatcher
         |${renderServiceDispatcher(i, ts)}
         |
         |// Base Server
         |${renderServiceServer(i, ts)}
         """.stripMargin

    val header =
      s"""${imports.render(ts)}
         |${importFromIRT(
          List("ServiceDispatcher", "Marshaller", "Void", "IncomingData", "OutgoingData", "ClientTransport", "Either", "Left as EitherLeft", "Right as EitherRight"),
          i.id.domain.toPackage,
        )}
         """.stripMargin

    ServiceProduct(svc, header, s"// ${i.id.name} client")
  }

  private def renderBuzzerClient(i: LegacyBuzzer, ts: Typespace): String = {
    s"""export interface I${i.id.name}Client {
       |${i.events.map(me => renderRPCMethodSignature(me, ts, spread = true)).mkString("\n").shift(4)}
       |}
       |
       |export class ${i.id.name}Client implements I${i.id.name}Client {
       |${renderRuntimeNamesBuzzer(i.id, s"${i.id.name}Client").shift(4)}
       |    protected _transport: ServerSocketTransport;
       |
       |    constructor(transport: ServerSocketTransport) {
       |        this._transport = transport;
       |    }
       |
       |    private send<I extends IncomingData, O extends OutgoingData>(method: string, data: I, inputType: {new(): I}, outputType: {new(data: any): O} ): Promise<O> {
       |        return new Promise((resolve, reject) => {
       |            this._transport.send(${i.id.name}Client.ClassName, method, data)
       |                .then((data: any) => {
       |                    try {
       |                        const output = new outputType(data);
       |                        resolve(output);
       |                    }
       |                    catch (err) {
       |                        reject(err);
       |                    }
       |                })
       |                .catch((err: any) => {
       |                    reject(err);
       |                });
       |            });
       |    }
       |${i.events.map(me => renderRPCClientMethod(i.id.name, me, ts)).mkString("\n").shift(4)}
       |}
     """.stripMargin
  }

  private def renderBuzzerDispatcher(i: LegacyBuzzer, ts: Typespace): String = {
    s"""export interface I${i.id.name}BuzzerHandlers<C> {
       |${i.events.map(me => renderRPCMethodSignature(me, ts, spread = true, forClient = false)).mkString("\n").shift(4)}
       |}
       |
       |export class ${i.id.name}Dispatcher<C, D> implements ServiceDispatcher<C, D> {
       |    private static readonly methods: string[] = [
       |${i.events.map(m => if (m.isInstanceOf[DefMethod.RPCMethod]) "        \"" + m.asInstanceOf[DefMethod.RPCMethod].name + "\"" else "").mkString(",\n")}\n    ];
       |    protected marshaller: Marshaller<D>;
       |    protected handlers: I${i.id.name}BuzzerHandlers<C>;
       |
       |    constructor(marshaller: Marshaller<D>, handlers: I${i.id.name}BuzzerHandlers<C>) {
       |        this.marshaller = marshaller;
       |        this.handlers = handlers;
       |    }
       |
       |    public getSupportedService(): string {
       |        return '${i.id.name}';
       |    }
       |
       |    public getSupportedMethods(): string[] {
       |        return  ${i.id.name}Dispatcher.methods;
       |    }
       |
       |    public dispatch(context: C, method: string, data: D | undefined): Promise<D> {
       |        switch (method) {
       |${i.events.map(m => renderServiceDispatcherHandler(m, "handlers", ts, useRawMarshaller = true)).mkString("\n").shift(12)}
       |            default:
       |                throw new Error(`Method $${method} is not supported by ${i.id.name}Dispatcher.`);
       |        }
       |    }
       |}
     """
  }

  private def renderBuzzerHandlerDummyMethod(member: DefMethod, ts: Typespace): String = {
    s"""public ${renderRPCMethodSignature(member, ts, spread = true, forClient = false)} {
       |    throw new Error('Not implemented.');
       |}
     """.stripMargin
  }

  private def renderBuzzerBase(i: LegacyBuzzer, ts: Typespace): String = {
    val name = s"${i.id.name}BuzzerHandlers"
    s"""export abstract class $name<C, D> extends ${i.id.name}Dispatcher<C, D> implements I${i.id.name}BuzzerHandlers<C> {
       |    constructor(marshaller: Marshaller<D>) {
       |        super(marshaller, null);
       |        this.handlers = this;
       |    }
       |
       |${i.events.map(m => renderBuzzerHandlerDummyMethod(m, ts)).mkString("\n").shift(4)}
       |}
     """.stripMargin
  }

  private def legacyRenderBuzzer(i: LegacyBuzzer, ts: Typespace): BuzzerProduct = {
    val imports = TypeScriptImports(ts, i, i.id.domain.toPackage, List.empty, tsManifest)

    val svc =
      s"""// Models
         |${i.events.map(me => renderRPCMethodModels(me, ts)).mkString("\n")}
         |
         |// Client
         |${renderBuzzerClient(i, ts)}
         |
         |// Dispatcher
         |${renderBuzzerDispatcher(i, ts)}
         |
         |// Buzzer Handlers Base
         |${renderBuzzerBase(i, ts)}
         """.stripMargin

    val header =
      s"""${imports.render(ts)}
         |${importFromIRT(
          List(
            "ServiceDispatcher",
            "Marshaller",
            "Void",
            "IncomingData",
            "OutgoingData",
            "ServerSocketTransport",
            "Either",
            "Left as EitherLeft",
            "Right as EitherRight",
          ),
          i.id.domain.toPackage,
        )}
         """.stripMargin

    BuzzerProduct(svc, header, s"// ${i.id.name}")
  }

  // -------------------- Test fixtures --------------------

  private def metaFor(domainId: DomainId): DomainMetadata =
    DomainMetadata(
      origin           = FSPath(domainId.toPackage :+ s"${domainId.id}.domain"),
      directInclusions = Seq.empty,
      directImports    = Seq.empty,
      meta             = emptyMeta,
    )

  private def newCtxFor(domainId: DomainId): DomainTSContext = {
    val newDomain = Domain(
      id                = domainId,
      meta              = metaFor(domainId),
      members           = Map.empty,
      roots             = Set.empty,
      ephemeralsOf      = Map.empty,
      ephemeralOwner    = Map.empty,
      flattenedStructs  = Map.empty,
      parents           = Map.empty,
      implementingDtos  = Map.empty,
      loops             = Set.empty,
      fingerprints      = Map.empty,
      domainFingerprint = Fingerprint(ByteVector.empty),
      imports           = Map.empty,
      consts            = List.empty,
      aliases           = Map.empty,
      userTypes         = Map.empty,
    )
    val parsedStub: DomainMeshResolved = new DomainMeshResolved {
      override def id: DomainId                                  = domainId
      override def imports: Seq[RawImport]                       = Seq.empty
      override def members: Seq[RawTopLevelDefn]                 = Seq.empty
      override def referenced: Map[DomainId, DomainMeshResolved] = Map.empty
      override def origin: FSPath                                = FSPath(domainId.toPackage :+ s"${domainId.id}.domain")
      override def directInclusions: Seq[RawInclusion]           = Seq.empty
      override def meta: RawNodeMeta                             = rawMeta
    }
    new DomainTSContext(newDomain, parsedStub, options)
  }

  private def legacyTypespaceForService(domainId: DomainId, svc: LegacyService): Typespace = {
    val legacyDomain = DomainDefinition(
      id         = domainId,
      meta       = metaFor(domainId),
      types      = Seq.empty,
      services   = Seq(svc),
      buzzers    = Seq.empty,
      streams    = Seq.empty,
      referenced = Map.empty,
    )
    new TypespaceImpl(legacyDomain)
  }

  private def legacyTypespaceForBuzzer(domainId: DomainId, bz: LegacyBuzzer): Typespace = {
    val legacyDomain = DomainDefinition(
      id         = domainId,
      meta       = metaFor(domainId),
      types      = Seq.empty,
      services   = Seq.empty,
      buzzers    = Seq(bz),
      streams    = Seq.empty,
      referenced = Map.empty,
    )
    new TypespaceImpl(legacyDomain)
  }

  test("service with single Singular method: byte-equal to legacy") {
    val svcId   = ServiceId(domainId, "Ping")
    val xField  = Field(Primitive.TInt32, "x", emptyMeta)
    val method = RPCMethod(
      name      = "ping",
      signature = Signature(
        input  = SimpleStructure(concepts = List.empty, fields = List(xField)),
        output = Singular(Primitive.TString),
      ),
      meta = emptyMeta,
    )
    val newSvc    = NewTypeDef.Service(svcId, List(method), emptyMeta)
    val legacySvc = LegacyService(svcId, List(method), emptyMeta)
    val ts        = legacyTypespaceForService(domainId, legacySvc)

    val ctxNew   = newCtxFor(domainId)
    val actual   = ctxNew.serviceRenderer.renderService(newSvc)
    val expected = legacyRenderService(legacySvc, ts)

    val _ = assert(expected.client == actual.client, s"client diverges\nlegacy=${expected.client}\nnew   =${actual.client}")
    val _ = assert(expected.header == actual.header, s"header diverges")
    val _ = assert(expected.preamble == actual.preamble, s"preamble diverges")
  }

  test("buzzer with single Void-output event: byte-equal to legacy") {
    val bzId          = BuzzerId(domainId, "Notif")
    val payloadField  = Field(Primitive.TString, "payload", emptyMeta)
    val event = RPCMethod(
      name      = "fire",
      signature = Signature(
        input  = SimpleStructure(concepts = List.empty, fields = List(payloadField)),
        output = Void(),
      ),
      meta = emptyMeta,
    )
    val newBz    = NewTypeDef.Buzzer(bzId, List(event), emptyMeta)
    val legacyBz = LegacyBuzzer(bzId, List(event), emptyMeta)
    val ts       = legacyTypespaceForBuzzer(domainId, legacyBz)

    val ctxNew   = newCtxFor(domainId)
    val actual   = ctxNew.serviceRenderer.renderBuzzer(newBz)
    val expected = legacyRenderBuzzer(legacyBz, ts)

    val _ = assert(expected.client == actual.client, s"client diverges\nlegacy=${expected.client}\nnew   =${actual.client}")
    val _ = assert(expected.header == actual.header, s"header diverges")
    val _ = assert(expected.preamble == actual.preamble, s"preamble diverges")
  }
}
