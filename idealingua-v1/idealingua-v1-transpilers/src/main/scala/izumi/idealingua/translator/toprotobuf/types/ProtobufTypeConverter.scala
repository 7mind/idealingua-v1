package izumi.idealingua.translator.toprotobuf.types

import izumi.idealingua.model.common.{DomainId, Generic, Primitive, PrimitiveId, TimeTypeId, TypeId}
import izumi.idealingua.model.il.ast.typed
import izumi.idealingua.model.problems.IDLException

class ProtobufTypeConverter(domain: DomainId) {

  def toProtobuf(fields: typed.IdTuple)(implicit dummyImplicit: DummyImplicit): List[ProtobufField] = {
    fields.map {
      field =>
        ProtobufField(field.name, toProtobuf(field.typeId))
    }
  }

  def toProtobuf(fields: typed.Tuple): List[ProtobufField] = {
    fields.map {
      field =>
        ProtobufField(field.name, toProtobuf(field.typeId))
    }
  }

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

  private def checkOnNestedGenerics(t: TypeId): Unit = {
    t match {
      case _: Generic => throw new IDLException(s"[$domain] Protobuf does not support nested Generic parameters. Parameter: ${t.path}#${t.name}")
      case _ => ()
    }
  }

  private def checkMapKeyTypes(t: TypeId): Unit = {
    t match {
      case t: Generic =>
        throw new IDLException(s"[$domain] Protobuf does not support nested Generic parameters. Parameter: ${t.path}#${t.name}")

      case t: Primitive =>
        t match {
          case Primitive.TFloat | Primitive.TDouble | Primitive.TBLOB =>
            throw new IDLException(s"[$domain] Protobuf does not support nested float/double/bytes as map key parameters. Parameter: ${t.path}#${t.name}")

          case Primitive.TUUID =>
            throw new IDLException(s"[$domain] Protobuf does not support UUIDs as map key parameters. Parameter: ${t.path}#${t.name}")

          case _: TimeTypeId =>
            throw new IDLException(s"[$domain] Protobuf does not support Time units as map key parameters. Parameter: ${t.path}#${t.name}")

          case _: PrimitiveId =>
        }

      case _ =>
        throw new IDLException(s"[$domain] Protobuf does not support custom types as map key parameters. Parameter: ${t.path}#${t.name}")

    }
  }

  private def toGeneric(typeId: Generic): ProtobufType = {
    typeId match {
      case s: Generic.TSet =>
        checkOnNestedGenerics(s.valueType)
        val arg = toProtobuf(s.valueType)
        ProtobufType(Seq.empty, s"repeated ${arg.fullName}", List(arg), None)
      case l: Generic.TList =>
        checkOnNestedGenerics(l.valueType)
        val arg = toProtobuf(l.valueType)
        ProtobufType(Seq.empty, s"repeated ${arg.fullName}", List(arg), None)
      case m: Generic.TMap =>
        checkMapKeyTypes(m.keyType)
        checkOnNestedGenerics(m.valueType)
        val arg1 = toProtobuf(m.keyType)
        val arg2 = toProtobuf(m.valueType)
        ProtobufType(Seq.empty, s"map<${arg1.fullName}, ${arg2.fullName}>", List(arg1, arg2), None)
      case o: Generic.TOption =>
        val arg = toProtobuf(o.valueType)
        o.valueType match {
          case _: Generic => ProtobufType(Seq.empty, toProtobuf(o.valueType).fullName, List(arg), optional = None)
          case _ => ProtobufType(Seq.empty, arg.fullName, List(arg), optional = Some(true))
        }
    }
  }

  private def toPrimitive(value: Primitive): ProtobufType = {
    value match {
      case Primitive.TBool =>
        ProtobufType(Seq.empty, "bool")
      case Primitive.TString =>
        ProtobufType(Seq.empty, "string")

      case Primitive.TInt32 | Primitive.TInt16 | Primitive.TInt8 =>
        ProtobufType(Seq.empty, "int32")
      case Primitive.TUInt32 | Primitive.TUInt16 | Primitive.TUInt8 =>
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
        ProtobufType(Seq("idl", "types"), "PUUID")
      case Primitive.TTsTz | Primitive.TTsO | Primitive.TTsU | Primitive.TTs | Primitive.TTime | Primitive.TDate =>
        ProtobufType(Seq("idl", "types"), "PTimestamp")
    }
  }
}
