package izumi.idealingua.translator.tocsharp.domain.extensions

import izumi.fundamentals.platform.strings.IzString._
import izumi.idealingua.model.common.TypeId._
import izumi.idealingua.model.common.{Generic, Primitive, TypeId}
import izumi.idealingua.model.il.ast.typed.DefMethod
import izumi.idealingua.model.il.ast.typed.DefMethod.Output.{Alternative, Singular}
import izumi.idealingua.model.problems.IDLException
import izumi.idealingua.model.typespace.Typespace
import izumi.idealingua.translator.tocsharp.CSharpImports
import izumi.idealingua.translator.tocsharp.domain.DomainCSStruct
import izumi.idealingua.translator.tocsharp.types.{CSharpClass, CSharpField, CSharpType}
import izumi.idealingua.typer.ir.{Domain, FlatStruct, TypeDef => NewTypeDef}

/** PR-02 IMPL-7c Phase B M4: new-IR port of `JsonNetExtension`.
  *
  * Wire-format-critical. For each `TypeDef.Dto` / `Interface` / `Identifier`
  * / `Adt` / `Enum`, emits the `[JsonConverter(typeof(...))]` attribute
  * (pre-model splice) and the corresponding `<Name>_JsonNetConverter`
  * class (post-model splice), plus the Newtonsoft import set.
  *
  * Mirror of legacy `JsonNetExtension`
  * (`…/tocsharp/extensions/JsonNetExtension.scala`). Byte-equality vs the
  * legacy emitter is the production contract — the generated JSON
  * serializer/deserializer is the wire-format authority for the C# leg
  * of the cross-language matrix.
  *
  * IR substitutions vs legacy:
  *
  *  - `ts.tools.implId(iface)`        → `DomainCSStruct.implId(iface) = DTOId(iface, "Struct")`.
  *  - `ts.inheritance.allParents(id)` (legacy DTO impl-iface lookup at
  *    legacy `:83-84`)                → `domain.parents(dtoId)` (`Set[InterfaceId]`,
  *    filtered for `implId(iface) == dtoId`).
  *  - `ts.dealias(al)`                → `domain.aliases.getOrElse(al, al)` —
  *    by Phase 3 every `AliasId` maps directly to its fully-dealiased
  *    target, so no recursion needed (the alias-target may still be a
  *    `Generic`, which is handled by the recursive value-encoding cases).
  *
  * `Typespace` is threaded per-call for `CSharpType(...).renderType(...)`
  * / `renderFromString(...)` — `CSharpType` reads `ts.dealias` and
  * `ts.inheritance.parentsInherited` for `renderType` paths that touch
  * inheritance (used in `prepareReadProperty` for nested types).
  * This mirrors the renderer M1–M3 convention.
  *
  * `TBLOB` paths preserve the legacy `???` defects (legacy `:188, 241, 351`).
  * Per Q3 lock the wire format is base64-string for all three languages;
  * the legacy C# emitter has a `???` defect that throws at codegen time
  * for any DTO field of type `TBLOB`. Parity is required: the new
  * extension preserves the same throw points so production-swap won't
  * change the defect surface.
  */
object DomainCSJsonNetExtension {

  // ---- pre-model attribute helpers --------------------------------------

  def preIdentifier(id: NewTypeDef.Identifier): String =
    s"[JsonConverter(typeof(${id.id.name}_JsonNetConverter))]"

  def preEnum(id: NewTypeDef.Enum): String =
    s"[JsonConverter(typeof(${id.id.name}_JsonNetConverter))]"

  /** Mirrors legacy `JsonNetExtension.preModelEmit(ctx, i: DTO)` at `:81-87`.
    *
    * Legacy: search `ts.inheritance.allParents(i.id)` for an interface
    * whose `ts.tools.implId(parent) == i.id`. If found, the converter
    * name becomes `<ParentIface>.name + i.id.name` (i.e. `<Iface>Struct`).
    *
    * New IR: read `domain.parents(i.id)` (set of all interface ancestors,
    * direct or transitive) and find the one whose `implId == i.id`. Sort
    * defensively by `.name` for determinism (legacy's `allParents` returns
    * a `List` whose ordering is set-iteration-derived; `.find` returns
    * the first match — at most one interface can satisfy `implId == i.id`
    * because `implId(iface) = DTOId(iface, "Struct")` is injective in
    * `iface`, so set vs list ordering doesn't matter).
    */
  def preDto(domain: Domain, i: NewTypeDef.Dto): String = {
    val converterName = implIfaceOf(domain, i.id).map(p => p.name + i.id.name).getOrElse(i.id.name)
    s"[JsonConverter(typeof(${converterName}_JsonNetConverter))]"
  }

  def preInterface(id: NewTypeDef.Interface): String =
    s"[JsonConverter(typeof(${id.id.name}_JsonNetConverter))]"

  /** Legacy emits the same attribute for both the interface itself AND
    * for its synthetic impl-DTO (`DTO(eid, ...)` in `renderInterface :365`,
    * spliced via `${ext.preModelEmit(ctx, dto)}` at `:378`). The new
    * `DomainCSInterfaceRenderer` doesn't currently splice this — M5
    * production-swap will. For now we expose the impl-DTO attribute as
    * a helper so the test harness and the future renderer can both
    * reach it.
    */
  def preInterfaceImplStruct(i: NewTypeDef.Interface): String = {
    val eid           = DomainCSStruct.implId(i.id)
    val converterName = i.id.name + eid.name
    s"[JsonConverter(typeof(${converterName}_JsonNetConverter))]"
  }

  def preAdt(id: NewTypeDef.Adt): String =
    s"[JsonConverter(typeof(${id.id.name}_JsonNetConverter))]"

  def preAlternative(name: String): String =
    s"[JsonConverter(typeof(${name}_JsonNetConverter))]"

  // ---- post-model converter classes -------------------------------------

  def postIdentifier(id: NewTypeDef.Identifier): String =
    s"""public class ${id.id.name}_JsonNetConverter: JsonNetConverter<${id.id.name}> {
       |
       |$unityScriptingAttribute
       |    public ${id.id.name}_JsonNetConverter() {}
       |
       |$unityScriptingAttribute
       |    public override void WriteJson(JsonWriter writer, ${id.id.name} value, JsonSerializer serializer) {
       |        writer.WriteValue(value.ToString());
       |    }
       |
       |$unityScriptingAttribute
       |    public override ${id.id.name} ReadJson(JsonReader reader, System.Type objectType, ${id.id.name} existingValue, bool hasExistingValue, JsonSerializer serializer) {
       |        return ${id.id.name}.From((string)reader.Value);
       |    }
       |}
     """.stripMargin

  def postEnum(id: NewTypeDef.Enum): String =
    s"""public class ${id.id.name}_JsonNetConverter: JsonNetConverter<${id.id.name}> {
       |
       |$unityScriptingAttribute
       |    public ${id.id.name}_JsonNetConverter() {}
       |
       |$unityScriptingAttribute
       |    public override void WriteJson(JsonWriter writer, ${id.id.name} value, JsonSerializer serializer) {
       |        writer.WriteValue(value.ToString());
       |    }
       |
       |$unityScriptingAttribute
       |    public override ${id.id.name} ReadJson(JsonReader reader, System.Type objectType, ${id.id.name} existingValue, bool hasExistingValue, JsonSerializer serializer) {
       |        return ${id.id.name}Helpers.From((string)reader.Value);
       |    }
       |}
     """.stripMargin

  def postDto(domain: Domain, i: NewTypeDef.Dto, ts: Typespace, im: CSharpImports): String = {
    implicit val _ts: Typespace     = ts
    implicit val _im: CSharpImports = im

    val flat = domain.flattenedStructs.getOrElse(
      i.id,
      FlatStruct(i.id, List.empty, List.empty, List.empty),
    )
    val structure = DomainCSStruct.fromFlat(i.id, flat, i.struct.superclasses, domain)
    val struct    = CSharpClass(i.id, i.id.name, structure, List.empty)
    val converterName = implIfaceOf(domain, i.id).map(p => p.name + i.id.name).getOrElse(i.id.name)
    renderStructConverter(domain, converterName, struct)
  }

  def postInterface(domain: Domain, i: NewTypeDef.Interface): String = {
    val eid     = DomainCSStruct.implId(i.id)
    val eidName = i.id.name + eid.name
    val _       = domain
    s"""public class ${i.id.name}_JsonNetConverter: JsonNetConverter<${i.id.name}> {
       |
       |$unityScriptingAttribute
       |    public ${i.id.name}_JsonNetConverter() {}
       |
       |$unityScriptingAttribute
       |    public override void WriteJson(JsonWriter writer, ${i.id.name} value, JsonSerializer serializer) {
       |${renderSerialize(i.id, "value").shift(8)}
       |    }
       |
       |$unityScriptingAttribute
       |    public override ${i.id.name} ReadJson(JsonReader reader, System.Type objectType, ${i.id.name} existingValue, bool hasExistingValue, JsonSerializer serializer) {
       |        var json = JObject.Load(reader);
       |        var kv = json.Properties().First();
       |        var v_tpe = $eidName.GetType(kv.Name);
       |        var res = serializer.Deserialize(kv.Value.CreateReader(), v_tpe);
       |        return (${i.id.name})res;
       |    }
       |}
     """.stripMargin
  }

  /** Companion converter for the synthetic impl-struct of an interface.
    * Mirrors `postModelEmit(ctx, dto)` invoked from `renderInterface`'s
    * `companion` block. Reuses the DTO struct-converter renderer with
    * `converterName = i.id.name + eid.name`, exactly as legacy
    * `JsonNetExtension.postModelEmit(ctx, i: DTO)` produces when the
    * `implIface.isDefined` branch fires.
    */
  def postInterfaceImplStruct(domain: Domain, i: NewTypeDef.Interface, ts: Typespace, im: CSharpImports): String = {
    implicit val _ts: Typespace     = ts
    implicit val _im: CSharpImports = im

    val eid           = DomainCSStruct.implId(i.id)
    val converterName = i.id.name + eid.name
    val flat = domain.flattenedStructs.getOrElse(
      i.id,
      FlatStruct(i.id, List.empty, List.empty, List.empty),
    )
    val ifaceStruct = DomainCSStruct.fromFlat(i.id, flat, i.struct.superclasses, domain)
    // Legacy: `CSharpClass(eid, i.id.name + eid.name, structure, List(i.id))`
    // — uses the same flattened-iface structure. JsonNet only reads
    // `struct.fields` to write/read JSON properties, so the iface
    // structure (which already carries all inherited fields, like the
    // impl-struct) gives a byte-equal serializer body.
    val struct = CSharpClass(eid, converterName, ifaceStruct, List(i.id))
    renderStructConverter(domain, converterName, struct)
  }

  def postAdt(i: NewTypeDef.Adt, ts: Typespace, im: CSharpImports): String = {
    implicit val _ts: Typespace     = ts
    implicit val _im: CSharpImports = im
    s"""public class ${i.id.name}_JsonNetConverter: JsonNetConverter<${i.id.name}> {
       |
       |$unityScriptingAttribute
       |    public ${i.id.name}_JsonNetConverter() {}
       |
       |$unityScriptingAttribute
       |    public override void WriteJson(JsonWriter writer, ${i.id.name} al, JsonSerializer serializer) {
       |        writer.WriteStartObject();
       |${i.alternatives
        .map(m => s"""if (al is ${i.id.name}.${m.typename}) {
                     |    writer.WritePropertyName("${m.wireId}");
                     |    var v = (al as ${i.id.name}.${m.typename}).Value;
                     |${renderSerialize(m.typeId, "v").shift(4)}
                     |} else""".stripMargin).mkString("\n").shift(8)}
       |        {
       |            throw new System.Exception("Unknown ${i.id.name} type: " + al);
       |        }
       |        writer.WriteEndObject();
       |    }
       |
       |$unityScriptingAttribute
       |    public override ${i.id.name} ReadJson(JsonReader reader, System.Type objectType, ${i.id.name} existingValue, bool hasExistingValue, JsonSerializer serializer) {
       |        var json = JObject.Load(reader);
       |        var kv = json.Properties().First();
       |        switch (kv.Name) {
       |${i.alternatives
        .map(m => s"""case "${m.wireId}": {
                     |    var v = serializer.Deserialize<${CSharpType(m.typeId).renderType(true)}>(kv.Value.CreateReader());
                     |    return new ${i.id.name}.${m.typename}(v);
                     |}
           """.stripMargin).mkString("\n").shift(12)}
       |            default:
       |                throw new System.Exception("Unknown ${i.id.name} type: " + kv.Name);
       |        }
       |    }
       |}
     """.stripMargin
  }

  def postAlternative(name: String, alternative: Alternative, leftType: TypeId, rightType: TypeId, ts: Typespace, im: CSharpImports): String = {
    implicit val _ts: Typespace     = ts
    implicit val _im: CSharpImports = im
    val left  = CSharpType(leftType).renderType(true)
    val right = CSharpType(rightType).renderType(true)
    s"""public class ${name}_JsonNetConverter: JsonNetConverter<$name> {
       |
       |$unityScriptingAttribute
       |    public ${name}_JsonNetConverter() {}
       |
       |$unityScriptingAttribute
       |    public override void WriteJson(JsonWriter writer, $name al, JsonSerializer serializer) {
       |        writer.WriteStartObject();
       |
       |        if (al.IsLeft()) {
       |            writer.WritePropertyName("Failure");
       |            var l = al.GetLeft();
       |${renderSerializeOutput(alternative.failure, "l").shift(12)}
       |        } else {
       |            writer.WritePropertyName("Success");
       |            var r = al.GetRight();
       |${renderSerializeOutput(alternative.success, "r").shift(12)}
       |        }
       |
       |        writer.WriteEndObject();
       |    }
       |
       |$unityScriptingAttribute
       |    public override $name ReadJson(JsonReader reader, System.Type objectType, $name existingValue, bool hasExistingValue, JsonSerializer serializer) {
       |        var json = JObject.Load(reader);
       |        var kv = json.Properties().First();
       |        switch (kv.Name) {
       |            case "Success": {
       |                var v = serializer.Deserialize<$right>(kv.Value.CreateReader());
       |                return new Either<$left, $right>.Right(v);
       |            }
       |
       |            case "Failure": {
       |                var v = serializer.Deserialize<$left>(kv.Value.CreateReader());
       |                return new Either<$left, $right>.Left(v);
       |            }
       |
       |            default:
       |                throw new System.Exception("Unknown either $name type: " + kv.Name);
       |        }
       |    }
       |}
     """.stripMargin
  }

  // ---- imports ----------------------------------------------------------

  def importsIdentifier: List[String] = List("Newtonsoft.Json", "IRT.Marshaller")
  def importsEnum: List[String]       = List("Newtonsoft.Json", "IRT.Marshaller")
  def importsDto: List[String]        = List("Newtonsoft.Json", "Newtonsoft.Json.Linq", "IRT.Marshaller")
  def importsInterface: List[String]  = List("Newtonsoft.Json", "System.Linq", "Newtonsoft.Json.Linq", "IRT.Marshaller")
  def importsAdt: List[String]        = List("Newtonsoft.Json", "System.Linq", "Newtonsoft.Json.Linq", "IRT.Marshaller")

  // ---- internals --------------------------------------------------------

  private def implIfaceOf(domain: Domain, dtoId: DTOId): Option[InterfaceId] = {
    domain.parents.getOrElse(dtoId, Set.empty).find(p => DomainCSStruct.implId(p) == dtoId)
  }

  private def renderStructConverter(domain: Domain, name: String, struct: CSharpClass)(implicit im: CSharpImports, ts: Typespace): String = {
    val currentDomain = struct.id.uniqueDomainName
    s"""public class ${name}_JsonNetConverter: JsonNetConverter<$name> {
       |
       |$unityScriptingAttribute
       |    public ${name}_JsonNetConverter() {}
       |
       |$unityScriptingAttribute
       |    public override void WriteJson(JsonWriter writer, $name v, JsonSerializer serializer) {
       |        writer.WriteStartObject();
       |${struct.fields.map(f => writeProperty(domain, f)).mkString("\n").shift(8)}
       |        writer.WriteEndObject();
       |    }
       |
       |$unityScriptingAttribute
       |    public override $name ReadJson(JsonReader reader, System.Type objectType, $name existingValue, bool hasExistingValue, JsonSerializer serializer) {
       |        ${if (struct.fields.isEmpty) "reader.Skip();" else "var json = JObject.Load(reader);"}
       |${struct.fields.map(f => prepareReadProperty(domain, f, currentDomain)).filter(_.isDefined).map(_.get).mkString("\n").shift(8)}
       |        return new $name(
       |${struct.fields.map(f => readProperty(domain, f, currentDomain)).mkString(", \n").shift(12)}
       |        );
       |    }
       |}
     """.stripMargin
  }

  private def renderSerialize(id: TypeId, varName: String): String = id match {
    case _: InterfaceId =>
      s"""// Serializing polymorphic type ${id.name}
         |writer.WriteStartObject();
         |writer.WritePropertyName($varName.GetFullClassName());
         |serializer.Serialize(writer, $varName);
         |writer.WriteEndObject();
        """.stripMargin
    case _ => s"""serializer.Serialize(writer, $varName);"""
  }

  private def renderSerializeOutput(output: DefMethod.Output, varName: String): String = output match {
    case si: Singular =>
      si.typeId match {
        case inf: InterfaceId =>
          s"""// Serializing polymorphic type ${inf.name}
             |writer.WriteStartObject();
             |writer.WritePropertyName($varName.GetFullClassName());
             |serializer.Serialize(writer, $varName);
             |writer.WriteEndObject();
        """.stripMargin
        case _ => s"""serializer.Serialize(writer, $varName);"""
      }
    case _ => s"""serializer.Serialize(writer, $varName);"""
  }

  private def writeProperty(domain: Domain, f: CSharpField)(implicit im: CSharpImports, ts: Typespace): String =
    writePropertyValue(domain, "v." + f.renderMemberName(), f.tp, Some(f.name))

  private def writePropertyValue(domain: Domain, src: String, t: CSharpType, key: Option[String] = None, depth: Int = 1)(implicit im: CSharpImports, ts: Typespace): String = {
    t.id match {
      case g: Generic.TOption =>
        val optionType = CSharpType(g.valueType)
        s"""if (${if (optionType.isNullable) src + " != null" else src + ".HasValue"}) {
           |${writePropertyValue(domain, if (optionType.isNullable) src else src + ".Value", optionType, key).shift(4)}
           |}
         """.stripMargin
      case al: AliasId => writePropertyValue(domain, src, CSharpType(domain.aliases.getOrElse(al, al)), key, depth)
      case _ =>
        (if (key.isDefined) s"""writer.WritePropertyName("${key.get}");\n""" else "") + (
          t.id match {
            case g: Generic =>
              g match {
                case m: Generic.TMap =>
                  val iter = s"mkv${if (depth > 1) depth.toString else ""}"
                  s"""writer.WriteStartObject();
                     |foreach(var $iter in $src) {
                     |    writer.WritePropertyName($iter.Key.ToString());
                     |${writePropertyValue(domain, s"$iter.Value", CSharpType(m.valueType), depth = depth + 1).shift(4)}
                     |}
                     |writer.WriteEndObject();
                 """.stripMargin
                case l: Generic.TList =>
                  val iter = s"lv${if (depth > 1) depth.toString else ""}"
                  s"""writer.WriteStartArray();
                     |foreach (var $iter in $src) {
                     |${writePropertyValue(domain, s"$iter", CSharpType(l.valueType), depth = depth + 1).shift(4)}
                     |}
                     |writer.WriteEndArray();
                 """.stripMargin
                case s: Generic.TSet =>
                  val iter = s"lv${if (depth > 1) depth.toString else ""}"
                  s"""writer.WriteStartArray();
                     |foreach (var $iter in $src) {
                     |${writePropertyValue(domain, s"$iter", CSharpType(s.valueType), depth = depth + 1).shift(4)}
                     |}
                     |writer.WriteEndArray();
                 """.stripMargin
                case _ => throw new Exception("Option should have been checked already.")
              }
            case p: Primitive =>
              p match {
                case Primitive.TBool   => s"writer.WriteValue($src);"
                case Primitive.TString => s"writer.WriteValue($src);"
                case Primitive.TInt8   => s"writer.WriteValue($src);"
                case Primitive.TInt16  => s"writer.WriteValue($src);"
                case Primitive.TInt32  => s"writer.WriteValue($src);"
                case Primitive.TInt64  => s"writer.WriteValue($src);"
                case Primitive.TUInt8  => s"writer.WriteValue($src);"
                case Primitive.TUInt16 => s"writer.WriteValue($src);"
                case Primitive.TUInt32 => s"writer.WriteValue($src);"
                case Primitive.TUInt64 => s"writer.WriteValue($src);"
                case Primitive.TFloat  => s"writer.WriteValue($src);"
                case Primitive.TDouble => s"writer.WriteValue($src);"
                case Primitive.TBLOB   => ???
                case Primitive.TUUID   => s"writer.WriteValue($src.ToString());"
                case Primitive.TTime => s"""writer.WriteValue(string.Format("{0:00}:{1:00}:{2:00}.{3:000}", (int)$src.TotalHours, $src.Minutes, $src.Seconds, $src.Milliseconds));"""
                case Primitive.TDate => s"""writer.WriteValue($src.ToString("yyyy-MM-dd", CultureInfo.InvariantCulture));"""
                case Primitive.TTs   => s"""writer.WriteValue($src.ToString(JsonNetTimeFormats.TslDefault, CultureInfo.InvariantCulture));"""
                case Primitive.TTsTz => s"""writer.WriteValue($src.ToString($src.Kind == DateTimeKind.Utc ? JsonNetTimeFormats.TsuDefault : JsonNetTimeFormats.TszDefault, CultureInfo.InvariantCulture));"""
                case Primitive.TTsU  => s"""writer.WriteValue($src.ToUniversalTime().ToString(JsonNetTimeFormats.TsuDefault, CultureInfo.InvariantCulture));"""
              }
            case _ =>
              t.id match {
                case _: EnumId | _: IdentifierId => s"""writer.WriteValue($src.ToString());"""
                case _: InterfaceId              => renderSerialize(t.id, src)
                case _: AdtId | _: DTOId         => s"""serializer.Serialize(writer, $src);"""
                case _                           => throw new IDLException(s"Impossible writePropertyValue type: ${t.id}")
              }
          }
        )
    }
  }

  private def propertyNeedsPrepare(domain: Domain, i: TypeId): Boolean = i match {
    case g: Generic =>
      g match {
        case _: Generic.TMap    => true
        case _: Generic.TList   => true
        case _: Generic.TSet    => true
        case _: Generic.TOption => true
      }
    case p: Primitive =>
      p match {
        case Primitive.TBool   => false
        case Primitive.TString => false
        case Primitive.TInt8   => false
        case Primitive.TInt16  => false
        case Primitive.TInt32  => false
        case Primitive.TInt64  => false
        case Primitive.TUInt8  => false
        case Primitive.TUInt16 => false
        case Primitive.TUInt32 => false
        case Primitive.TUInt64 => false
        case Primitive.TFloat  => false
        case Primitive.TDouble => false
        case Primitive.TUUID   => false
        case Primitive.TTime   => false
        case Primitive.TDate   => false
        case Primitive.TTs     => false
        case Primitive.TTsTz   => false
        case Primitive.TTsU    => false
        case Primitive.TBLOB   => ???
      }
    case c =>
      c match {
        case _: EnumId | _: IdentifierId => false
        case _: DTOId                    => true
        case _: InterfaceId | _: AdtId   => false
        case al: AliasId                 => propertyNeedsPrepare(domain, domain.aliases.getOrElse(al, al))
        case _                           => throw new IDLException(s"Impossible propertyNeedsPrepare type: $i")
      }
  }

  private def prepareReadProperty(domain: Domain, f: CSharpField, currentDomain: String)(implicit im: CSharpImports, ts: Typespace): Option[String] = {
    if (!propertyNeedsPrepare(domain, f.tp.id)) None
    else prepareReadPropertyValue(domain, s"""json["${f.name}"]""", s"_${f.name}", f.tp, createDst = true, currentDomain)
  }

  private def prepareReadPropertyValue(domain: Domain, src: String, dst: String, i: CSharpType, createDst: Boolean, currentDomain: String)(implicit
    im: CSharpImports,
    ts: Typespace,
  ): Option[String] = {
    if (!propertyNeedsPrepare(domain, i.id)) None
    else {
      i.id match {
        case gm: Generic.TMap =>
          val mk = CSharpType(gm.keyType)
          val mt = CSharpType(gm.valueType)
          Some(
            s"""${if (createDst) "var " else " "}$dst = new ${i.renderType(true)}();
               |foreach (var ${dst}_kv in ((JObject)$src).Properties()) {
               |    ${mt.renderType(true)} ${dst}_dv;
               |${(if (propertyNeedsPrepare(domain, mt.id)) prepareReadPropertyValue(domain, dst + "_kv.Value", dst + "_dv", mt, createDst = false, currentDomain).get
                  else s"${dst}_dv = ${readPropertyValue(domain, dst + "_kv.Value", mt, currentDomain)};").shift(4)}
               |    $dst.Add(${mk.renderFromString(dst + "_kv.Name", unescape = false, currentDomain)}, ${dst}_dv);
               |}
             """.stripMargin
          )
        case gl: Generic.TList =>
          val lt = CSharpType(gl.valueType)
          Some(
            s"""${if (createDst) "var " else " "}$dst = new ${i.renderType(true)}();
               |foreach (var ${dst}_sv in (JArray)$src) {
               |    ${lt.renderType(true)} ${dst}_d;
               |${(if (propertyNeedsPrepare(domain, gl.valueType)) prepareReadPropertyValue(domain, dst + "_sv", dst + "_d", lt, createDst = false, currentDomain).get
                  else s"${dst}_d = ${readPropertyValue(domain, dst + "_sv", lt, currentDomain)};").shift(4)}
               |    $dst.Add(${dst}_d);
               |}
             """.stripMargin
          )
        case gs: Generic.TSet =>
          val st = CSharpType(gs.valueType)
          Some(
            s"""${if (createDst) "var " else " "}$dst = new ${i.renderType(true)}();
               |foreach (var ${dst}_lv in (JArray)$src) {
               |    ${st.renderType(true)} ${dst}_d;
               |${(if (propertyNeedsPrepare(domain, gs.valueType)) prepareReadPropertyValue(domain, dst + "_lv", dst + "_d", st, createDst = false, currentDomain).get
                  else s"${dst}_d = ${readPropertyValue(domain, dst + "_lv", st, currentDomain)};").shift(4)}
               |    $dst.Add(${dst}_d);
               |}
             """.stripMargin
          )
        case o: Generic.TOption =>
          val ot       = CSharpType(o.valueType)
          val proxySrc = dst + "Raw"
          Some(s"""${i.renderType(true)} $dst = null;
                  |var $proxySrc = $src;
                  |if ($proxySrc != null && $proxySrc.Type != JTokenType.Null) {
                  |${(if (propertyNeedsPrepare(domain, o.valueType)) prepareReadPropertyValue(domain, proxySrc, dst, ot, createDst = false, currentDomain).get
                     else s"$dst = ${readPropertyValue(domain, proxySrc, ot, currentDomain)};").shift(4)}
                  |}
             """.stripMargin)
        case al: AliasId =>
          prepareReadPropertyValue(domain, src, dst, CSharpType(domain.aliases.getOrElse(al, al)), createDst = createDst, currentDomain)
        case _: DTOId =>
          Some(s"""${if (createDst) "var " else ""}$dst = serializer.Deserialize<${i.renderType(true)}>($src.CreateReader());""".stripMargin)
        case _ => throw new Exception("Other cases should have been checked already.")
      }
    }
  }

  private def readPropertyValue(domain: Domain, src: String, t: CSharpType, currentDomain: String)(implicit im: CSharpImports, ts: Typespace): String = {
    t.id match {
      case p: Primitive =>
        p match {
          case Primitive.TBool   => s"$src.Value<bool>()"
          case Primitive.TString => s"$src.Value<string>()"
          case Primitive.TInt8   => s"$src.Value<sbyte>()"
          case Primitive.TInt16  => s"$src.Value<short>()"
          case Primitive.TInt32  => s"$src.Value<int>()"
          case Primitive.TInt64  => s"$src.Value<long>()"
          case Primitive.TUInt8  => s"$src.Value<byte>()"
          case Primitive.TUInt16 => s"$src.Value<ushort>()"
          case Primitive.TUInt32 => s"$src.Value<uint>()"
          case Primitive.TUInt64 => s"$src.Value<ulong>()"
          case Primitive.TFloat  => s"$src.Value<float>()"
          case Primitive.TDouble => s"$src.Value<double>()"
          case Primitive.TBLOB   => ???
          case Primitive.TUUID   => s"new System.Guid($src.Value<string>())"
          case Primitive.TTime   => s"TimeSpan.Parse($src.Value<string>())"
          case Primitive.TDate   => s"DateTime.Parse($src.Value<string>(), CultureInfo.InvariantCulture)"
          case Primitive.TTs     => s"DateTime.ParseExact($src.Value<string>(), JsonNetTimeFormats.Tsl, CultureInfo.InvariantCulture, DateTimeStyles.None)"
          case Primitive.TTsTz   => s"DateTime.ParseExact($src.Value<string>(), JsonNetTimeFormats.Tsz, CultureInfo.InvariantCulture, DateTimeStyles.None)"
          case Primitive.TTsU    => s"DateTime.ParseExact($src.Value<string>(), JsonNetTimeFormats.Tsu, CultureInfo.InvariantCulture, DateTimeStyles.None)"
        }
      case _ =>
        t.id match {
          case _: EnumId                 => s"${t.renderType(t.id.uniqueDomainName != currentDomain)}Helpers.From($src.Value<string>())"
          case _: IdentifierId           => s"${t.renderType(true)}.From($src.Value<string>())"
          case _: InterfaceId | _: AdtId => s"serializer.Deserialize<${t.renderType(true)}>($src.CreateReader())"
          case al: AliasId               => readPropertyValue(domain, src, CSharpType(domain.aliases.getOrElse(al, al)), currentDomain)
          case _                         => throw new IDLException(s"Impossible readPropertyValue type: ${t.id}")
        }
    }
  }

  private def readProperty(domain: Domain, f: CSharpField, currentDomain: String)(implicit im: CSharpImports, ts: Typespace): String = {
    if (propertyNeedsPrepare(domain, f.tp.id)) s"_${f.name}"
    else readPropertyValue(domain, s"""json["${f.name}"]""", f.tp, currentDomain)
  }

  private val unityScriptingAttribute =
    """#if UNITY_5_3_OR_NEWER
      |    [UnityEngine.Scripting.RequiredMember]
      |#endif""".stripMargin
}
