package izumi.idealingua.translator.tocsharp.domain.extensions

import izumi.fundamentals.platform.strings.IzString._
import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.{Generic, Primitive, StructureId, TypeId}
import izumi.idealingua.model.output.Module
import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.translator.tocsharp.domain.{DomainCSClass, DomainCSStruct, DomainCSharpType}
import izumi.idealingua.translator.tocsharp.tools.ModuleTools
import izumi.idealingua.typer.ir.{Domain, FlatStruct, TypeDef => NewTypeDef}

/** Domain-consuming twin of the legacy `NUnitExtension` (deleted in
  * PR-02 IMPL-10c). Emits NUnit `[TestFixture]` companions exercising the
  * Newtonsoft JSON marshaller round-trip for every Enum / Identifier /
  * Adt / DTO surfaced by `DomainCSharpTranslator`.
  *
  * Each emitted `Module` carries `meta("scope") == "test"` so
  * `CSharpLayouter` routes it into `tests/<domain>/<Type>Tests.cs`.
  *
  * Gated on `options.manifest.enableNUnit`; only invoked by the translator
  * when that flag is true.
  */
object DomainCSNUnitExtension {

  private val modules = new ModuleTools()

  /** A reference is "local" iff every transitive user-type referenced by it
    * resolves inside `domain.userTypes`. The legacy `NUnitExtension`
    * implicitly assumed `Typespace` was a full multi-domain index; the new
    * `Domain` IR is single-domain-scoped, so test scaffolds that reach into
    * a foreign domain produce invalid C# (`null` for value-typed enums,
    * single-arg DTOs whose true field set lives in another domain, etc.).
    * We pre-walk the reference graph here and skip the test emission for
    * any TypeDef that would cross a domain boundary — matching the
    * conservative behaviour required by the Cs2 single-domain converter.
    */
  private def isLocallyResolvable(id: TypeId, domain: Domain, seen: Set[TypeId] = Set.empty): Boolean = {
    if (seen.contains(id)) return true
    val nextSeen = seen + id
    id match {
      case _: Primitive => true
      case g: Generic =>
        g match {
          case gm: Generic.TMap    => isLocallyResolvable(gm.keyType, domain, nextSeen) && isLocallyResolvable(gm.valueType, domain, nextSeen)
          case gl: Generic.TList   => isLocallyResolvable(gl.valueType, domain, nextSeen)
          case gs: Generic.TSet    => isLocallyResolvable(gs.valueType, domain, nextSeen)
          case go: Generic.TOption => isLocallyResolvable(go.valueType, domain, nextSeen)
        }
      case al: AliasId =>
        val target = domain.aliases.getOrElse(al, al)
        if (target == al) domain.userTypes.contains(al) else isLocallyResolvable(target, domain, nextSeen)
      case _: TypeId =>
        domain.userTypes.get(id) match {
          case Some(e: NewTypeDef.Enum)       => true
          case Some(id2: NewTypeDef.Identifier) => id2.fields.forall(f => isLocallyResolvable(f.typeId, domain, nextSeen))
          case Some(a: NewTypeDef.Adt)        => a.alternatives.forall(alt => isLocallyResolvable(alt.typeId, domain, nextSeen))
          case Some(d: NewTypeDef.Dto) =>
            domain.flattenedStructs.get(d.id) match {
              case Some(fs) => fs.fields.forall(ff => isLocallyResolvable(ff.field.typeId, domain, nextSeen))
              case None     => d.struct.fields.forall(f => isLocallyResolvable(f.typeId, domain, nextSeen))
            }
          case Some(i: NewTypeDef.Interface) =>
            domain.flattenedStructs.get(i.id) match {
              case Some(fs) => fs.fields.forall(ff => isLocallyResolvable(ff.field.typeId, domain, nextSeen))
              case None     => i.struct.fields.forall(f => isLocallyResolvable(f.typeId, domain, nextSeen))
            }
          case _ => false
        }
    }
  }

  def postEnum(e: NewTypeDef.Enum)(implicit im: CSharpImports, domain: Domain): Seq[Module] = {
    if (!isLocallyResolvable(e.id, domain)) return Seq.empty
    val name       = e.id.name
    val testMember = e.members.head.value
    val code =
      s"""public static class ${name}TestHelper {
         |    public static $name Create() {
         |        return $name.$testMember;
         |    }
         |}
         |
         |[TestFixture]
         |public class ${name}_ShouldSerialize {
         |    IJsonMarshaller marshaller;
         |    public ${name}_ShouldSerialize() {
         |        marshaller = new JsonNetMarshaller();
         |    }
         |
         |    [Test]
         |    public void Serialize() {
         |        var v = ${name}TestHelper.Create();
         |        var json = marshaller.Marshal<$name>(v);
         |        Assert.AreEqual("\\"$testMember\\"", json);
         |    }
         |
         |    [Test]
         |    public void Deserialize() {
         |        var v = marshaller.Unmarshal<$name>("\\"$testMember\\"");
         |        Assert.AreEqual(v, $name.$testMember);
         |    }
         |
         |    [Test]
         |    public void SerializeDeserialize() {
         |        var v1 = ${name}TestHelper.Create();
         |        var json = marshaller.Marshal<$name>(v1);
         |        var v2 = marshaller.Unmarshal<$name>(json);
         |        Assert.AreEqual(v1, v2);
         |    }
         |}
         """.stripMargin

    val header =
      """using IRT;
        |using IRT.Marshaller;
        |using NUnit.Framework;
       """.stripMargin

    modules.toTestSource(e.id.path.domain, modules.toTestModuleId(e.id), header, code)
  }

  def postIdentifier(id: NewTypeDef.Identifier)(implicit im: CSharpImports, domain: Domain): Seq[Module] = {
    if (!isLocallyResolvable(id.id, domain)) return Seq.empty
    val name = id.id.name
    val code =
      s"""public static class ${name}TestHelper {
         |    public static $name Create() {
         |        return new $name(${id.fields.map(f => DomainCSharpType(f.typeId).getRandomValue(0)).mkString(", ")});
         |    }
         |}
         |
         |[TestFixture]
         |public class ${name}_ShouldSerialize {
         |    IJsonMarshaller marshaller;
         |
         |    public ${name}_ShouldSerialize() {
         |        marshaller = new JsonNetMarshaller();
         |    }
         |
         |    [Test]
         |    public void SerializeDeserialize() {
         |        var v1 = ${name}TestHelper.Create();
         |        var json1 = marshaller.Marshal<$name>(v1);
         |        var v2 = marshaller.Unmarshal<$name>(json1);
         |        var json2 = marshaller.Marshal<$name>(v2);
         |        Assert.AreEqual(v1.ToString(), v2.ToString());
         |        Assert.AreEqual(json1.ToString(), json2.ToString());
         |    }
         |}
       """.stripMargin

    val header =
      """using IRT;
        |using IRT.Marshaller;
        |using NUnit.Framework;
       """.stripMargin

    modules.toTestSource(id.id.path.domain, modules.toTestModuleId(id.id), header, code)
  }

  def postAdt(a: NewTypeDef.Adt)(implicit im: CSharpImports, domain: Domain): Seq[Module] = {
    if (!isLocallyResolvable(a.id, domain)) return Seq.empty
    val name = a.id.name
    val alt  = a.alternatives.head
    val testValue = alt.typeId match {
      case _: StructureId =>
        DomainCSharpType(alt.typeId).getRandomValue(3)
      case _ =>
        s"${DomainCSharpType(alt.typeId).renderType(true)}TestHelper.Create()"
    }

    val code =
      s"""public static class ${name}TestHelper {
         |    public static $name Create() {
         |        return new $name.${alt.typename}($testValue);
         |    }
         |}
         |
         |[TestFixture]
         |public class ${name}_ShouldSerialize {
         |    IJsonMarshaller marshaller;
         |
         |    public ${name}_ShouldSerialize() {
         |        marshaller = new JsonNetMarshaller();
         |    }
         |
         |    [Test]
         |    public void SerializeDeserialize() {
         |        var v1 = ${name}TestHelper.Create();
         |        var json1 = marshaller.Marshal<$name>(v1);
         |        var v2 = marshaller.Unmarshal<$name>(json1);
         |        var json2 = marshaller.Marshal<$name>(v2);
         |        Assert.AreEqual(v1.ToString(), v2.ToString());
         |        Assert.AreEqual(json1.ToString(), json2.ToString());
         |    }
         |}
       """.stripMargin

    val header =
      """using IRT;
        |using IRT.Marshaller;
        |using System;
        |using System.Globalization;
        |using System.Collections;
        |using System.Collections.Generic;
        |using NUnit.Framework;
      """.stripMargin

    modules.toTestSource(a.id.path.domain, modules.toTestModuleId(a.id), header, code)
  }

  def postDto(d: NewTypeDef.Dto)(implicit im: CSharpImports, domain: Domain): Seq[Module] = {
    if (!isLocallyResolvable(d.id, domain)) return Seq.empty
    // Mirror legacy: a DTO synthesized as an interface impl (`DTOId(iface, "Struct")`)
    // is named `<Iface><DtoName>` in the generated C#. The legacy lookup was
    // `ts.inheritance.allParents(i.id).find(ii => ts.tools.implId(ii) == i.id)`;
    // the Domain equivalent is `domain.parents(d.id).find(ii => DomainCSStruct.implId(ii) == d.id)`.
    val implIface = domain.parents.getOrElse(d.id, Set.empty).find(ii => DomainCSStruct.implId(ii) == d.id)
    val dtoName   = if (implIface.isDefined) implIface.get.name + d.id.name else d.id.name

    val flat = domain.flattenedStructs.getOrElse(
      d.id,
      FlatStruct(d.id, List.empty, List.empty, List.empty),
    )
    val structure = DomainCSStruct.fromFlat(d.id, flat, d.struct.superclasses, domain)
    val struct    = DomainCSClass(d.id, d.id.name, structure, List.empty)

    val code =
      s"""public static class ${dtoName}TestHelper {
         |    public static $dtoName Create() {
         |        return new $dtoName(
         |${struct.fields.map(f => f.tp.getRandomValue(3)).mkString(",\n").shift(12)}
         |        );
         |    }
         |}
         |
         |[TestFixture]
         |public class ${dtoName}_ShouldSerialize {
         |    IJsonMarshaller marshaller;
         |
         |    public ${dtoName}_ShouldSerialize() {
         |        marshaller = new JsonNetMarshaller();
         |    }
         |
         |    [Test]
         |    public void SerializeDeserialize() {
         |        var v1 = ${dtoName}TestHelper.Create();
         |        var json1 = marshaller.Marshal<$dtoName>(v1);
         |        var v2 = marshaller.Unmarshal<$dtoName>(json1);
         |        var json2 = marshaller.Marshal<$dtoName>(v2);
         |        Assert.AreEqual(v1.ToString(), v2.ToString());
         |        Assert.AreEqual(json1.ToString(), json2.ToString());
         |    }
         |}
       """.stripMargin

    val header =
      """using IRT;
        |using IRT.Marshaller;
        |using System;
        |using System.Globalization;
        |using System.Collections;
        |using System.Collections.Generic;
        |using NUnit.Framework;
       """.stripMargin

    modules.toTestSource(
      d.id.path.domain,
      modules.toTestModuleId(d.id, if (implIface.isDefined) Some(implIface.get.name) else None),
      header,
      code,
    )
  }
}
