package izumi.idealingua.il.parser

import izumi.idealingua.il.parser.structure._
import izumi.idealingua.model.il.ast.raw.defns.RawMethod
import org.scalatest.wordspec.AnyWordSpec
import fastparse._

class BasicParserTest extends AnyWordSpec with ParserTestTools {

  import ctx._

  "IL parser" should {
    "parse annos" in {
      assertParses(defConst.defAnno(using _), """@TestAnno()""".stripMargin)

      assertParses(defConst.defAnno(using _), """@TestAnno(a=1)""".stripMargin)

      assertParses(defConst.defAnno(using _), """@TestAnno(a=1, b="xxx",c=true,d=false,e=[1,2,"x",],f={a=1,b="str"} ,)""".stripMargin)
      assertParses(defConst.defAnno(using _), """@TestAnno(e=[1,2,"x",],f = ( lst[str]([1,2,3]) ) )""".stripMargin)
      assertParses(defConst.defAnno(using _), """@AnotherAnno(a=1, b="str", c=[1,2,3], d={x=true, y=1}, e=lst[str]([1,2,3]))""".stripMargin)

      assertParses(defConst.defAnno(using _), """@TestAnno(a=1, /*comment*/ b="xxx")""".stripMargin)
    }

    "parse imports" in {
      import defDomain._
      assertParses(importBlock(using _), "import a.b.c")
      assertParses(importBlock(using _), "import     a.b.c")
      assertParses(importBlock(using _), "import a.b.{c, d}")
      assertParses(importBlock(using _), "import a.b.{c, d,}")
      assertParses(importBlock(using _), "import a.b.{c, d ,}")
      assertParses(importBlock(using _), "import a")
    }

    "parse domain declaration" in {
      import defDomain._
      assertParses(domainBlock(using _), "domain x.y.z")
    }

    "parse foreign type interpolations" in {
      import ids._
      assertParses(typeInterp(using _), """t"java.util.Map"""")
      assertParses(typeInterp(using _), """t"java.util.Map<${A}, ${B}>"""")
    }

    "parse aliases" in {
      assertParses(defStructure.aliasBlock(using _), "alias x = y")
    }

    "parse enclosed enums" in {
      assertParses(defStructure.enumBlock(using _), "enum MyEnum {X Y Zz}")
      assertParses(defStructure.enumBlock(using _), "enum MyEnum { X Y Z }")
      assertParses(defStructure.enumBlock(using _), "enum MyEnum {  X  Y  Z  }")
      assertParses(
        defStructure.enumBlock(using _),
        """enum MyEnum {
          | + BaseEnum
          | - REMOVED_BASE_ELEMENT
          | NEW_ELEMENT
          |}""".stripMargin,
      )
      assertParses(
        defStructure.enumBlock(using _),
        """enum MyEnum {
          |X
          | Y
          |Z
          |}""".stripMargin,
      )
      assertParses(
        defStructure.enumBlock(using _),
        """enum MyEnum {
          |  ELEMENT1
          |  // comment
          |  ELEMENT2
          |}""".stripMargin,
      )

      assertParses(
        defStructure.enumBlock(using _),
        """enum MyEnum {
          |  ELEMENT1
          |  // comment
          |  /* comment 2*/
          |  ELEMENT2
          |}""".stripMargin,
      )
      assertParses(
        defStructure.enumBlock(using _),
        """enum MyEnum {
          |  ELEMENT1 // comment 3
          |  // comment
          |  /* comment 2*/
          |  ELEMENT2
          |}""".stripMargin,
      )
      assertParses(defStructure.enumBlock(using _), "enum MyEnum {X,Y,Z}")
      assertParses(defStructure.enumBlock(using _), "enum MyEnum {X|Y|Z}")
      assertParses(defStructure.enumBlock(using _), "enum MyEnum { X|Y|Z }")
      assertParses(defStructure.enumBlock(using _), "enum MyEnum {X | Y | Z}")
      assertParses(defStructure.enumBlock(using _), "enum MyEnum { X | Y | Z }")
      assertParses(defStructure.enumBlock(using _), "enum MyEnum { X , Y , Z }")
      assertParses(defStructure.enumBlock(using _), "enum MyEnum { X , Y , Z , F }")

    }

    "parse free-form enums" in {
      assertParses(defStructure.enumBlock(using _), "enum MyEnum = X | Y | Z")
      assertParses(defStructure.enumBlock(using _), "enum MyEnum = X | /**/ Y | Z")
      assertParses(
        defStructure.enumBlock(using _),
        """enum MyEnum = X
          ||Y
          || Z""".stripMargin,
      )
      assertParses(
        defStructure.enumBlock(using _),
        """enum MyEnum =
          || X
          | | Y
          || Z""".stripMargin,
      )

    }

    "parse empty blocks" in {
      assertParses(defStructure.mixinBlock(using _), "mixin Mixin {}")
      assertParses(defStructure.idBlock(using _), "id Id {}")
      assertParses(defService.serviceBlock(using _), "service Service {}")
      assertParses(defStructure.dtoBlock(using _), "data Data {}")
    }

    "parse dto blocks" in {
      assertParses(
        defStructure.dtoBlock(using _),
        """data Data {
          |& Add
          |&&& Add
          |+ Embed
          |+++ Embed
          |- Remove
          |--- Remove
          |field: F
          |... Embed
          |another: F
          |}""".stripMargin,
      )
    }

    "parse complex comments" in {
      assertParses(
        sep.any(using _),
        """// test
          |/*test*/
          | /* test/**/*/
        """.stripMargin,
      )
      assertParses(comments.ShortComment(using _), "// test\n")
      assertParses(comments.ShortComment(using _), "//\n")
      assertParses(sep.any(using _), "//\n")
      assertParses(sep.any(using _), "// test\n")
    }

    "parse complex comments -2" in {
      assertParses(defStructure.sepEnum(using _), " ")
      assertParses(defStructure.sepEnum(using _), "//comment\n")
      assertParses(defStructure.sepEnum(using _), " //comment\n")
      assertParses(defStructure.sepEnum(using _), "  //comment\n  ")
      assertParses(defStructure.sepEnum(using _), "  //comment0\n  //comment1\n  ")
      assertParses(defStructure.sepEnum(using _), "  //comment0\n  //comment1\n")
    }

    "parse service defintions" in {
      assertParses(defStructure.inlineStruct(using _), "(a: A, b: B, + C)")
      assertParses(defStructure.inlineStruct(using _), "(a: str)")
      assertParses(defStructure.inlineStruct(using _), "(+ A)")
      assertParses(defStructure.adtOut(using _), "( A \n | \n B )")
      assertParses(defStructure.adtOut(using _), "(A|B)")
      assertParses(defStructure.adtOut(using _), "(A | B)")
      assertParses(defStructure.inlineStruct(using _), "(\n  firstName: str \n , \n secondName: str\n)")
    }

    "parse identifiers" in {
      assertParses(ids.domainId(using _), "x.y.z")
      assertParses(ids.identifier(using _), "x.y#z")
    }

    "parse fields" in {
      assertParses(defStructure.field(using _), "a: str")
      assertParses(defStructure.field(using _), "a: domain#Type")
      assertParses(defStructure.field(using _), "a: map[str, str]")
      assertParses(defStructure.field(using _), "a: map[str, set[domain#Type]]")
    }

    "parse adt members" in {
      assertParses(defStructure.adtMember(using _), "X")
      assertParses(defStructure.adtMember(using _), "X as T")

      assertParses(defStructure.adtMember(using _), "a.b.c#X")
      assertParses(defStructure.adtMember(using _), "a.b.c#X as T")
    }

    "parse adt blocks" in {
      assertParses(defStructure.adtBlock(using _), "adt MyAdt { X as XXX | Y }")
      assertParses(defStructure.adtBlock(using _), "adt MyAdt { X | Y | a.b.c#D as B }")
      assertParses(defStructure.adtBlock(using _), "adt MyAdt = X | Y | Z")
      assertParses(defStructure.adtBlock(using _), "adt MyAdt { X }")
      assertParses(defStructure.adtBlock(using _), "adt MyAdt {a.b.c#D}")
      assertParses(defStructure.adtBlock(using _), "adt MyAdt { a.b.c#D }")
      assertParses(defStructure.adtBlock(using _), "adt MyAdt { a.b.c#D  Z  Z }")
      assertParses(defStructure.adtBlock(using _), "adt MyAdt { X Y a.b.c#D Z }")
      assertParses(defStructure.adtBlock(using _), "adt MyAdt { X Y a.b.c#D }")
      assertParses(
        defStructure.adtBlock(using _),
        """adt MyAdt {
          | X
          |Y
          | a.b.c#D
          |}""".stripMargin,
      )
    }

    "parse service definition" in {
      def defm[$: P]: P[RawMethod.RPCMethod] = defSignature.method(kw.defm)

      assertParses(defm(using _), "def greetAlgebraicOut(firstName: str, secondName: str) => ( SuccessData | ErrorData )")
      assertParseableCompletely(
        defService.methods(using _),
        """def greetAlgebraicOut(firstName: str, secondName: str) => ( SuccessData | ErrorData )
          |def greetAlgebraicOut(firstName: str, secondName: str) => ( SuccessData | ErrorData )""".stripMargin,
      )

      assertParseableCompletely(
        defService.serviceBlock(using _),
        """service FileService {
          |  def greetAlgebraicOut(firstName: str, secondName: str) => ( SuccessData | ErrorData )
          |}""".stripMargin,
      )
    }

  }
}
