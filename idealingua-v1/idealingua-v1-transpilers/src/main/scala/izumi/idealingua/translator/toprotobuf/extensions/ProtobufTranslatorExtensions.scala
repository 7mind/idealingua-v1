package izumi.idealingua.translator.toprotobuf.extensions

import izumi.idealingua.translator.toprotobuf.PBTContext

class ProtobufTranslatorExtensions(ctx: PBTContext, extensions: Seq[ProtobufTranslatorExtension]) {
  def extend[S, P]
  (
    source: S
    , entity: P
    , entityTransformer: ProtobufTranslatorExtension => (PBTContext, S, P) => P
  ): P = {
    extensions.foldLeft(entity) {
      case (acc, v) =>
        entityTransformer(v)(ctx, source, acc)
    }
  }
}
