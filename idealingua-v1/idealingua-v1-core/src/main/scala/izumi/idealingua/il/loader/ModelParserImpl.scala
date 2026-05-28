package izumi.idealingua.il.loader

import izumi.idealingua.il.parser.{IDLParser, IDLParserContext}
import izumi.idealingua.model.loader._
import izumi.idealingua.util.Parallel
import fastparse._

class ModelParserImpl(parallel: Parallel = Parallel.Default) extends ModelParser {
  def parseModels(files: Map[FSPath, String]): ParsedModels = ParsedModels {
    parallel.parMap(files.toSeq) {
      case (file, content) =>
        new IDLParser(IDLParserContext(file)).parseModel(content) match {
          case Parsed.Success(value, _) =>
            ModelParsingResult.Success(file, value)
          case f: Parsed.Failure =>
            ModelParsingResult.Failure(file, s"Failed to parse model $file: ${f.msg}")
        }
    }
  }

  def parseDomains(files: Map[FSPath, String]): ParsedDomains = ParsedDomains {
    parallel.parMap(files.toSeq) {
      case (file, content) =>
        new IDLParser(IDLParserContext(file)).parseDomain(content) match {
          case Parsed.Success(value, _) =>
            DomainParsingResult.Success(file, value)
          case f: Parsed.Failure =>
            DomainParsingResult.Failure(file, s"Failed to parse domain $file: ${f.trace().msg}")
        }
    }
  }
}
