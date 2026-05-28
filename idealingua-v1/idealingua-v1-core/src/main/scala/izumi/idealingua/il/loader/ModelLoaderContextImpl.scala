package izumi.idealingua.il.loader

import izumi.idealingua.util.Parallel

class ModelLoaderContextImpl(
  makeEnumerator: BaseModelLoadContext => FilesystemEnumerator,
  parallel: Parallel = Parallel.Default,
) extends ModelLoaderContext {
  val domainExt: String = ".domain"

  val modelExt: String = ".model"

  val overlayExt: String = ".overlay"

  val parser: ModelParser = new ModelParserImpl(parallel)

  val enumerator: FilesystemEnumerator = makeEnumerator(this)

  val loader: ModelLoader = new ModelLoaderImpl(enumerator, parser, modelExt, domainExt, overlayExt)
}
