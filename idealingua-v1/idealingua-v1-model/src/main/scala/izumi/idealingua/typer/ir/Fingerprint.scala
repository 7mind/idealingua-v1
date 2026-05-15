package izumi.idealingua.typer.ir

import scodec.bits.ByteVector

/** SHA-256 digest of a type or domain.
  *
  * Uses `scodec.bits.ByteVector` rather than `Array[Byte]` because `Array[Byte]`
  * uses reference equality in Scala — two arrays with identical content are not
  * `==` equal. `ByteVector` provides structural equality, is immutable, and
  * cross-builds cleanly on Scala 2.13 + 3.x (see tasks.md C3/Q8, F1).
  */
final case class Fingerprint(value: ByteVector)
