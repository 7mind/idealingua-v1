package izumi.idealingua.translator.tocsharp.domain

import izumi.fundamentals.platform.strings.IzString._
import izumi.idealingua.model.common.TypeId
import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.il.ast.typed.{Field, SimpleStructure, Super}
import izumi.idealingua.model.typespace.structures.Struct
import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.typer.ir.{Domain, FlatStruct}

/** Domain-consuming twin of `CSharpClass` (PR-02 IMPL-10-prep-Cs1).
  *
  * Legacy `CSharpClass` is `(id, name, fields, implements)(implicit im, ts)`.
  * The two `Typespace` usages inside `renderSlice`:
  *
  *   - `ts.tools.implId(i)` → `DomainCSStruct.implId(i)` —
  *     `DTOId(i, "Struct")` constant.
  *   - `ts.structure.structure(eid)` → the legacy `Struct` shape for the
  *     interface's synthetic impl-DTO. Since the impl-DTO is not in
  *     `domain.userTypes`, we read the iface's `FlatStruct` from
  *     `domain.flattenedStructs(i)` and reshape it via
  *     `DomainCSStruct.implFlatStruct` + `fromFlat`.
  *
  * Class structure mirrors legacy verbatim (constructor params, `render` /
  * `renderHeader` / `renderSlice` bodies). The single shape change is the
  * implicit list: `Domain` replaces `Typespace`.
  */
final case class DomainCSClass(
  id: TypeId,
  name: String,
  fields: Seq[DomainCSField],
  implements: List[InterfaceId] = List.empty,
  /** Original flattened source struct, when this class was constructed via
    * `apply(id, name, st: Struct, ...)`. Used by `renderSlice` to look up
    * which fields belong to a given parent interface (legacy
    * `ts.structure.structure(eid)` substitute — necessary for cross-domain
    * mixin parents whose `flattenedStructs` entry lives in the foreign
    * domain's `Domain`, not this one).
    */
  sourceStruct: Option[Struct] = None,
)(implicit
  im: CSharpImports,
  domain: Domain,
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
    val eid = DomainCSStruct.implId(i)

    // Legacy `ts.structure.structure(eid)` returns the impl-DTO's flat
    // struct (iface's own fields + every field transitively contributed
    // through `&` interfaces and `+` concept mixins).
    //
    // For LOCAL ifaces we project the corresponding `FlatStruct` entry
    // out of `domain.flattenedStructs` and feed it through the same
    // `DomainCSStruct.fromFlat` pipeline used for the owning DTO — this
    // is the byte-equivalent of legacy.
    //
    // For CROSS-DOMAIN ifaces the flattener does not emit a local
    // `FlatStruct` entry (PR-02 IMPL-7a.2-Fj harvests foreign structs
    // into BFS but stores `FlatStruct` only for local owners). In that
    // case we fall back to filtering the calling class's `sourceStruct`
    // for fields with `origin == iface` — sufficient because cross-domain
    // mixin interfaces in the corpus carry no parents/concepts and the
    // BFS already enumerated every iface-contributed field with the
    // iface as `origin`.
    val eidClass: DomainCSClass = domain.flattenedStructs.get(i) match {
      case Some(ifaceFlat) =>
        val implFlat  = DomainCSStruct.implFlatStruct(eid, ifaceFlat)
        val supers    = Super(List(i), List.empty, List.empty)
        val eidStruct = DomainCSStruct.fromFlat(eid, implFlat, supers, domain)
        DomainCSClass(eid, i.name + eid.name, eidStruct, List.empty)
      case None =>
        sourceStruct match {
          case Some(s) =>
            val sliceExtended = s.all.filter(_.defn.definedBy == i)
            val sliceStruct = new Struct(
              id           = eid,
              superclasses = Super(List(i), List.empty, List.empty),
              unambigious  = s.unambigious.filter(_.defn.definedBy == i),
              ambigious    = s.ambigious.filter(_.defn.definedBy == i),
              all          = sliceExtended,
            )
            DomainCSClass(eid, i.name + eid.name, sliceStruct, List.empty)
          case None =>
            // Last-resort fallback (empty slice). Matches the legacy
            // behaviour for an iface whose struct is structurally
            // unresolved, since neither path could surface its fields.
            val emptyFlat = FlatStruct(i, List.empty, List.empty, List.empty)
            val implFlat  = DomainCSStruct.implFlatStruct(eid, emptyFlat)
            val supers    = Super(List(i), List.empty, List.empty)
            val eidStruct = DomainCSStruct.fromFlat(eid, implFlat, supers, domain)
            DomainCSClass(eid, i.name + eid.name, eidStruct, List.empty)
        }
    }

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

object DomainCSClass {
  def apply(
    id: TypeId,
    name: String,
    fields: Seq[DomainCSField],
  )(implicit
    im: CSharpImports,
    domain: Domain,
  ): DomainCSClass = new DomainCSClass(id, name, fields, List.empty)

  def apply(
    id: TypeId,
    name: String,
    fields: Seq[DomainCSField],
    implements: List[InterfaceId],
  )(implicit
    im: CSharpImports,
    domain: Domain,
  ): DomainCSClass = new DomainCSClass(id, name, fields, implements)

  def apply(id: TypeId, name: String, st: Struct, implements: List[InterfaceId])(implicit im: CSharpImports, domain: Domain): DomainCSClass = {
    val names = st.all.map(_.field.name).distinct
    val fields = names.map {
      fieldName =>
        val group = st.all.filter(_.field.name == fieldName)
        DomainCSField(
          field      = if (group.head.defn.variance.nonEmpty) group.head.defn.variance.last else group.head.field,
          structName = name,
          by         = if (group.length > 1) group.map(ef => ef.defn.definedBy.name) else Seq.empty,
        )
    }
    new DomainCSClass(id, name, fields, st.superclasses.interfaces ++ implements, sourceStruct = Some(st))
  }

  def apply(id: TypeId, st: SimpleStructure)(implicit im: CSharpImports, domain: Domain): DomainCSClass =
    new DomainCSClass(id, id.name, st.fields.map(f => DomainCSField(f, id.name, Seq.empty)), List.empty)

  /** Construct a `DomainCSClass` directly from raw fields, mirroring the
    * legacy `CSharpClass(id, name, fields, implements)` form used by
    * `DomainCSharpType.randomInterface`.
    */
  def fromFields(id: TypeId, name: String, rawFields: Seq[Field], implements: List[InterfaceId])(implicit im: CSharpImports, domain: Domain): DomainCSClass =
    new DomainCSClass(id, name, rawFields.map(f => DomainCSField(f, name, Seq.empty)), implements)
}
