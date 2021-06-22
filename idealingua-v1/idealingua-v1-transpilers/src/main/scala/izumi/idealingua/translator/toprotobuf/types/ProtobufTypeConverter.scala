package izumi.idealingua.translator.toprotobuf.types

import izumi.idealingua.model.common.{DomainId, Generic, Primitive, TypeId}

class ProtobufTypeConverter(domain: DomainId) {
  def toProtobuf(id: TypeId): ProtobufType = {
    id match {
      case t: Generic =>
        toGeneric(t)

      case t: Primitive =>
        toPrimitive(t)

      case _ =>
        ProtobufType(id.path.toPackage, id.name)
    }
  }

  private def toGeneric(typeId: Generic): ProtobufType = {
    typeId match {
      case s: Generic.TSet =>
        ProtobufType(Seq.empty, s"repeated ${s.valueType.name}")
      case l: Generic.TList =>
        ProtobufType(Seq.empty, s"repeated ${l.valueType.name}")
      case m: Generic.TMap =>
        ProtobufType(Seq.empty, s"map<${m.keyType.name}, ${m.valueType.name}>")
      case o: Generic.TOption =>
        ProtobufType(Seq.empty, o.valueType.name, optional = true)
    }
  }

  private def toPrimitive(value: Primitive): ProtobufType = {
    value match {
      case Primitive.TBool =>
        ProtobufType(Seq.empty, "bool")
      case Primitive.TString =>
        ProtobufType(Seq.empty, "string")

      case Primitive.TInt32 | Primitive.TInt16 | Primitive.TInt8=>
        ProtobufType(Seq.empty, "int32")
      case Primitive.TUInt32 | Primitive.TUInt16 | Primitive.TUInt8=>
        ProtobufType(Seq.empty, "uint32")

      case Primitive.TInt64 =>
        ProtobufType(Seq.empty, "int64")
      case Primitive.TUInt64 =>
        ProtobufType(Seq.empty, "uint64")

      case Primitive.TFloat =>
        ProtobufType(Seq.empty, "float")
      case Primitive.TDouble =>
        ProtobufType(Seq.empty, "double")
      case Primitive.TBLOB =>
        ProtobufType(Seq.empty, "repeated int32")

      case Primitive.TUUID =>
        ProtobufType(Seq.empty, "string")

//todo: add uuid proto
//      case Primitive.TUUID =>
//        ProtobufType(Seq("idealingua", "protobuf", "uuid.proto"), "timestamp")

      case Primitive.TTsTz | Primitive.TTsU | Primitive.TTs |  Primitive.TTime |  Primitive.TDate=>
        ProtobufType(Seq("google", "protobuf", "timestamp.proto"), "timestamp")
    }
  }
}
