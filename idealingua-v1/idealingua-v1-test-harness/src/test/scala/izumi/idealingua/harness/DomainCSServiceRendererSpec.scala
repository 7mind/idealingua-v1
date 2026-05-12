package izumi.idealingua.harness

import izumi.fundamentals.platform.strings.IzString._
import izumi.idealingua.model.common.TypeId.{BuzzerId, DTOId, ServiceId}
import izumi.idealingua.model.common.{DomainId, Primitive}
import izumi.idealingua.model.il.ast.InputPosition
import izumi.idealingua.model.il.ast.raw.defns.{RawNodeMeta, RawTopLevelDefn}
import izumi.idealingua.model.il.ast.raw.domains.{DomainMeshResolved, Import => RawImport}
import izumi.idealingua.model.il.ast.raw.models.{Inclusion => RawInclusion}
import izumi.idealingua.model.il.ast.typed.DefMethod.Output.{Singular, Void}
import izumi.idealingua.model.il.ast.typed.DefMethod.{RPCMethod, Signature}
import izumi.idealingua.model.il.ast.typed.{Buzzer => LegacyBuzzer, Service => LegacyService}
import izumi.idealingua.model.il.ast.typed.{DefMethod, DomainDefinition, DomainMetadata, Field, NodeMeta, SimpleStructure}
import izumi.idealingua.model.loader.FSPath
import izumi.idealingua.model.publishing.manifests.CSharpBuildManifest
import izumi.idealingua.model.typespace.{Typespace, TypespaceImpl}
import izumi.idealingua.translator.CompilerOptions
import izumi.idealingua.translator.IDLLanguage
import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.translator.tocsharp.domain.DomainCSContext
import izumi.idealingua.translator.tocsharp.extensions.CSharpTranslatorExtension
import izumi.idealingua.translator.tocsharp.products.CogenProduct.{BuzzerProduct, ServiceProduct}
import izumi.idealingua.translator.tocsharp.types.{CSharpClass, CSharpField, CSharpType}
import izumi.idealingua.typer.ir.{Domain, Fingerprint, TypeDef => NewTypeDef}
import org.scalatest.funsuite.AnyFunSuite
import scodec.bits.ByteVector

/** PR-02 IMPL-7c Phase B M3: byte-parity unit test for
  * `DomainCSServiceRenderer`.
  *
  * Two cases:
  *   - Service with one Singular-output method: emits the Usings /
  *     Models / Client / Dispatcher / ServerBase quintuple and matches
  *     the legacy `CSharpTranslator.renderService` mirror byte-for-byte.
  *   - Buzzer with one Void-output event: emits the Buzzer parallel
  *     (Client / Dispatcher / Handlers) and matches the legacy
  *     `renderBuzzer` mirror byte-for-byte.
  */
final class DomainCSServiceRendererSpec extends AnyFunSuite {

  private val domainId   = DomainId(Seq("idltest"), "cs_service_render_spec")
  private val emptyMeta  = NodeMeta.empty
  private val rawMeta    = RawNodeMeta(None, Seq.empty, InputPosition.Undefined)
  private val csManifest = CSharpBuildManifest.example
  private val emptyExts: Seq[CSharpTranslatorExtension] = Seq.empty
  private val options    = CompilerOptions[CSharpTranslatorExtension, CSharpBuildManifest](IDLLanguage.CSharp, emptyExts, csManifest)

  // -------------------- Legacy mirror helpers --------------------

  private def isServiceMethodReturnExistent(method: DefMethod.RPCMethod): Boolean = method.signature.output match {
    case _: Void => false
    case _       => true
  }

  private def renderRPCMethodOutputModel(svcOrBuzzer: String, method: DefMethod.RPCMethod)(implicit im: CSharpImports, ts: Typespace): String =
    method.signature.output match {
      case si: Singular => s"${CSharpType(si.typeId).renderType(true)}"
      case _: Void      => "void"
      case _            => throw new Exception("non-singular non-void out unsupported in mirror")
    }

  private def renderRPCMethodOutputSignature(svcOrBuzzer: String, method: DefMethod.RPCMethod)(implicit im: CSharpImports, ts: Typespace): String =
    s"${renderRPCMethodOutputModel(svcOrBuzzer, method)}"

  private def renderRPCMethodSignature(svcOrBuzzer: String, method: DefMethod, forClient: Boolean)(implicit im: CSharpImports, ts: Typespace): String = {
    method match {
      case m: DefMethod.RPCMethod =>
        val returnValue = if (isServiceMethodReturnExistent(m)) s"<${renderRPCMethodOutputSignature(svcOrBuzzer, m)}>" else ""
        val callback =
          s"${if (m.signature.input.fields.isEmpty) "" else ", "}Action$returnValue onSuccess, Action<Exception> onFailure, Action onAny = null, C ctx = null"
        val fields  = m.signature.input.fields.map(f => CSharpType(f.typeId).renderType(true) + " " + CSharpField.safeVarName(f.name)).mkString(", ")
        val context = s"C ctx${if (m.signature.input.fields.isEmpty) "" else ", "}"
        if (forClient) {
          s"void ${m.name.capitalize}($fields$callback)"
        } else {
          s"${renderRPCMethodOutputSignature(svcOrBuzzer, m)} ${m.name.capitalize}($context$fields)"
        }
    }
  }

  private def renderServiceMethodInModel(i: DTOId, structure: SimpleStructure)(implicit im: CSharpImports, ts: Typespace): String = {
    val csClass = CSharpClass(i, structure)
    s"""
       |${csClass.render(withWrapper = true, withSlices = false, withRTTI = true)}
       |""".stripMargin
  }

  private def renderServiceMethodOutModel(@annotation.unused svcOrBuzzerId: izumi.idealingua.model.common.TypeId, @annotation.unused name: String, out: DefMethod.Output)(implicit @annotation.unused im: CSharpImports, @annotation.unused ts: Typespace): String =
    out match {
      case si: Singular => s"// ${si.typeId}"
      case _: Void      => ""
      case _            => throw new Exception("unsupported in mirror (singular/void only)")
    }

  private def renderServiceMethodModels(i: LegacyService, method: DefMethod)(implicit im: CSharpImports, ts: Typespace): String = method match {
    case m: DefMethod.RPCMethod =>
      s"""${if (m.signature.input.fields.isEmpty) "" else renderServiceMethodInModel(DTOId(i.id, s"In${m.name.capitalize}"), m.signature.input)}
         |${renderServiceMethodOutModel(i.id, s"Out${m.name.capitalize}", m.signature.output)}
       """.stripMargin
  }

  private def renderBuzzerMethodModels(i: LegacyBuzzer, method: DefMethod)(implicit im: CSharpImports, ts: Typespace): String = method match {
    case m: DefMethod.RPCMethod =>
      s"""${if (m.signature.input.fields.isEmpty) "" else renderServiceMethodInModel(DTOId(i.id, s"In${m.name.capitalize}"), m.signature.input)}
         |${renderServiceMethodOutModel(i.id, s"Out${m.name.capitalize}", m.signature.output)}
       """.stripMargin
  }

  private def renderRPCClientMethod(svcOrBuzzer: String, method: DefMethod)(implicit im: CSharpImports, ts: Typespace): String = method match {
    case m: DefMethod.RPCMethod =>
      m.signature.output match {
        case _: Singular =>
          s"""public ${renderRPCMethodSignature(svcOrBuzzer, method, forClient = true)} {
             |    ${
              if (m.signature.input.fields.isEmpty) "// No input params for this method"
              else s"var inData = new $svcOrBuzzer.In${m.name.capitalize}(${m.signature.input.fields.map(ff => CSharpField.safeVarName(ff.name)).mkString(", ")});"
            }
             |    Transport.Send<${if (m.signature.input.fields.nonEmpty) s"$svcOrBuzzer.In${m.name.capitalize}" else "object"}, ${renderRPCMethodOutputModel(
              svcOrBuzzer,
              m,
            )}>("$svcOrBuzzer", "${m.name}", ${if (m.signature.input.fields.isEmpty) "null" else "inData"},
             |        new ClientTransportCallback<${renderRPCMethodOutputModel(svcOrBuzzer, m)}>(onSuccess, onFailure, onAny), ctx);
             |}
       """.stripMargin
        case _: Void =>
          s"""public ${renderRPCMethodSignature(svcOrBuzzer, method, forClient = true)} {
             |    ${
              if (m.signature.input.fields.isEmpty) "// No input params for this method"
              else s"var inData = new $svcOrBuzzer.In${m.name.capitalize}(${m.signature.input.fields.map(ff => CSharpField.safeVarName(ff.name)).mkString(", ")});"
            }
             |    Transport.Send<${if (m.signature.input.fields.nonEmpty) s"$svcOrBuzzer.In${m.name.capitalize}" else "object"}, IRT.Void>("$svcOrBuzzer", "${m.name}", ${
              if (m.signature.input.fields.isEmpty) "null" else "inData"
            },
             |        new ClientTransportCallback<IRT.Void>(_ => onSuccess(), onFailure, onAny), ctx);
             |}
       """.stripMargin
        case _ => throw new Exception("unsupported in mirror")
      }
  }

  private def renderRPCDispatcherHandler(svcOrBuzzer: String, method: DefMethod, server: String)(implicit im: CSharpImports, ts: Typespace): String = method match {
    case m: DefMethod.RPCMethod =>
      if (isServiceMethodReturnExistent(m))
        s"""case "${m.name}": {
           |    ${
            if (m.signature.input.fields.isEmpty) "// No input params for this method"
            else s"var obj = marshaller.Unmarshal<${if (m.signature.input.fields.nonEmpty) s"$svcOrBuzzer.In${m.name.capitalize}" else "object"}>(data);"
          }
           |    return marshaller.Marshal<${renderRPCMethodOutputModel(svcOrBuzzer, m)}>(\n        $server.${m.name.capitalize}(ctx${
            if (m.signature.input.fields.isEmpty) "" else ", "
          }${m.signature.input.fields.map(f => s"obj.${f.name.capitalize}").mkString(", ")})\n    );
           |}
         """.stripMargin
      else
        s"""case "${m.name}": {
           |    ${
            if (m.signature.input.fields.isEmpty) "// No input params for this method"
            else s"var obj = marshaller.Unmarshal<${if (m.signature.input.fields.nonEmpty) s"$svcOrBuzzer.In${m.name.capitalize}" else "object"}>(data);"
          }
           |    $server.${m.name.capitalize}(ctx${if (m.signature.input.fields.isEmpty) "" else ", "}${m.signature.input.fields
            .map(f => s"obj.${f.name.capitalize}").mkString(", ")});
           |    return marshaller.Marshal<IRT.Void>(null);
           |}
       """.stripMargin
  }

  private def renderRPCDummyMethod(svcOrBuzzer: String, member: DefMethod, virtual: Boolean)(implicit im: CSharpImports, ts: Typespace): String = {
    val retValue = member match {
      case m: DefMethod.RPCMethod =>
        m.signature.output match {
          case s: Singular => "return " + CSharpType(s.typeId).defaultValue + ";"
          case _: Void     => "// Nothing to return"
          case _           => throw new Exception("unsupported")
        }
      case _ => throw new Exception("unsupported")
    }
    s"""public ${if (virtual) "virtual " else ""}${renderRPCMethodSignature(svcOrBuzzer, member, forClient = false)} {
       |    $retValue
       |}
     """.stripMargin
  }

  // -- Service block mirrors --

  private def renderServiceClient(i: LegacyService)(implicit im: CSharpImports, ts: Typespace): String = {
    val name = s"${i.id.name}Client"
    s"""public interface I$name<C> where C: class, IClientTransportContext {
       |${i.methods.map(m => renderRPCMethodSignature(i.id.name, m, forClient = true) + ";").mkString("\n").shift(4)}
       |}
       |
       |public class ${name}Generic<C>: I$name<C> where C: class, IClientTransportContext {
       |    public IClientTransport<C> Transport { get; private set; }
       |
       |    public ${name}Generic(IClientTransport<C> t) {
       |        Transport = t;
       |    }
       |
       |    public void SetHTTPTransport(string endpoint, IJsonMarshaller marshaller, bool blocking = false, int timeout = 60) {
       |        if (blocking) {
       |            this.Transport = new SyncHttpTransportGeneric<C>(endpoint, marshaller, timeout);
       |        } else {
       |            this.Transport = new AsyncHttpTransportGeneric<C>(endpoint, marshaller, timeout);
       |        }
       |    }
       |${i.methods.map(me => renderRPCClientMethod(i.id.name, me)).mkString("\n").shift(4)}
       |}
       |
       |public class $name: ${name}Generic<IClientTransportContext> {
       |    public $name(IClientTransport<IClientTransportContext> t): base(t) {}
       |}
     """.stripMargin
  }

  private def renderServiceDispatcher(i: LegacyService)(implicit im: CSharpImports, ts: Typespace): String = {
    s"""public interface I${i.id.name}Server<C> {
       |${i.methods.map(m => renderRPCMethodSignature(i.id.name, m, forClient = false) + ";").mkString("\n").shift(4)}
       |}
       |
       |public class ${i.id.name}Dispatcher<C, D>: IServiceDispatcher<C, D> {
       |    private static readonly string[] methods = { ${i.methods
        .map(m => if (m.isInstanceOf[DefMethod.RPCMethod]) "\"" + m.asInstanceOf[DefMethod.RPCMethod].name + "\"" else "").mkString(", ")} };
       |    protected IMarshaller<D> marshaller;
       |    protected I${i.id.name}Server<C> server;
       |
       |    public ${i.id.name}Dispatcher(IMarshaller<D> marshaller, I${i.id.name}Server<C> server) {
       |        this.marshaller = marshaller;
       |        this.server = server;
       |    }
       |
       |    public string GetSupportedService() {
       |        return "${i.id.name}";
       |    }
       |
       |    public string[] GetSupportedMethods() {
       |        return ${i.id.name}Dispatcher<C, D>.methods;
       |    }
       |
       |    public D Dispatch(C ctx, string method, D data) {
       |        switch(method) {
       |${i.methods.map(m => renderRPCDispatcherHandler(i.id.name, m, "server")).mkString("\n").shift(12)}
       |            default:
       |                throw new DispatcherException(string.Format("Method {0} is not supported by ${i.id.name}Dispatcher.", method));
       |        }
       |    }
       |}
     """.stripMargin
  }

  private def renderServiceServerBase(i: LegacyService)(implicit im: CSharpImports, ts: Typespace): String = {
    val name = s"${i.id.name}Server"
    s"""public abstract class $name<C, D>: ${i.id.name}Dispatcher<C, D>,  I${i.id.name}Server<C> {
       |    public $name(IMarshaller<D> marshaller): base(marshaller, null) {
       |        server = this;
       |    }
       |
       |${i.methods.map(m => renderRPCDummyMethod(i.id.name, m, virtual = true)).mkString("\n").shift(4)}
       |}
     """.stripMargin
  }

  private def legacyRenderService(i: LegacyService)(implicit im: CSharpImports, ts: Typespace): ServiceProduct = {
    val svc =
      s"""${"" /* usings */}
         |
         |public static class ${i.id.name} {
         |${i.methods.map(me => renderServiceMethodModels(i, me)).mkString("\n").shift(4)}
         |}
         |
         |// ============== Service Client ==============
         |${renderServiceClient(i)}
         |
         |// ============== Service Dispatcher ==============
         |${renderServiceDispatcher(i)}
         |
         |// ============== Service Server Base ==============
         |${renderServiceServerBase(i)}
         """.stripMargin

    ServiceProduct(
      svc,
      im.renderImports(List("IRT", "IRT.Marshaller", "IRT.Transport.Client", "System", "System.Collections", "System.Collections.Generic")),
    )
  }

  // -- Buzzer block mirrors --

  private def renderBuzzerClient(i: LegacyBuzzer)(implicit im: CSharpImports, ts: Typespace): String = {
    val name = s"${i.id.name}Client"
    s"""public interface I$name<C> where C: class, IClientTransportContext {
       |${i.events.map(m => renderRPCMethodSignature(i.id.name, m, forClient = true) + ";").mkString("\n").shift(4)}
       |}
       |
       |public class ${name}Generic<C, D>: I$name<C> where C: class, IClientTransportContext {
       |    public IClientSocketTransport<C, D> Transport { get; private set; }
       |
       |    public ${name}Generic(IClientSocketTransport<C, D> t) {
       |        Transport = t;
       |    }
       |
       |${i.events.map(me => renderRPCClientMethod(i.id.name, me)).mkString("\n").shift(4)}
       |}
       |
       |public class $name: ${name}Generic<IClientTransportContext, string> {
       |    public $name(IClientSocketTransport<IClientTransportContext, string> t): base(t) {}
       |}
     """.stripMargin
  }

  private def renderBuzzerDispatcher(i: LegacyBuzzer)(implicit im: CSharpImports, ts: Typespace): String = {
    s"""public interface I${i.id.name}BuzzerHandlers<C> {
       |${i.events.map(m => renderRPCMethodSignature(i.id.name, m, forClient = false) + ";").mkString("\n").shift(4)}
       |}
       |
       |public class ${i.id.name}Dispatcher<C, D>: IServiceDispatcher<C, D> {
       |    private static readonly string[] methods = { ${i.events
        .map(m => if (m.isInstanceOf[DefMethod.RPCMethod]) "\"" + m.asInstanceOf[DefMethod.RPCMethod].name + "\"" else "").mkString(", ")} };
       |    protected IMarshaller<D> marshaller;
       |    protected I${i.id.name}BuzzerHandlers<C> handlers;
       |
       |    public ${i.id.name}Dispatcher(IMarshaller<D> marshaller, I${i.id.name}BuzzerHandlers<C> handlers) {
       |        this.marshaller = marshaller;
       |        this.handlers = handlers;
       |    }
       |
       |    public string GetSupportedService() {
       |        return "${i.id.name}";
       |    }
       |
       |    public string[] GetSupportedMethods() {
       |        return ${i.id.name}Dispatcher<C, D>.methods;
       |    }
       |
       |    public D Dispatch(C ctx, string method, D data) {
       |        switch(method) {
       |${i.events.map(m => renderRPCDispatcherHandler(i.id.name, m, "handlers")).mkString("\n").shift(12)}
       |            default:
       |                throw new DispatcherException(string.Format("Method {0} is not supported by ${i.id.name}Dispatcher.", method));
       |        }
       |    }
       |}
     """.stripMargin
  }

  private def renderBuzzerHandlersDummy(i: LegacyBuzzer)(implicit im: CSharpImports, ts: Typespace): String = {
    val name = s"${i.id.name}BuzzerHandlers"
    s"""public abstract class $name<C, D>: ${i.id.name}Dispatcher<C, D>,  I${i.id.name}BuzzerHandlers<C> {
       |    public $name(IMarshaller<D> marshaller): base(marshaller, null) {
       |        handlers = this;
       |    }
       |
       |${i.events.map(m => renderRPCDummyMethod(i.id.name, m, virtual = true)).mkString("\n").shift(4)}
       |}
     """.stripMargin
  }

  private def legacyRenderBuzzer(i: LegacyBuzzer)(implicit im: CSharpImports, ts: Typespace): BuzzerProduct = {
    val svc =
      s"""${"" /* usings */}
         |
         |public static class ${i.id.name} {
         |${i.events.map(me => renderBuzzerMethodModels(i, me)).mkString("\n").shift(4)}
         |}
         |
         |// ============== Client ==============
         |${renderBuzzerClient(i)}
         |
         |// ============== Dispatcher ==============
         |${renderBuzzerDispatcher(i)}
         |
         |// ============== Buzzer Handlers Base ==============
         |${renderBuzzerHandlersDummy(i)}
         """.stripMargin

    BuzzerProduct(
      svc,
      im.renderImports(List("IRT", "IRT.Marshaller", "IRT.Transport.Client", "System", "System.Collections", "System.Collections.Generic")),
    )
  }

  // -------------------- Test fixtures --------------------

  private def metaFor(domainId: DomainId): DomainMetadata =
    DomainMetadata(
      origin           = FSPath(domainId.toPackage :+ s"${domainId.id}.domain"),
      directInclusions = Seq.empty,
      directImports    = Seq.empty,
      meta             = emptyMeta,
    )

  private def newCtxFor(d: DomainId): DomainCSContext = {
    val newDomain = Domain(
      id                = d,
      meta              = metaFor(d),
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
      override def id: DomainId                                  = d
      override def imports: Seq[RawImport]                       = Seq.empty
      override def members: Seq[RawTopLevelDefn]                 = Seq.empty
      override def referenced: Map[DomainId, DomainMeshResolved] = Map.empty
      override def origin: FSPath                                = FSPath(d.toPackage :+ s"${d.id}.domain")
      override def directInclusions: Seq[RawInclusion]           = Seq.empty
      override def meta: RawNodeMeta                             = rawMeta
    }
    new DomainCSContext(newDomain, parsedStub, options)
  }

  private def legacyTypespaceForService(d: DomainId, svc: LegacyService): Typespace = {
    val legacyDomain = DomainDefinition(
      id         = d,
      meta       = metaFor(d),
      types      = Seq.empty,
      services   = Seq(svc),
      buzzers    = Seq.empty,
      streams    = Seq.empty,
      referenced = Map.empty,
    )
    new TypespaceImpl(legacyDomain)
  }

  private def legacyTypespaceForBuzzer(d: DomainId, bz: LegacyBuzzer): Typespace = {
    val legacyDomain = DomainDefinition(
      id         = d,
      meta       = metaFor(d),
      types      = Seq.empty,
      services   = Seq.empty,
      buzzers    = Seq(bz),
      streams    = Seq.empty,
      referenced = Map.empty,
    )
    new TypespaceImpl(legacyDomain)
  }

  test("service with single Singular method: byte-equal to legacy") {
    val svcId  = ServiceId(domainId, "Ping")
    val xField = Field(Primitive.TInt32, "x", emptyMeta)
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
    implicit val ts: Typespace     = legacyTypespaceForService(domainId, legacySvc)
    implicit val im: CSharpImports = CSharpImports(legacySvc, svcId.domain.toPackage, List.empty)

    val ctxNew   = newCtxFor(domainId)
    val actual   = ctxNew.serviceRenderer.renderService(newSvc, im)
    val expected = legacyRenderService(legacySvc)

    val _ = assert(expected.client == actual.client, s"client diverges\nlegacy=${expected.client}\nnew   =${actual.client}")
    val _ = assert(expected.header == actual.header, s"header diverges")
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
    implicit val ts: Typespace     = legacyTypespaceForBuzzer(domainId, legacyBz)
    implicit val im: CSharpImports = CSharpImports(legacyBz, bzId.domain.toPackage, List.empty)

    val ctxNew   = newCtxFor(domainId)
    val actual   = ctxNew.serviceRenderer.renderBuzzer(newBz, im)
    val expected = legacyRenderBuzzer(legacyBz)

    val _ = assert(expected.client == actual.client, s"client diverges\nlegacy=${expected.client}\nnew   =${actual.client}")
    val _ = assert(expected.header == actual.header, s"header diverges")
  }
}
