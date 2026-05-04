package izumi.test.domain01



trait GoAliasTest extends Any with izumi.idealingua.runtime.model.IDLGeneratedType { def a: String }

trait GoAliasTestCirce {
  import _root_.io.circe.syntax.*
  import _root_.io.circe.{Encoder, Decoder, DecodingFailure}
  implicit val encodeGoAliasTest: Encoder.AsObject[GoAliasTest] = Encoder.AsObject.instance {
    case v: GoAliasTest.Struct =>
      Map("izumi.test.domain01.GoAliasTest.Struct" -> v).asJsonObject
  }
  implicit val decodeGoAliasTest: Decoder[GoAliasTest] = Decoder.instance(c => {
    val maybeContent = c.keys.flatMap(_.headOption).toRight(DecodingFailure("No type name found in JSON, expected JSON of form { \"type_name\": { ...fields } }", c.history))
    for (fname <- maybeContent; value = c.downField(fname); result <- fname match {
      case "izumi.test.domain01.GoAliasTest.Struct" =>
        value.as[GoAliasTest.Struct]
      case _ =>
        val cname = "izumi.test.domain01.GoAliasTest"
        val alts = List("izumi.test.domain01.GoAliasTest.Struct").mkString(",")
        Left(DecodingFailure(s"Can't decode type $fname as $cname, expected one of [$alts]", value.history))
    }) yield result
  })
}

object GoAliasTest extends GoAliasTestCirce {
  def apply(a: String) = Struct(a)
  final case class Struct(a: String) extends AnyVal with GoAliasTest
  trait StructCirce extends _root_.izumi.idealingua.runtime.circe.IRTTimeInstances {
    import _root_.io.circe.{Encoder, Decoder}
    implicit val encodeStruct: Encoder.AsObject[Struct] = Encoder.forProduct1[Struct, String]("a")((v: Struct) => v.a)
    implicit val decodeStruct: Decoder[Struct] = Decoder.forProduct1[Struct, String]("a")((d: String) => new Struct(d))
  }
  object Struct extends GoAliasTest.StructCirce {
    def apply(goaliastest: GoAliasTest): GoAliasTest.Struct = {
      assert(goaliastest.asInstanceOf[_root_.scala.AnyRef] ne null)
      new GoAliasTest.Struct(a = goaliastest.a)
    }
    implicit object Struct_upcast_Struct extends izumi.idealingua.runtime.IRTCast[GoAliasTest.Struct, GoAliasTest.Struct] {
      override def convert(_value: GoAliasTest.Struct): GoAliasTest.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        GoAliasTest.Struct(a = _value.a)
      }
    }
    implicit object Struct_upcast_GoAliasTest extends izumi.idealingua.runtime.IRTCast[GoAliasTest.Struct, GoAliasTest] {
      override def convert(_value: GoAliasTest.Struct): GoAliasTest = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        GoAliasTest.Struct(a = _value.a)
      }
    }
    implicit class StructExtensions(override protected val _value: GoAliasTest.Struct) extends izumi.idealingua.runtime.IRTConversions[GoAliasTest.Struct]
  }
  implicit object GoAliasTest_downcast_extend_GoAliasTestStruct extends izumi.idealingua.runtime.IRTExtend[GoAliasTest, GoAliasTest.Struct] {
    class Call(private val _value: GoAliasTest) extends AnyVal {
      def using(): GoAliasTest.Struct = {
        assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
        GoAliasTest.Struct(a = _value.a)
      }
    }
    override type INSTANTIATOR = Call
    override def next(_value: GoAliasTest): Call = new Call(_value)
  }
  implicit object GoAliasTest_upcast_GoAliasTest extends izumi.idealingua.runtime.IRTCast[GoAliasTest, GoAliasTest] {
    override def convert(_value: GoAliasTest): GoAliasTest = {
      assert(_value.asInstanceOf[_root_.scala.AnyRef] ne null)
      GoAliasTest.Struct(a = _value.a)
    }
  }
  implicit class GoAliasTestExtensions(override protected val _value: GoAliasTest) extends izumi.idealingua.runtime.IRTConversions[GoAliasTest]
}
       