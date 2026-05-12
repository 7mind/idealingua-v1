package izumi.idealingua.translator.tocsharp.domain

import izumi.fundamentals.platform.strings.IzString._
import izumi.idealingua.model.common.TypeId.DTOId
import izumi.idealingua.model.il.ast.typed.DefMethod
import izumi.idealingua.model.il.ast.typed.DefMethod.Output.Algebraic
import izumi.idealingua.model.typespace.Typespace
import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.translator.tocsharp.products.CogenProduct.{BuzzerProduct, ServiceProduct}
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

/** Renders a new-IR `TypeDef.Service` / `TypeDef.Buzzer` as the same
  * `ServiceProduct` / `BuzzerProduct` the legacy
  * `CSharpTranslator.renderService` / `renderBuzzer` produces (modulo
  * the extension chain).
  *
  * IMPL-7c Phase B M3: byte-parity port. Emits the four-block service
  * shape:
  *   - Usings block: per-method ADT member `using _<Name> = ...`
  *     aliases (only when method output is `Algebraic`).
  *   - Holder static class `<Name> { ... }` containing the `In<Method>`
  *     / `Out<Method>` model classes for every method.
  *   - Service Client block: `I<Name>Client<C>` + `<Name>ClientGeneric<C>`
  *     + `<Name>Client : <Name>ClientGeneric<IClientTransportContext>`.
  *   - Service Dispatcher block: `I<Name>Server<C>` + `<Name>Dispatcher<C, D>`
  *     with `Dispatch` switch over method names.
  *   - Service Server Base block: `<Name>Server<C, D> : <Name>Dispatcher`
  *     with virtual default implementations.
  *
  * Buzzer parallel emits the same shape but routed through
  * `IClientSocketTransport<C, D>` (vs `IClientTransport<C>`) and
  * `<Name>BuzzerHandlers` (vs `<Name>Server`). Per F16 absorption, the
  * services + buzzers live in `domain.userTypes` (new IR consolidation).
  *
  * `Typespace` + `CSharpImports` are threaded per-call mirroring the M2
  * convention. The new-IR `Service` / `Buzzer` cases carry `methods` /
  * `events` directly; iteration order follows declaration order (the
  * `List` preserves it, see master plan §4 "Field-ordering invariant").
  *
  * Extension chain (`ext.imports(ctx, defn)` for the per-defn import
  * augmentation) is omitted: the default C# extension set
  * (`JsonNetExtension`) has no `imports` overrides for `Adt` / `Service`
  * / `Buzzer` / structural cases that would change the import set, so
  * the pre-extension product is byte-equal to the post-extension
  * product for the default extension list. The legacy renderer threads
  * a synthetic `Adt(AdtId(TypePath(DomainId.Undefined, Seq.empty),
  * "FakeName"), List.empty, NodeMeta.empty)` for the import-collection
  * pass — left in place at the call site comment but unused in the new
  * renderer (the synthetic entry's only purpose was to trigger imports
  * for ADT-using methods through extensions that don't currently exist
  * in the default set).
  */
final class DomainCSServiceRenderer(ctx: DomainCSContext, adtRenderer: DomainCSAdtRenderer) {

  private val methodProduct = new DomainCSServiceMethodProduct(ctx, adtRenderer)

  // -- Service -------------------------------------------------------------

  def renderService(i: NewTypeDef.Service, ts: Typespace, im: CSharpImports): ServiceProduct = {
    implicit val _ts: Typespace     = ts
    implicit val _im: CSharpImports = im

    val svc =
      s"""${renderServiceUsings(i)}
         |
         |public static class ${i.id.name} {
         |${renderServiceModels(i).shift(4)}
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

  private def renderServiceUsings(i: NewTypeDef.Service)(implicit imports: CSharpImports, ts: Typespace): String =
    i.methods.flatMap(me => renderServiceMethodAdtUsings(me)).distinct.mkString("\n")

  private def renderServiceModels(i: NewTypeDef.Service)(implicit imports: CSharpImports, ts: Typespace): String =
    i.methods.map(me => renderServiceMethodModels(i, me)).mkString("\n")

  private def renderServiceMethodModels(i: NewTypeDef.Service, method: DefMethod)(implicit imports: CSharpImports, ts: Typespace): String = method match {
    case m: DefMethod.RPCMethod =>
      s"""${if (m.signature.input.fields.isEmpty) "" else methodProduct.renderServiceMethodInModel(DTOId(i.id, s"In${m.name.capitalize}"), m.signature.input)}
         |${methodProduct.renderServiceMethodOutModel(i.id, s"Out${m.name.capitalize}", m.signature.output)}
       """.stripMargin
  }

  private def renderServiceClient(i: NewTypeDef.Service)(implicit imports: CSharpImports, ts: Typespace): String = {
    val name = s"${i.id.name}Client"
    s"""public interface I$name<C> where C: class, IClientTransportContext {
       |${i.methods.map(m => methodProduct.renderRPCMethodSignature(i.id.name, m, forClient = true) + ";").mkString("\n").shift(4)}
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
       |${i.methods.map(me => methodProduct.renderRPCClientMethod(i.id.name, me)).mkString("\n").shift(4)}
       |}
       |
       |public class $name: ${name}Generic<IClientTransportContext> {
       |    public $name(IClientTransport<IClientTransportContext> t): base(t) {}
       |}
     """.stripMargin
  }

  private def renderServiceDispatcher(i: NewTypeDef.Service)(implicit imports: CSharpImports, ts: Typespace): String = {
    s"""public interface I${i.id.name}Server<C> {
       |${i.methods.map(m => methodProduct.renderRPCMethodSignature(i.id.name, m, forClient = false) + ";").mkString("\n").shift(4)}
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
       |${i.methods.map(m => methodProduct.renderRPCDispatcherHandler(i.id.name, m, "server")).mkString("\n").shift(12)}
       |            default:
       |                throw new DispatcherException(string.Format("Method {0} is not supported by ${i.id.name}Dispatcher.", method));
       |        }
       |    }
       |}
     """.stripMargin
  }

  private def renderServiceServerBase(i: NewTypeDef.Service)(implicit imports: CSharpImports, ts: Typespace): String = {
    val name = s"${i.id.name}Server"
    s"""public abstract class $name<C, D>: ${i.id.name}Dispatcher<C, D>,  I${i.id.name}Server<C> {
       |    public $name(IMarshaller<D> marshaller): base(marshaller, null) {
       |        server = this;
       |    }
       |
       |${i.methods.map(m => methodProduct.renderRPCDummyMethod(i.id.name, m, virtual = true)).mkString("\n").shift(4)}
       |}
     """.stripMargin
  }

  // -- Buzzer --------------------------------------------------------------

  def renderBuzzer(i: NewTypeDef.Buzzer, ts: Typespace, im: CSharpImports): BuzzerProduct = {
    implicit val _ts: Typespace     = ts
    implicit val _im: CSharpImports = im

    val svc =
      s"""${renderBuzzerUsings(i)}
         |
         |public static class ${i.id.name} {
         |${renderBuzzerModels(i).shift(4)}
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

  private def renderBuzzerUsings(i: NewTypeDef.Buzzer)(implicit imports: CSharpImports, ts: Typespace): String =
    i.events.flatMap(me => renderServiceMethodAdtUsings(me)).distinct.mkString("\n")

  private def renderBuzzerModels(i: NewTypeDef.Buzzer)(implicit imports: CSharpImports, ts: Typespace): String =
    i.events.map(me => renderBuzzerMethodModels(i, me)).mkString("\n")

  private def renderBuzzerMethodModels(i: NewTypeDef.Buzzer, method: DefMethod)(implicit imports: CSharpImports, ts: Typespace): String = method match {
    case m: DefMethod.RPCMethod =>
      s"""${if (m.signature.input.fields.isEmpty) "" else methodProduct.renderServiceMethodInModel(DTOId(i.id, s"In${m.name.capitalize}"), m.signature.input)}
         |${methodProduct.renderBuzzerMethodOutModel(i.id, s"Out${m.name.capitalize}", m.signature.output)}
       """.stripMargin
  }

  private def renderBuzzerClient(i: NewTypeDef.Buzzer)(implicit imports: CSharpImports, ts: Typespace): String = {
    val name = s"${i.id.name}Client"
    s"""public interface I$name<C> where C: class, IClientTransportContext {
       |${i.events.map(m => methodProduct.renderRPCMethodSignature(i.id.name, m, forClient = true) + ";").mkString("\n").shift(4)}
       |}
       |
       |public class ${name}Generic<C, D>: I$name<C> where C: class, IClientTransportContext {
       |    public IClientSocketTransport<C, D> Transport { get; private set; }
       |
       |    public ${name}Generic(IClientSocketTransport<C, D> t) {
       |        Transport = t;
       |    }
       |
       |${i.events.map(me => methodProduct.renderRPCClientMethod(i.id.name, me)).mkString("\n").shift(4)}
       |}
       |
       |public class $name: ${name}Generic<IClientTransportContext, string> {
       |    public $name(IClientSocketTransport<IClientTransportContext, string> t): base(t) {}
       |}
     """.stripMargin
  }

  private def renderBuzzerDispatcher(i: NewTypeDef.Buzzer)(implicit imports: CSharpImports, ts: Typespace): String = {
    s"""public interface I${i.id.name}BuzzerHandlers<C> {
       |${i.events.map(m => methodProduct.renderRPCMethodSignature(i.id.name, m, forClient = false) + ";").mkString("\n").shift(4)}
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
       |${i.events.map(m => methodProduct.renderRPCDispatcherHandler(i.id.name, m, "handlers")).mkString("\n").shift(12)}
       |            default:
       |                throw new DispatcherException(string.Format("Method {0} is not supported by ${i.id.name}Dispatcher.", method));
       |        }
       |    }
       |}
     """.stripMargin
  }

  private def renderBuzzerHandlersDummy(i: NewTypeDef.Buzzer)(implicit imports: CSharpImports, ts: Typespace): String = {
    val name = s"${i.id.name}BuzzerHandlers"
    s"""public abstract class $name<C, D>: ${i.id.name}Dispatcher<C, D>,  I${i.id.name}BuzzerHandlers<C> {
       |    public $name(IMarshaller<D> marshaller): base(marshaller, null) {
       |        handlers = this;
       |    }
       |
       |${i.events.map(m => methodProduct.renderRPCDummyMethod(i.id.name, m, virtual = true)).mkString("\n").shift(4)}
       |}
     """.stripMargin
  }

  // -- Shared --------------------------------------------------------------

  /** Mirror of legacy `renderServiceMethodAdtUsings` (lines 641-648). */
  private def renderServiceMethodAdtUsings(method: DefMethod)(implicit imports: CSharpImports, ts: Typespace): List[String] = method match {
    case m: DefMethod.RPCMethod =>
      m.signature.output match {
        case al: Algebraic => al.alternatives.map(adtm => adtRenderer.renderAdtUsings(adtm))
        case _             => List.empty
      }
  }

}
