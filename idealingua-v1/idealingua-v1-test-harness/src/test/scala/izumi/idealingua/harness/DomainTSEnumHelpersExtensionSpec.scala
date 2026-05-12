package izumi.idealingua.harness

import izumi.idealingua.model.common.TypeId.EnumId
import izumi.idealingua.model.common.{DomainId, TypePath}
import izumi.idealingua.model.il.ast.typed.{EnumMember, NodeMeta, TypeDef => LegacyTypeDef}
import izumi.idealingua.translator.totypescript.domain.extensions.DomainTSEnumHelpersExtension
import izumi.idealingua.translator.totypescript.extensions.EnumHelpersExtension
import izumi.idealingua.translator.totypescript.products.CogenProduct.EnumProduct
import izumi.idealingua.typer.ir.{TypeDef => NewTypeDef}
import org.scalatest.funsuite.AnyFunSuite

/** PR-02 IMPL-7b Phase B M4: byte-parity unit test for
  * `DomainTSEnumHelpersExtension`.
  *
  * Asserts that the Domain-consuming `DomainTSEnumHelpersExtension.handleEnum`
  * produces the same `EnumProduct` extension as the legacy
  * `EnumHelpersExtension.handleEnum`, given matching new-IR / legacy-IR
  * inputs and a fixed pre-extension product.
  *
  * The legacy extension consumes `TSTContext` only to satisfy the trait
  * signature (it doesn't read anything off the context); we pass a `null`
  * `TSTContext` to the legacy mirror because the body only touches the
  * `Enumeration` and `EnumProduct` arguments. If a future legacy change
  * starts reading `ctx`, this test will fail loudly with an NPE — which is
  * what we want.
  */
final class DomainTSEnumHelpersExtensionSpec extends AnyFunSuite {

  private val domainId  = DomainId(Seq("idltest"), "ts_enum_helpers_ext_spec")
  private val typePath  = TypePath(domainId, Seq.empty)
  private val emptyMeta = NodeMeta.empty

  private def assertProductEqual(label: String, expected: EnumProduct, actual: EnumProduct): Unit = {
    val _ = assert(expected.content == actual.content, s"$label: content diverges\nlegacy=${expected.content}\nnew   =${actual.content}")
    val _ = assert(expected.preamble == actual.preamble, s"$label: preamble diverges")
  }

  test("EnumHelpers handleEnum: byte-equal to legacy (single-member)") {
    val enumId  = EnumId(typePath, "Mono")
    val members = List(EnumMember("ONLY", emptyMeta))
    val newEnum    = NewTypeDef.Enum(enumId, members, emptyMeta)
    val legacyEnum = LegacyTypeDef.Enumeration(enumId, members, emptyMeta)
    val basePreamble = "// Mono Enumeration"
    val baseContent = "export enum Mono {\n    ONLY = 'ONLY'\n}\n       "
    val base = EnumProduct(baseContent, basePreamble)

    val actual   = DomainTSEnumHelpersExtension.handleEnum(newEnum, base)
    val expected = EnumHelpersExtension.handleEnum(null, legacyEnum, base)

    assertProductEqual("mono-helpers", expected, actual)
  }

  test("EnumHelpers handleEnum: byte-equal to legacy (multi-member, order-preserving)") {
    val enumId  = EnumId(typePath, "Color")
    val members = List(EnumMember("RED", emptyMeta), EnumMember("GREEN", emptyMeta), EnumMember("BLUE", emptyMeta))
    val newEnum    = NewTypeDef.Enum(enumId, members, emptyMeta)
    val legacyEnum = LegacyTypeDef.Enumeration(enumId, members, emptyMeta)
    val basePreamble = "// Color Enumeration"
    val baseContent = "export enum Color {\n    RED = 'RED',\n    GREEN = 'GREEN',\n    BLUE = 'BLUE'\n}\n       "
    val base = EnumProduct(baseContent, basePreamble)

    val actual   = DomainTSEnumHelpersExtension.handleEnum(newEnum, base)
    val expected = EnumHelpersExtension.handleEnum(null, legacyEnum, base)

    assertProductEqual("rgb-helpers", expected, actual)
  }
}
