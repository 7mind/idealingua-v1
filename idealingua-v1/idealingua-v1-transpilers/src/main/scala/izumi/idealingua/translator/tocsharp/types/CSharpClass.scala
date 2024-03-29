package izumi.idealingua.translator.tocsharp.types

import izumi.fundamentals.platform.strings.IzString.*
import izumi.idealingua.model.common.TypeId
import izumi.idealingua.model.common.TypeId.*
import izumi.idealingua.model.il.ast.typed.SimpleStructure
import izumi.idealingua.model.typespace.Typespace
import izumi.idealingua.model.typespace.structures.Struct
import izumi.idealingua.translator.tocsharp.CSharpImports

final case class CSharpClass(
  id: TypeId,
  name: String,
  fields: Seq[CSharpField],
  implements: List[InterfaceId] = List.empty,
)(implicit
  im: CSharpImports,
  ts: Typespace,
) {

  def renderHeader(): String = {
    val impls = if (implements.isEmpty) "" else " : " + implements.map(i => i.name).mkString(", ")
    s"public class $name$impls"
  }

  def render(withWrapper: Boolean, withSlices: Boolean, withRTTI: Boolean, withCTORs: Option[String] = None): String = {
    val indent = if (withWrapper) 4 else 0

    val ctorWithParams =
      s"""public $name(${fields.map(f => s"${f.tp.renderType(true)} ${f.renderMemberName(capitalize = false, uncapitalize = true)}").mkString(", ")}) {
         |${fields.map(f => s"this.${f.renderMemberName()} = ${f.renderMemberName(capitalize = false, uncapitalize = true)};").mkString("\n").shift(4)}
         |}
         """.stripMargin

    val pkg = id.path.toPackage.mkString(".")
    val rtti =
      s"""public static readonly string RTTI_PACKAGE = "$pkg";
         |public static readonly string RTTI_CLASSNAME = "${id.name}";
         |public static readonly string RTTI_FULLCLASSNAME = "${id.wireId}";
         |public string GetPackageName() { return $name.RTTI_PACKAGE; }
         |public string GetClassName() { return $name.RTTI_CLASSNAME; }
         |public string GetFullClassName() { return $name.RTTI_FULLCLASSNAME; }
         """.stripMargin

    val ctors =
      if (withCTORs.isEmpty) ""
      else
        s"""private static Dictionary<string, System.Type> __types = new Dictionary<string, System.Type>();
           |public static void Register(string id, System.Type tpe) {
           |    $name.__types[id] = tpe;
           |}
           |
           |public static void Unregister(string id) {
           |    $name.__types.Remove(id);
           |}
           |
           |public static System.Type GetType(string id) {
           |    if (!$name.__types.TryGetValue(id, out var tpe)) {
           |        throw new Exception("Unknown class name: " + id + " for interface ${withCTORs.get}.");
           |    }
           |
           |    return tpe;
           |}
           |
           |static $name() {
           |    var type = typeof(${withCTORs.get});
           |    #if IRT_SCAN_ALL_ASSEMBLIES
           |        var assemblies = AppDomain.CurrentDomain.GetAssemblies();
           |    #else
           |        var assemblies = new[] {Assembly.GetExecutingAssembly()};
           |    #endif
           |    foreach (var assembly in assemblies) {
           |        System.Type[] types = null;
           |        try {
           |            types = assembly.GetTypes();
           |        } catch (Exception) {
           |            // ReflectionTypeLoadException potentially caught here
           |            continue;
           |        }
           |        foreach (var tp in types) {
           |            if (type.IsAssignableFrom(tp) && !tp.IsInterface) {
           |                var rttiID = tp.GetField("RTTI_FULLCLASSNAME");
           |                if (rttiID != null) {
           |                    $name.Register((string)rttiID.GetValue(null), tp);
           |                }
           |            }
           |        }
           |    }
           |}
         """.stripMargin

    val content =
      s"""${if (withRTTI) rtti else ""}
         |${fields.map(f => f.renderMember(false)).mkString("\n")}
         |
         |public $name() {
         |${fields
          .map(f => if (f.tp.getInitValue.isDefined) f.renderMemberName() + " = " + f.tp.getInitValue.get + ";" else "").filterNot(_.isEmpty).mkString("\n").shift(4)}
         |}
         |
         |${if (!fields.isEmpty) ctorWithParams else ""}
         |${if (withSlices) "\n" + renderSlices() else ""}
         |${if (withCTORs.isDefined) ctors else ""}
         """.stripMargin

    s"""${if (withWrapper) s"${renderHeader()} {" else ""}
       |${content.shift(indent)}
       |${if (withWrapper) "}" else ""}
       """.stripMargin
  }

  private def renderSlice(i: InterfaceId): String = {
//    val interface = ts(i).asInstanceOf[Interface]
    val eid       = ts.tools.implId(i)
    val eidStruct = ts.structure.structure(eid)
    val eidClass  = CSharpClass(eid, i.name + eid.name, eidStruct, List.empty)

    s"""public ${i.name} To${i.name}() {
       |    var res = new ${i.name}${eid.name}();
       |${eidClass.fields.map(f => s"res.${f.renderMemberName()} = this.${f.renderMemberName()};").mkString("\n").shift(4)}
       |    return res;
       |}
       |
       |public void Load${i.name}(${i.name} value) {
       |${eidClass.fields.map(f => s"this.${f.renderMemberName()} = value.${f.renderMemberName()};").mkString("\n").shift(4)}
       |}
     """.stripMargin
  }

  private def renderSlices(): String = {
    implements.map(i => renderSlice(i)).mkString("\n")
  }
}

object CSharpClass {
  def apply(
    id: TypeId,
    name: String,
    fields: Seq[CSharpField],
    implements: List[InterfaceId] = List.empty,
  )(implicit
    im: CSharpImports,
    ts: Typespace,
  ): CSharpClass = new CSharpClass(id, name, fields, implements)

  def apply(id: TypeId, name: String, st: Struct, implements: List[InterfaceId])(implicit im: CSharpImports, ts: Typespace): CSharpClass = {
    val names = st.all.map(_.field.name).distinct
    val fields = names.map {
      fieldName =>
        val group = st.all.filter(_.field.name == fieldName)
        CSharpField(
          field      = if (group.head.defn.variance.nonEmpty) group.head.defn.variance.last else group.head.field,
          structName = name,
          by         = if (group.length > 1) group.map(ef => ef.defn.definedBy.name) else Seq.empty,
        )
    }
    new CSharpClass(id, name, fields, st.superclasses.interfaces ++ implements)
  }

  def apply(id: TypeId, st: SimpleStructure)(implicit im: CSharpImports, ts: Typespace): CSharpClass =
    new CSharpClass(id, id.name, st.fields.map(f => CSharpField(f, id.name, Seq.empty)), List.empty)
}
