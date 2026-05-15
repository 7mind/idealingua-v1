package izumi.idealingua.translator.tocsharp.domain

import izumi.fundamentals.platform.strings.IzString._
import izumi.fundamentals.platform.strings.TextTree
import izumi.fundamentals.platform.strings.TextTree.*
import izumi.idealingua.model.common.TypeId.DTOId
import izumi.idealingua.model.il.ast.typed.DefMethod
import izumi.idealingua.model.il.ast.typed.DefMethod.Output.Algebraic
import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.translator.tocsharp.products.CogenProduct.{BuzzerProduct, ServiceProduct}
import izumi.idealingua.typer.ir.{Domain, TypeDef => NewTypeDef}

/** Renders a new-IR `TypeDef.Service` / `TypeDef.Buzzer` as the same
  * `ServiceProduct` / `BuzzerProduct` the legacy
  * `CSharpTranslator.renderService` / `renderBuzzer` produces.
  *
  * F-TextTree M3 — the top-level service / buzzer envelopes are composed
  * as `TextTree[CSRefHandle]` and rendered via `.mapRender(resolver.resolve)`
  * at the product boundary. Inner section helpers (`renderServiceClient`,
  * `renderServiceDispatcher`, etc.) continue to return `String`: the
  * sections are heavily templated with conditional substitutions
  * delegating into `methodProduct` and `adtRenderer`, which themselves
  * already render fully-resolved C# code via the converter family.
  * Adopting TextTree at the envelope keeps the renderer family on the
  * typed protocol while avoiding gratuitous churn in the per-section
  * helpers — the byte-parity contract is preserved by `verifyGoldens`.
  */
final class DomainCSServiceRenderer(ctx: DomainCSContext, adtRenderer: DomainCSAdtRenderer) {

  private val methodProduct = new DomainCSServiceMethodProduct(ctx, adtRenderer)

  private var spliceJsonNet: Boolean = false

  // -- Service -------------------------------------------------------------

  def renderService(i: NewTypeDef.Service, im: CSharpImports): ServiceProduct =
    renderService(i, im, withJsonNet = false)

  def renderService(i: NewTypeDef.Service, im: CSharpImports, withJsonNet: Boolean): ServiceProduct = {
    implicit val _domain: Domain    = ctx.domain
    implicit val _im: CSharpImports = im
    val resolver                    = new DomainCSTypeResolver()
    spliceJsonNet = withJsonNet

    val usings     = renderServiceUsings(i)
    val models     = renderServiceModels(i)
    val client     = renderServiceClient(i)
    val dispatcher = renderServiceDispatcher(i)
    val serverBase = renderServiceServerBase(i)

    val tree: TextTree[CSRefHandle] =
      q"""$usings
         |
         |public static class ${i.id.name} {
         |${models.shift(4)}
         |}
         |
         |// ============== Service Client ==============
         |$client
         |
         |// ============== Service Dispatcher ==============
         |$dispatcher
         |
         |// ============== Service Server Base ==============
         |$serverBase
         |         """.stripMargin

    val baseImports = List("IRT", "IRT.Marshaller", "IRT.Transport.Client", "System", "System.Collections", "System.Collections.Generic")
    val extraImports =
      if (withJsonNet)
        (izumi.idealingua.translator.tocsharp.domain.extensions.DomainCSJsonNetExtension.importsDto ++
         izumi.idealingua.translator.tocsharp.domain.extensions.DomainCSJsonNetExtension.importsAdt).distinct
      else List.empty
    ServiceProduct(
      tree.mapRender(resolver.resolve),
      im.renderImports(baseImports ++ extraImports),
    )
  }

  private def renderServiceUsings(i: NewTypeDef.Service)(implicit imports: CSharpImports, domain: Domain): String =
    i.methods.flatMap(me => renderServiceMethodAdtUsings(me)).distinct.mkString("\n")

  private def renderServiceModels(i: NewTypeDef.Service)(implicit imports: CSharpImports, domain: Domain): String =
    i.methods.map(me => renderServiceMethodModels(i, me)).mkString("\n")

  private def renderServiceMethodModels(i: NewTypeDef.Service, method: DefMethod)(implicit imports: CSharpImports, domain: Domain): String = method match {
    case m: DefMethod.RPCMethod =>
      s"""${if (m.signature.input.fields.isEmpty) "" else methodProduct.renderServiceMethodInModel(DTOId(i.id, s"In${m.name.capitalize}"), m.signature.input, spliceJsonNet)}
         |${methodProduct.renderServiceMethodOutModel(i.id, s"Out${m.name.capitalize}", m.signature.output, spliceJsonNet)}
       """.stripMargin
  }

  private def renderServiceClient(i: NewTypeDef.Service)(implicit imports: CSharpImports, domain: Domain): String = {
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

  private def renderServiceDispatcher(i: NewTypeDef.Service)(implicit imports: CSharpImports, domain: Domain): String = {
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

  private def renderServiceServerBase(i: NewTypeDef.Service)(implicit imports: CSharpImports, domain: Domain): String = {
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

  def renderBuzzer(i: NewTypeDef.Buzzer, im: CSharpImports): BuzzerProduct =
    renderBuzzer(i, im, withJsonNet = false)

  def renderBuzzer(i: NewTypeDef.Buzzer, im: CSharpImports, withJsonNet: Boolean): BuzzerProduct = {
    implicit val _domain: Domain    = ctx.domain
    implicit val _im: CSharpImports = im
    val resolver                    = new DomainCSTypeResolver()
    spliceJsonNet = withJsonNet

    val usings     = renderBuzzerUsings(i)
    val models     = renderBuzzerModels(i)
    val client     = renderBuzzerClient(i)
    val dispatcher = renderBuzzerDispatcher(i)
    val handlers   = renderBuzzerHandlersDummy(i)

    val tree: TextTree[CSRefHandle] =
      q"""$usings
         |
         |public static class ${i.id.name} {
         |${models.shift(4)}
         |}
         |
         |// ============== Client ==============
         |$client
         |
         |// ============== Dispatcher ==============
         |$dispatcher
         |
         |// ============== Buzzer Handlers Base ==============
         |$handlers
         |         """.stripMargin

    val baseImports = List("IRT", "IRT.Marshaller", "IRT.Transport.Client", "System", "System.Collections", "System.Collections.Generic")
    val extraImports =
      if (withJsonNet)
        (izumi.idealingua.translator.tocsharp.domain.extensions.DomainCSJsonNetExtension.importsDto ++
         izumi.idealingua.translator.tocsharp.domain.extensions.DomainCSJsonNetExtension.importsAdt).distinct
      else List.empty
    BuzzerProduct(
      tree.mapRender(resolver.resolve),
      im.renderImports(baseImports ++ extraImports),
    )
  }

  private def renderBuzzerUsings(i: NewTypeDef.Buzzer)(implicit imports: CSharpImports, domain: Domain): String =
    i.events.flatMap(me => renderServiceMethodAdtUsings(me)).distinct.mkString("\n")

  private def renderBuzzerModels(i: NewTypeDef.Buzzer)(implicit imports: CSharpImports, domain: Domain): String =
    i.events.map(me => renderBuzzerMethodModels(i, me)).mkString("\n")

  private def renderBuzzerMethodModels(i: NewTypeDef.Buzzer, method: DefMethod)(implicit imports: CSharpImports, domain: Domain): String = method match {
    case m: DefMethod.RPCMethod =>
      s"""${if (m.signature.input.fields.isEmpty) "" else methodProduct.renderServiceMethodInModel(DTOId(i.id, s"In${m.name.capitalize}"), m.signature.input, spliceJsonNet)}
         |${methodProduct.renderBuzzerMethodOutModel(i.id, s"Out${m.name.capitalize}", m.signature.output, spliceJsonNet)}
       """.stripMargin
  }

  private def renderBuzzerClient(i: NewTypeDef.Buzzer)(implicit imports: CSharpImports, domain: Domain): String = {
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

  private def renderBuzzerDispatcher(i: NewTypeDef.Buzzer)(implicit imports: CSharpImports, domain: Domain): String = {
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

  private def renderBuzzerHandlersDummy(i: NewTypeDef.Buzzer)(implicit imports: CSharpImports, domain: Domain): String = {
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

  private def renderServiceMethodAdtUsings(method: DefMethod)(implicit imports: CSharpImports, domain: Domain): List[String] = method match {
    case m: DefMethod.RPCMethod =>
      m.signature.output match {
        case al: Algebraic => al.alternatives.map(adtm => adtRenderer.renderAdtUsings(adtm))
        case _             => List.empty
      }
  }

}
