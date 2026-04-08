package izumi.idealingua.compiler

import org.scalatest.wordspec.AnyWordSpec

class CommitHashResolverTest extends AnyWordSpec {
  private val hash = "abcdef1234567890abcdef1234567890abcdef12"

  "CommitHashResolver" should {
    "resolve <commit> to first 7 chars by default" in {
      assert(CommitHashResolver.resolveQualifier("build.<commit>", hash) == "build.abcdef1")
    }

    "resolve <commitN> to first N chars" in {
      assert(CommitHashResolver.resolveQualifier("build.<commit10>", hash) == "build.abcdef1234")
      assert(CommitHashResolver.resolveQualifier("build.<commit3>", hash) == "build.abc")
      assert(CommitHashResolver.resolveQualifier("<commit40>", hash) == hash)
    }

    "handle multiple commit templates in one string" in {
      assert(CommitHashResolver.resolveQualifier("<commit>-<commit10>", hash) == "abcdef1-abcdef1234")
    }

    "leave string unchanged when no template present" in {
      assert(CommitHashResolver.resolveQualifier("SNAPSHOT", hash) == "SNAPSHOT")
      assert(CommitHashResolver.resolveQualifier("build.0", hash) == "build.0")
    }

    "handle N larger than hash length" in {
      assert(CommitHashResolver.resolveQualifier("<commit100>", "abcdef") == "abcdef")
    }

    "handle 'unknown' hash" in {
      assert(CommitHashResolver.resolveQualifier("build.<commit>", "unknown") == "build.unknown")
    }

    "detect commit template presence" in {
      assert(CommitHashResolver.containsCommitTemplate("<commit>"))
      assert(CommitHashResolver.containsCommitTemplate("build.<commit10>"))
      assert(CommitHashResolver.containsCommitTemplate("prefix-<commit3>-suffix"))
      assert(!CommitHashResolver.containsCommitTemplate("SNAPSHOT"))
      assert(!CommitHashResolver.containsCommitTemplate("build.0"))
      assert(!CommitHashResolver.containsCommitTemplate("commit"))
    }

    "resolve actual git commit hash" in {
      val resolved = CommitHashResolver.resolveCommitHash()
      assert(resolved == "unknown" || resolved.matches("[0-9a-f]{40}"))
    }
  }
}
