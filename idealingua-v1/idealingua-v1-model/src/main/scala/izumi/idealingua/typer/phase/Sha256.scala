package izumi.idealingua.typer.phase

import scala.collection.mutable

/** Minimal pure-Scala SHA-256 implementation.
  *
  * Required because `java.security.MessageDigest` is unavailable on
  * Scala.js. The implementation follows FIPS 180-4 §6.2 directly; no
  * defensive padding tricks or platform-specific shortcuts.  Determinism is
  * the only property that matters here (the IR fingerprints feed downstream
  * stability checks, not security boundaries).
  */
private[phase] final class Sha256 {

  import Sha256._

  private val state: Array[Int] = Array(
    0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a,
    0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19,
  )
  private val buffer: mutable.ArrayBuffer[Byte] = mutable.ArrayBuffer.empty
  private var totalBytes: Long = 0L

  def update(b: Byte): Unit = {
    buffer += b
    totalBytes += 1
    drainBlocks()
  }

  def update(bs: Array[Byte]): Unit = {
    var i = 0
    while (i < bs.length) {
      buffer += bs(i)
      i += 1
    }
    totalBytes += bs.length
    drainBlocks()
  }

  private def drainBlocks(): Unit = {
    while (buffer.length >= 64) {
      val block = new Array[Byte](64)
      var i = 0
      while (i < 64) { block(i) = buffer(i); i += 1 }
      buffer.remove(0, 64)
      processBlock(block)
    }
  }

  def digest(): Array[Byte] = {
    val bitLength = totalBytes * 8L
    // Append 0x80, then 0x00 until len % 64 == 56, then 8-byte big-endian bit-length.
    buffer += 0x80.toByte
    while (buffer.length % 64 != 56) buffer += 0x00.toByte
    var k = 7
    while (k >= 0) {
      buffer += ((bitLength >>> (k * 8)) & 0xff).toByte
      k -= 1
    }
    drainBlocks()
    require(buffer.isEmpty)

    val out = new Array[Byte](32)
    var j = 0
    while (j < 8) {
      val v = state(j)
      out(j * 4 + 0) = ((v >>> 24) & 0xff).toByte
      out(j * 4 + 1) = ((v >>> 16) & 0xff).toByte
      out(j * 4 + 2) = ((v >>>  8) & 0xff).toByte
      out(j * 4 + 3) = ( v         & 0xff).toByte
      j += 1
    }
    out
  }

  private def processBlock(block: Array[Byte]): Unit = {
    val w = new Array[Int](64)
    var t = 0
    while (t < 16) {
      val o = t * 4
      w(t) = ((block(o) & 0xff) << 24) |
             ((block(o + 1) & 0xff) << 16) |
             ((block(o + 2) & 0xff) << 8) |
             (block(o + 3) & 0xff)
      t += 1
    }
    while (t < 64) {
      val s0 = rotr(w(t - 15), 7) ^ rotr(w(t - 15), 18) ^ (w(t - 15) >>> 3)
      val s1 = rotr(w(t - 2), 17) ^ rotr(w(t - 2), 19) ^ (w(t - 2) >>> 10)
      w(t) = w(t - 16) + s0 + w(t - 7) + s1
      t += 1
    }

    var a = state(0); var b = state(1); var c = state(2); var d = state(3)
    var e = state(4); var f = state(5); var g = state(6); var h = state(7)

    var i = 0
    while (i < 64) {
      val S1 = rotr(e, 6) ^ rotr(e, 11) ^ rotr(e, 25)
      val ch = (e & f) ^ ((~e) & g)
      val temp1 = h + S1 + ch + K(i) + w(i)
      val S0 = rotr(a, 2) ^ rotr(a, 13) ^ rotr(a, 22)
      val maj = (a & b) ^ (a & c) ^ (b & c)
      val temp2 = S0 + maj
      h = g
      g = f
      f = e
      e = d + temp1
      d = c
      c = b
      b = a
      a = temp1 + temp2
      i += 1
    }

    state(0) += a; state(1) += b; state(2) += c; state(3) += d
    state(4) += e; state(5) += f; state(6) += g; state(7) += h
  }

  private def rotr(x: Int, n: Int): Int = (x >>> n) | (x << (32 - n))
}

private[phase] object Sha256 {

  private val K: Array[Int] = Array(
    0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
    0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
    0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
    0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
    0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
    0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
    0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
    0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2,
  )
}
