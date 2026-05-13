package regression_harness

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}

/** Line-by-line diff over canonical NDJSON streams.
 *
 *  Both inputs MUST be Canonicalize-emitted (sorted, normalized whitespace).
 *  We pair lines by (wireId, scenario); a missing key on either side is a
 *  divergence of type `OnlyOld` / `OnlyNew`; matching keys with differing
 *  payloads are `JsonDiffer`.
 */
object Diff {

  case class Entry(wireId: String, scenario: String, json: String)
  sealed trait Divergence { def key: (String, String) }
  case class OnlyOld(key: (String, String), old: String) extends Divergence
  case class OnlyNew(key: (String, String), neu: String) extends Divergence
  case class JsonDiffer(key: (String, String), old: String, neu: String) extends Divergence

  case class Report(
    totalLines:   Int,
    oldOnly:      Int,
    newOnly:      Int,
    differs:      Int,
    divergences:  Vector[Divergence],
  )

  def compare(oldNdjson: Path, newNdjson: Path): Report = {
    val a = read(oldNdjson)
    val b = read(newNdjson)
    val keysA = a.map(e => (e.wireId, e.scenario) -> e.json).toMap
    val keysB = b.map(e => (e.wireId, e.scenario) -> e.json).toMap
    val all   = (keysA.keySet ++ keysB.keySet).toVector.sorted

    val divs = Vector.newBuilder[Divergence]
    var oldOnly, newOnly, differ = 0
    all.foreach { k =>
      (keysA.get(k), keysB.get(k)) match {
        case (Some(x), Some(y)) if x == y => ()
        case (Some(x), Some(y))           => divs += JsonDiffer(k, x, y); differ += 1
        case (Some(x), None)              => divs += OnlyOld(k, x);       oldOnly += 1
        case (None,    Some(y))           => divs += OnlyNew(k, y);       newOnly += 1
        case (None,    None)              => ()
      }
    }

    Report(
      totalLines  = all.size,
      oldOnly     = oldOnly,
      newOnly     = newOnly,
      differs     = differ,
      divergences = divs.result(),
    )
  }

  def writeReports(report: Report, outDir: Path, format: String): Unit = {
    Files.createDirectories(outDir)

    if (format == "human" || format == "both") {
      val human = renderHuman(report)
      Files.writeString(outDir.resolve("report.txt"), human)
      System.out.println(human)
    }
    if (format == "json" || format == "both") {
      val js = renderJson(report)
      Files.writeString(outDir.resolve("report.json"), js)
    }
  }

  private def renderHuman(r: Report): String = {
    val sb = new StringBuilder
    sb.append(s"=== idl-regress report ===\n")
    sb.append(s"  total keys:    ${r.totalLines}\n")
    sb.append(s"  divergences:   ${r.divergences.size}\n")
    sb.append(s"    only in old: ${r.oldOnly}\n")
    sb.append(s"    only in new: ${r.newOnly}\n")
    sb.append(s"    differ:      ${r.differs}\n")
    if (r.divergences.isEmpty) {
      sb.append("\nOK: zero divergences.\n")
    } else {
      sb.append("\n--- divergences ---\n")
      r.divergences.foreach {
        case OnlyOld((w,s), j)   => sb.append(s"[-] $w\t$s\n    old: $j\n")
        case OnlyNew((w,s), j)   => sb.append(s"[+] $w\t$s\n    new: $j\n")
        case JsonDiffer((w,s),x,y) =>
          sb.append(s"[~] $w\t$s\n    old: $x\n    new: $y\n")
      }
    }
    sb.toString
  }

  private def renderJson(r: Report): String = {
    val sb = new StringBuilder
    sb.append("{\"total\":").append(r.totalLines)
      .append(",\"only_old\":").append(r.oldOnly)
      .append(",\"only_new\":").append(r.newOnly)
      .append(",\"differs\":").append(r.differs)
      .append(",\"divergences\":[")
    var first = true
    r.divergences.foreach { d =>
      if (!first) sb.append(','); first = false
      d match {
        case OnlyOld((w,s), j)     => writeObj(sb, "only_old", w, s, "old" -> j)
        case OnlyNew((w,s), j)     => writeObj(sb, "only_new", w, s, "new" -> j)
        case JsonDiffer((w,s),x,y) => writeObj(sb, "differ",   w, s, "old" -> x, "new" -> y)
      }
    }
    sb.append("]}\n")
    sb.toString
  }

  private def writeObj(sb: StringBuilder, kind: String, w: String, s: String, payload: (String, String)*): Unit = {
    sb.append("{\"kind\":\"").append(kind).append("\"")
      .append(",\"wireId\":").append(jstr(w))
      .append(",\"scenario\":").append(jstr(s))
    payload.foreach { case (k, v) =>
      sb.append(",\"").append(k).append("\":").append(jstr(v))
    }
    sb.append('}')
  }

  private def jstr(s: String): String = {
    val sb = new StringBuilder
    sb.append('"')
    s.foreach {
      case '"'  => sb.append("\\\"")
      case '\\' => sb.append("\\\\")
      case '\b' => sb.append("\\b")
      case '\f' => sb.append("\\f")
      case '\n' => sb.append("\\n")
      case '\r' => sb.append("\\r")
      case '\t' => sb.append("\\t")
      case c if c < 0x20 => sb.append("\\u%04x".format(c.toInt))
      case c    => sb.append(c)
    }
    sb.append('"')
    sb.toString
  }

  private def read(p: Path): Vector[Entry] = {
    val src = new String(Files.readAllBytes(p), StandardCharsets.UTF_8)
    src.linesIterator.flatMap { raw =>
      val line = raw.stripLineEnd
      if (line.isEmpty) None
      else {
        val cols = line.split('\t')
        if (cols.length < 3) None
        else Some(Entry(cols(0), cols(1), cols.drop(2).mkString("\t")))
      }
    }.toVector
  }
}
