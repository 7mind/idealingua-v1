package regression_harness

import java.nio.file.Path

/** Resolved compiler reference: everything an adapter needs to materialize
 *  a project against the matching compiler version.
 *
 *  - `launcher`        — staged sbt-native-packager bin (idlc).
 *  - `runtimeRepoPath` — local maven/ivy repository holding `publishLocal`-ed
 *                        `idealingua-v1-runtime-rpc-*` (and `-model`) artifacts.
 *                        For `self`, this is the user's `~/.ivy2/local` (we
 *                        publish into ivy2Local). For `git:`, this is a
 *                        per-sha sandboxed local maven directory.
 *  - `runtimeRepoUri`  — scala-cli `using repository "<uri>"` form. For
 *                        ivy2Local this is the string `"ivy2Local"`. For a
 *                        per-sha m2 dir this is `file:///abs/path/to/m2`.
 *  - `runtimeVersion`  — Maven coordinate version (e.g. `1.4.20-SNAPSHOT`).
 */
final case class IdlcResolution(
  launcher:        Path,
  runtimeRepoPath: Path,
  runtimeRepoUri:  String,
  runtimeVersion:  String,
)
