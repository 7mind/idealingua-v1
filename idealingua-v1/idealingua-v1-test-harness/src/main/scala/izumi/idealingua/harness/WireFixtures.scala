package izumi.idealingua.harness

import java.nio.file.{Files, Path}
import java.util.stream.Collectors
import scala.jdk.CollectionConverters._

/**
  * Fixture loader for Layer B wire-byte tests.
  *
  * Directory convention: `<root>/<wireId>/<scenario>.json`
  * where `<root>` is already language-specific (e.g. `wire-fixtures/scala/`).
  * The `wireId` directory name may contain dots (e.g. `idltest.dtofields.Point`).
  * Non-`.json` siblings (e.g. `*.notes.md`) are silently ignored.
  */
private[harness] object WireFixtures {

  final case class FixtureFile(
    wireId: String,
    scenario: String,
    bytes: Array[Byte],
    file: Path,
  )

  def load(root: Path): Seq[FixtureFile] = {
    if (!Files.exists(root)) return Seq.empty

    val stream = Files.walk(root, 2)
    try {
      stream
        .collect(Collectors.toList[Path]())
        .asScala
        .filter(p => Files.isRegularFile(p) && p.getFileName.toString.endsWith(".json"))
        .flatMap { file =>
          val parent = file.getParent
          if (parent == null || parent == root) None
          else {
            val wireId  = parent.getFileName.toString
            val scenFn  = file.getFileName.toString
            val scenario = scenFn.stripSuffix(".json")
            Some(FixtureFile(wireId, scenario, Files.readAllBytes(file), file))
          }
        }
        .sortBy(f => (f.wireId, f.scenario))
        .toSeq
    } finally {
      stream.close()
    }
  }
}
