package izumi.idealingua.il.loader

class ModelLoaderContextImpl(makeEnumerator: BaseModelLoadContext => FilesystemEnumerator) extends ModelLoaderContext {
  val domainExt: String = ".domain"

  val modelExt: String = ".model"

  val overlayExt: String = ".overlay"

  val parser: ModelParser = new ModelParserImpl()

  val enumerator: FilesystemEnumerator = makeEnumerator(this)

  val loader: ModelLoader = new ModelLoaderImpl(enumerator, parser, modelExt, domainExt, overlayExt)
}
