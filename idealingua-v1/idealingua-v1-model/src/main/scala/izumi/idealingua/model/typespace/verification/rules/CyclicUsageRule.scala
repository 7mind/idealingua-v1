package izumi.idealingua.model.typespace.verification.rules

import izumi.idealingua.model.common.{Builtin, TypeId}
import izumi.idealingua.model.il.ast.typed.TypeDef.*
import izumi.idealingua.model.il.ast.typed.{Field, TypeDef}
import izumi.idealingua.model.problems.IDLDiagnostics
import izumi.idealingua.model.problems.TypespaceError.CyclicUsage
import izumi.idealingua.model.typespace.Typespace
import izumi.idealingua.model.typespace.verification.VerificationRule

import scala.collection.mutable

object CyclicUsageRule extends VerificationRule {
  override def verify(ts: Typespace): IDLDiagnostics = IDLDiagnostics {
    val verifier = new Queries(ts)
    ts.domain.types.flatMap {
      t =>
        val cycles = verifier.verify(t)
        if (cycles.nonEmpty) {
          Seq(CyclicUsage(t.id, cycles))
        } else {
          Seq.empty
        }
    }
  }

  class Queries(ts: Typespace) {
    def verify(toVerify: TypeDef): Set[TypeId] = {
      val verified = mutable.Set.empty[TypeId]

      def extractTypeCycles(definition: TypeDef, ignoreFields: Set[Field] = Set.empty): Set[TypeId] = {
        def extractFieldCycles(name: String, typeId: TypeId): Set[TypeId] = {
          if (toVerify.id == typeId) {
            Set(definition.id)
          } else if (ignoreFields.exists(f => f.typeId == typeId && f.name == name) || verified.contains(typeId)) {
            Set.empty
          } else {
            extractTypeCycles(ts.apply(typeId))
          }
        }

        verified.add(definition.id)

        definition match {
          case id: Identifier =>
            id.fields.filterNot(_.typeId.isInstanceOf[Builtin]).flatMap(f => extractFieldCycles(f.name, f.typeId)).toSet

          case structure: WithStructure =>
            val removedFields = structure.struct.removedFields.toSet
            val fields = structure.struct.fields.filterNot(_.typeId.isInstanceOf[Builtin]).flatMap(f => extractFieldCycles(f.name, f.typeId)).toSet
            val interfaceFields = structure.struct.superclasses.interfaces.flatMap(t => extractTypeCycles(ts.apply(t), removedFields)).toSet
            val conceptsFields = structure.struct.superclasses.concepts.flatMap(t => extractTypeCycles(ts.apply(t), removedFields)).toSet
            fields ++ interfaceFields ++ conceptsFields

          case _: Enumeration => Set.empty

          case _: Alias => Set.empty

          // skip adt check, it will be verified in top level
          case adt: Adt =>
            val membersCycles = adt.alternatives.filterNot(_.typeId.isInstanceOf[Builtin]).map(_.typeId).map(tpe => extractTypeCycles(ts.apply(tpe)))
            val issues = membersCycles.filter(_.nonEmpty)
            if (membersCycles.size == issues.size) {
              issues.toSet.flatten
            } else {
              Set.empty
            }
        }
      }

      toVerify match {
        case structure: WithStructure => extractTypeCycles(structure)
        case i: Identifier => extractTypeCycles(i)
        case adt: Adt => extractTypeCycles(adt)
        case _: Alias => Set.empty
        case _: Enumeration => Set.empty

      }
    }
  }
}
