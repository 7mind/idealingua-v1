package regression_harness

import java.nio.file.Path

/** Per-language build+run adapter.
 *
 *  Contract:
 *    materialize a working directory containing
 *      - the language-specific build descriptor (e.g. scala-cli using-directives),
 *      - the generated sources from `genDir`,
 *      - the sample app dropped at `sampleApp`,
 *    then build and execute, capturing stdout into `rawOut`.
 */
trait LangAdapter {

  /** Build and run; return Right(()) on success, Left(message) on failure.
   *  Implementations MUST capture combined stdout into `rawOut` even on failure
   *  if any partial output is available; otherwise leave it empty.
   */
  def buildAndRun(
    workDir:   Path,
    genDir:    Path,
    sampleApp: Path,
    rawOut:    Path,
  ): Either[String, Unit]
}
