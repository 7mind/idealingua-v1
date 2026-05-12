package izumi.idealingua.translator.tocsharp

import izumi.idealingua.model.common.TypeId

final case class CSharpImport(id: TypeId, namespace: Seq[String], usingName: String)

/** C# `using`/aliased-imports accumulator.
  *
  * IMPL-10c (2026-05-12): the legacy `CSharpImports.apply(definition, …)(ts)`
  * factories — which traversed a legacy `Typespace` to collect transitive
  * type dependencies — were deleted with the rest of the legacy translator
  * tree. The Domain pipeline uses `tocsharp.domain.DomainCSImports.apply`
  * which constructs `CSharpImports(imports: List[CSharpImport])` directly
  * from a `Domain` IR. Only the case-class + its rendering instance methods
  * survive.
  */
final case class CSharpImports(imports: List[CSharpImport] = List.empty) {
  protected def isAmbiguousName(name: String): Boolean = {
    val ambiguous = Seq("Type", "Environment")
    ambiguous.contains(name)
  }

  def renderImports(extra: List[String] = List.empty): String = {
    if (imports.isEmpty && extra.isEmpty) {
      return ""
    }

    val usings = imports.flatMap(i => if (i.namespace.nonEmpty) i.namespace.map(n => s"using $n;") else List("")) ++
      extra.map(e => s"using $e;")
    usings.distinct.mkString("\n")
  }

  def renderUsings(): String = {
    if (imports.isEmpty) {
      return ""
    }

    imports.map(i => if (i.usingName != "") s"using ${i.usingName} = ${i.id.path.toPackage.map(p => p.capitalize).mkString(".")}.${i.id.name};" else "").mkString("\n")
  }

  def findImport(id: TypeId): Option[CSharpImport] = {
    imports.find(i => i.id == id)
  }

  def withImport(id: TypeId): String = {
    if (isAmbiguousName(id.name)) {
      id.path.toPackage.map(p => p.capitalize).mkString(".") + "." + id.name
    } else {
      val rec = findImport(id)
      if (rec.isDefined && rec.get.usingName != "") {
        rec.get.usingName
      } else {
        id.name
      }
    }
  }
}

object CSharpImports {
  def apply(imports: List[CSharpImport]): CSharpImports =
    new CSharpImports(imports)
}
