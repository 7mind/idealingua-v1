package izumi.idealingua.translator.totypescript.domain

import izumi.fundamentals.platform.strings.IzString.*
import izumi.idealingua.model.il.ast.typed.DefMethod
import izumi.idealingua.model.publishing.manifests.TypeScriptProjectLayout
import izumi.idealingua.translator.totypescript.products.CogenProduct.{BuzzerProduct, ServiceProduct}
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}

import izumi.idealingua.model.common.Package

/** Renders a new-IR `TypeDef.Service` / `TypeDef.Buzzer` as the same
  * `ServiceProduct` / `BuzzerProduct` the legacy
  * `TypeScriptTranslator.renderService` / `renderBuzzer` produces (modulo
  * the extension chain).
  *
  * IMPL-7b Phase B M3: byte-parity port. Emits the four-block service
  * shape:
  *   - Models block: `In<Method>` / `Out<Method>` classes + Serialized
  *     interfaces (one pair per method).
  *   - Client block: `I<Name>Client` interface + `<Name>Client` class with
  *     `_transport: ClientTransport` and per-method send/promise wrappers.
  *   - Dispatcher block: `I<Name>Server<C>` interface + `<Name>Dispatcher<C, D>`
  *     class with `ServiceDispatcher` mixin and `dispatch` switch over
  *     `<methodName>`.
  *   - Server block: `<Name>Server<C, D>` abstract class extending
  *     `<Name>Dispatcher` with dummy method implementations.
  *
  * Buzzer parallel emits the same shape but routed through
  * `ServerSocketTransport` (vs `ClientTransport`) and
  * `<Name>BuzzerHandlers` (vs `<Name>Server`). Per F16 absorption, the
  * services + buzzers live in `domain.userTypes` (new IR consolidation).
  *
  * `Typespace` threading per-call mirrors M2 — the converter
  * (`TypeScriptTypeConverter`) and imports (`TypeScriptImports.apply`) both
  * walk the legacy typespace; M3 keeps that plumbing.
  *
  * Extension chain: legacy `TypeScriptTranslator.renderService` / `renderBuzzer`
  * do NOT thread `ext.extend` — `TypeScriptTranslatorExtension` has no
  * `handleService` / `handleBuzzer` overrides, so the pre-extension product
  * equals the post-extension product.
  */
final class DomainTSServiceRenderer(ctx: DomainTSContext, adtRenderer: DomainTSAdtRenderer) {

  import ctx._

  private val methodProduct = new DomainTSServiceMethodProduct(ctx, adtRenderer)

  // -- Service -------------------------------------------------------------

  def renderService(i: NewTypeDef.Service): ServiceProduct = {
    val imports  = DomainTSImports.forService(i, i.id.domain.toPackage, ctx.domain, manifest)
    val typeName = i.id.name

    val svc =
      s"""// Models
         |${renderServiceModels(i)}
         |
         |// Client
         |${renderServiceClient(i)}
         |
         |// Dispatcher
         |${renderServiceDispatcher(i)}
         |
         |// Base Server
         |${renderServiceServer(i)}
         """.stripMargin

    val header =
      s"""${imports.render}
         |${importFromIRT(
          List("ServiceDispatcher", "Marshaller", "Void", "IncomingData", "OutgoingData", "ClientTransport", "Either", "Left as EitherLeft", "Right as EitherRight"),
          i.id.domain.toPackage,
        )}
         """.stripMargin

    ServiceProduct(svc, header, s"// $typeName client")
  }

  private def renderServiceModels(i: NewTypeDef.Service): String =
    i.methods.map(me => methodProduct.renderRPCMethodModels(me)).mkString("\n")

  private def renderServiceClient(i: NewTypeDef.Service): String = {
    s"""export interface I${i.id.name}Client {
       |${i.methods.map(me => methodProduct.renderRPCMethodSignature(me, spread = true)).mkString("\n").shift(4)}
       |}
       |
       |export class ${i.id.name}Client implements I${i.id.name}Client {
       |${renderRuntimeNamesForService(i.id, s"${i.id.name}Client").shift(4)}
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
       |${i.methods.map(me => methodProduct.renderRPCClientMethod(i.id.name, me)).mkString("\n").shift(4)}
       |}
     """.stripMargin
  }

  private def renderServiceDispatcher(i: NewTypeDef.Service): String = {
    s"""export interface I${i.id.name}Server<C> {
       |${i.methods.map(me => methodProduct.renderRPCMethodSignature(me, spread = true, forClient = false)).mkString("\n").shift(4)}
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
       |${i.methods.map(m => methodProduct.renderServiceDispatcherHandler(m, "server")).mkString("\n").shift(12)}
       |            default:
       |                throw new Error(`Method $${method} is not supported by ${i.id.name}Dispatcher.`);
       |        }
       |    }
       |}
     """
  }

  private def renderServiceServer(i: NewTypeDef.Service): String = {
    val name = s"${i.id.name}Server"
    s"""export abstract class $name<C, D> extends ${i.id.name}Dispatcher<C, D> implements I${i.id.name}Server<C> {
       |    constructor(marshaller: Marshaller<D>) {
       |        super(marshaller, null);
       |        this.server = this;
       |    }
       |
       |${i.methods.map(m => renderServiceServerDummyMethod(m)).mkString("\n").shift(4)}
       |}
     """.stripMargin
  }

  private def renderServiceServerDummyMethod(member: DefMethod): String = {
    s"""public ${methodProduct.renderRPCMethodSignature(member, spread = true, forClient = false)} {
       |    throw new Error('Not implemented.');
       |}
     """.stripMargin
  }

  // -- Buzzer --------------------------------------------------------------

  def renderBuzzer(i: NewTypeDef.Buzzer): BuzzerProduct = {
    val imports  = DomainTSImports.forBuzzer(i, i.id.domain.toPackage, ctx.domain, manifest)
    val typeName = i.id.name

    val svc =
      s"""// Models
         |${renderBuzzerModels(i)}
         |
         |// Client
         |${renderBuzzerClient(i)}
         |
         |// Dispatcher
         |${renderBuzzerDispatcher(i)}
         |
         |// Buzzer Handlers Base
         |${renderBuzzerBase(i)}
         """.stripMargin

    val header =
      s"""${imports.render}
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

    BuzzerProduct(svc, header, s"// $typeName")
  }

  private def renderBuzzerModels(i: NewTypeDef.Buzzer): String =
    i.events.map(me => methodProduct.renderRPCMethodModels(me)).mkString("\n")

  private def renderBuzzerClient(i: NewTypeDef.Buzzer): String = {
    s"""export interface I${i.id.name}Client {
       |${i.events.map(me => methodProduct.renderRPCMethodSignature(me, spread = true)).mkString("\n").shift(4)}
       |}
       |
       |export class ${i.id.name}Client implements I${i.id.name}Client {
       |${renderRuntimeNamesForBuzzer(i.id, s"${i.id.name}Client").shift(4)}
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
       |${i.events.map(me => methodProduct.renderRPCClientMethod(i.id.name, me)).mkString("\n").shift(4)}
       |}
     """.stripMargin
  }

  private def renderBuzzerDispatcher(i: NewTypeDef.Buzzer): String = {
    s"""export interface I${i.id.name}BuzzerHandlers<C> {
       |${i.events.map(me => methodProduct.renderRPCMethodSignature(me, spread = true, forClient = false)).mkString("\n").shift(4)}
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
       |${i.events.map(m => methodProduct.renderServiceDispatcherHandler(m, "handlers", useRawMarshaller = true)).mkString("\n").shift(12)}
       |            default:
       |                throw new Error(`Method $${method} is not supported by ${i.id.name}Dispatcher.`);
       |        }
       |    }
       |}
     """
  }

  private def renderBuzzerBase(i: NewTypeDef.Buzzer): String = {
    val name = s"${i.id.name}BuzzerHandlers"
    s"""export abstract class $name<C, D> extends ${i.id.name}Dispatcher<C, D> implements I${i.id.name}BuzzerHandlers<C> {
       |    constructor(marshaller: Marshaller<D>) {
       |        super(marshaller, null);
       |        this.handlers = this;
       |    }
       |
       |${i.events.map(m => renderBuzzerHandlerDummyMethod(m)).mkString("\n").shift(4)}
       |}
     """.stripMargin
  }

  private def renderBuzzerHandlerDummyMethod(member: DefMethod): String = {
    s"""public ${methodProduct.renderRPCMethodSignature(member, spread = true, forClient = false)} {
       |    throw new Error('Not implemented.');
       |}
     """.stripMargin
  }

  // -- Shared helpers ------------------------------------------------------

  /** Mirror of legacy `renderRuntimeNames(s: ServiceId, holderName: String)`
    * (`TypeScriptTranslator.scala:104-115`). */
  private def renderRuntimeNamesForService(s: izumi.idealingua.model.common.TypeId.ServiceId, holderName: String): String = {
    val pkg = s.domain.toPackage.mkString(".")
    s"""// Runtime identification methods
       |public static readonly PackageName = '$pkg';
       |public static readonly ClassName = '${s.name}';
       |public static readonly FullClassName = '$pkg.${s.name}';
       |
       |public getPackageName(): string { return ${if (holderName == null) s.name else holderName}.PackageName; }
       |public getClassName(): string { return ${if (holderName == null) s.name else holderName}.ClassName; }
       |public getFullClassName(): string { return ${if (holderName == null) s.name else holderName}.FullClassName; }
       """.stripMargin
  }

  /** Mirror of legacy `renderRuntimeNames(i: BuzzerId, holderName: String)`
    * (`TypeScriptTranslator.scala:117-128`). */
  private def renderRuntimeNamesForBuzzer(i: izumi.idealingua.model.common.TypeId.BuzzerId, holderName: String): String = {
    val pkg = i.domain.toPackage.mkString(".")
    s"""// Runtime identification methods
       |public static readonly PackageName = '$pkg';
       |public static readonly ClassName = '${i.name}';
       |public static readonly FullClassName = '$pkg.${i.name}';
       |
       |public getPackageName(): string { return ${if (holderName == null) i.name else holderName}.PackageName; }
       |public getClassName(): string { return ${if (holderName == null) i.name else holderName}.ClassName; }
       |public getFullClassName(): string { return ${if (holderName == null) i.name else holderName}.FullClassName; }
       """.stripMargin
  }

  /** Mirror of legacy `importFromIRT`
    * (`TypeScriptTranslator.scala:948-959`). */
  private def importFromIRT(names: List[String], pkg: Package): String = {
    var importOffset = ""
    (1 to pkg.length).foreach(_ => importOffset += "../")
    if (manifest.layout == TypeScriptProjectLayout.YARN) {
      importOffset = manifest.yarn.scope + "/"
    }

    s"""import {
       |${names.map(n => s"    $n").mkString(",\n")}
       |} from '${importOffset}irt'
     """.stripMargin
  }
}
