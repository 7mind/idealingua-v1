package izumi.idealingua.compiler

import io.circe.syntax.*
import io.circe.parser.decode
import izumi.idealingua.model.publishing.manifests.*
import org.scalatest.wordspec.AnyWordSpec

class PlatformEnumCodecsTest extends AnyWordSpec with Codecs {

  "PlatformEnumCodecs" should {
    "encode ScalaProjectLayout as string" in {
      assert((ScalaProjectLayout.SBT: ScalaProjectLayout).asJson.noSpaces == """"SBT"""")
      assert((ScalaProjectLayout.PLAIN: ScalaProjectLayout).asJson.noSpaces == """"PLAIN"""")
    }

    "decode ScalaProjectLayout from string" in {
      assert(decode[ScalaProjectLayout](""""SBT"""") == Right(ScalaProjectLayout.SBT))
      assert(decode[ScalaProjectLayout](""""PLAIN"""") == Right(ScalaProjectLayout.PLAIN))
    }

    "encode TypeScriptProjectLayout as string" in {
      assert((TypeScriptProjectLayout.YARN: TypeScriptProjectLayout).asJson.noSpaces == """"YARN"""")
      assert((TypeScriptProjectLayout.PLAIN: TypeScriptProjectLayout).asJson.noSpaces == """"PLAIN"""")
    }

    "decode TypeScriptProjectLayout from string" in {
      assert(decode[TypeScriptProjectLayout](""""YARN"""") == Right(TypeScriptProjectLayout.YARN))
      assert(decode[TypeScriptProjectLayout](""""PLAIN"""") == Right(TypeScriptProjectLayout.PLAIN))
    }

    "encode GoProjectLayout as string" in {
      assert((GoProjectLayout.REPOSITORY: GoProjectLayout).asJson.noSpaces == """"REPOSITORY"""")
      assert((GoProjectLayout.PLAIN: GoProjectLayout).asJson.noSpaces == """"PLAIN"""")
    }

    "decode GoProjectLayout from string" in {
      assert(decode[GoProjectLayout](""""REPOSITORY"""") == Right(GoProjectLayout.REPOSITORY))
      assert(decode[GoProjectLayout](""""PLAIN"""") == Right(GoProjectLayout.PLAIN))
    }

    "encode CSharpProjectLayout as string" in {
      assert((CSharpProjectLayout.NUGET: CSharpProjectLayout).asJson.noSpaces == """"NUGET"""")
      assert((CSharpProjectLayout.PLAIN: CSharpProjectLayout).asJson.noSpaces == """"PLAIN"""")
    }

    "decode CSharpProjectLayout from string" in {
      assert(decode[CSharpProjectLayout](""""NUGET"""") == Right(CSharpProjectLayout.NUGET))
      assert(decode[CSharpProjectLayout](""""PLAIN"""") == Right(CSharpProjectLayout.PLAIN))
    }

    "roundtrip all layout enums" in {
      def roundtrip[T: io.circe.Encoder: io.circe.Decoder](value: T): Unit = {
        val json = value.asJson.noSpaces
        assert(decode[T](json) == Right(value), s"roundtrip failed for $value: encoded as $json")
      }

      roundtrip[ScalaProjectLayout](ScalaProjectLayout.SBT)
      roundtrip[ScalaProjectLayout](ScalaProjectLayout.PLAIN)
      roundtrip[TypeScriptProjectLayout](TypeScriptProjectLayout.YARN)
      roundtrip[TypeScriptProjectLayout](TypeScriptProjectLayout.PLAIN)
      roundtrip[GoProjectLayout](GoProjectLayout.REPOSITORY)
      roundtrip[GoProjectLayout](GoProjectLayout.PLAIN)
      roundtrip[CSharpProjectLayout](CSharpProjectLayout.NUGET)
      roundtrip[CSharpProjectLayout](CSharpProjectLayout.PLAIN)
    }
  }
}
