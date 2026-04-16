package izumi.idealingua.il.parser.structure

import izumi.idealingua.il.parser.ParserTestTools
import org.scalatest.wordspec.AnyWordSpec

class CommentsAndDocstringsTest extends AnyWordSpec with ParserTestTools {

  "IL parser" should {

    "parse docstrings" in {
      assertParses(
        comments.DocComment(using _),
        """/** docstring
          | */""".stripMargin,
      )

      assertParses(
        comments.DocComment(using _),
        """/** docstring
          |  * docstring
          |  */""".stripMargin,
      )

      assertParses(
        comments.DocComment(using _),
        """/** docstring
          |* docstring
          |*/""".stripMargin,
      )

      assertParses(
        comments.DocComment(using _),
        """/**
          |* docstring
          |*/""".stripMargin,
      )

      assertParses(
        comments.DocComment(using _),
        """/**
          |* docstring
          |*
          |*/""".stripMargin,
      )

      assertParsesInto(
        comments.DocComment(using _),
        """/** docstring
          |  * with *stars*
          |  */""".stripMargin,
        """ docstring
          | with *stars*""".stripMargin,
      )
    }

  }
}
