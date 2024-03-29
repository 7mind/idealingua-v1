package izumi.idealingua.runtime.circe

import io.circe.{Decoder, DecodingFailure, Encoder, Json}
import izumi.fundamentals.platform.time.IzTime

import java.time._
import java.time.format.{DateTimeFormatter, DateTimeParseException}

trait IRTTimeInstances {
  // Place impls in a single object to avoid massive duplication of codec objects for all inheritors of IRTTimeInstances
  implicit final def decodeInstant: Decoder[Instant] = IRTTimeInstances.decodeInstant
  implicit final def encodeInstant: Encoder[Instant] = IRTTimeInstances.encodeInstant

  implicit final def decodeZoneId: Decoder[ZoneId] = IRTTimeInstances.decodeZoneId
  implicit final def encodeZoneId: Encoder[ZoneId] = IRTTimeInstances.encodeZoneId

  final def decodeLocalDateTime(formatter: DateTimeFormatter): Decoder[LocalDateTime] = IRTTimeInstances.decodeLocalDateTime(formatter)
  final def encodeLocalDateTime(formatter: DateTimeFormatter): Encoder[LocalDateTime] = IRTTimeInstances.encodeLocalDateTime(formatter)

  implicit final def decodeLocalDateTimeDefault: Decoder[LocalDateTime] = IRTTimeInstances.decodeLocalDateTimeDefault
  implicit final def encodeLocalDateTimeDefault: Encoder[LocalDateTime] = IRTTimeInstances.encodeLocalDateTimeDefault

  final def decodeZonedDateTime(formatter: DateTimeFormatter): Decoder[ZonedDateTime] = IRTTimeInstances.decodeZonedDateTime(formatter)
  final def encodeZonedDateTime(formatter: DateTimeFormatter): Encoder[ZonedDateTime] = IRTTimeInstances.encodeZonedDateTime(formatter)

  implicit final def decodeZonedDateTimeDefault: Decoder[ZonedDateTime] = IRTTimeInstances.decodeZonedDateTimeDefault
  implicit final def encodeZonedDateTimeDefault: Encoder[ZonedDateTime] = IRTTimeInstances.encodeZonedDateTimeDefault

  final def decodeOffsetDateTime(formatter: DateTimeFormatter): Decoder[OffsetDateTime] = IRTTimeInstances.decodeOffsetDateTime(formatter)
  final def encodeOffsetDateTime(formatter: DateTimeFormatter): Encoder[OffsetDateTime] = IRTTimeInstances.encodeOffsetDateTime(formatter)

  implicit final def decodeOffsetDateTimeDefault: Decoder[OffsetDateTime] = IRTTimeInstances.decodeOffsetDateTimeDefault
  implicit final def encodeOffsetDateTimeDefault: Encoder[OffsetDateTime] = IRTTimeInstances.encodeOffsetDateTimeDefault

  final def decodeLocalDate(formatter: DateTimeFormatter): Decoder[LocalDate] = IRTTimeInstances.decodeLocalDate(formatter)
  final def encodeLocalDate(formatter: DateTimeFormatter): Encoder[LocalDate] = IRTTimeInstances.encodeLocalDate(formatter)

  implicit final def decodeLocalDateDefault: Decoder[LocalDate] = IRTTimeInstances.decodeLocalDateDefault
  implicit final def encodeLocalDateDefault: Encoder[LocalDate] = IRTTimeInstances.encodeLocalDateDefault

  final def decodeLocalTime(formatter: DateTimeFormatter): Decoder[LocalTime] = IRTTimeInstances.decodeLocalTime(formatter)
  final def encodeLocalTime(formatter: DateTimeFormatter): Encoder[LocalTime] = IRTTimeInstances.encodeLocalTime(formatter)

  implicit final def decodeLocalTimeDefault: Decoder[LocalTime] = IRTTimeInstances.decodeLocalTimeDefault
  implicit final def encodeLocalTimeDefault: Encoder[LocalTime] = IRTTimeInstances.encodeLocalTimeDefault

  final def decodeOffsetTime(formatter: DateTimeFormatter): Decoder[OffsetTime] = IRTTimeInstances.decodeOffsetTime(formatter)
  final def encodeOffsetTime(formatter: DateTimeFormatter): Encoder[OffsetTime] = IRTTimeInstances.encodeOffsetTime(formatter)

  implicit final def decodeOffsetTimeDefault: Decoder[OffsetTime] = IRTTimeInstances.decodeOffsetTimeDefault
  implicit final def encodeOffsetTimeDefault: Encoder[OffsetTime] = IRTTimeInstances.encodeOffsetTimeDefault

  implicit final def decodePeriod: Decoder[Period] = IRTTimeInstances.decodePeriod
  implicit final def encodePeriod: Encoder[Period] = IRTTimeInstances.encodePeriod

  final def decodeYearMonth(formatter: DateTimeFormatter): Decoder[YearMonth] = IRTTimeInstances.decodeYearMonth(formatter)
  final def encodeYearMonth(formatter: DateTimeFormatter): Encoder[YearMonth] = IRTTimeInstances.encodeYearMonth(formatter)

  implicit final def decodeYearMonthDefault: Decoder[YearMonth] = IRTTimeInstances.decodeYearMonthDefault
  implicit final def encodeYearMonthDefault: Encoder[YearMonth] = IRTTimeInstances.encodeYearMonthDefault

  implicit final def decodeDuration: Decoder[Duration] = IRTTimeInstances.decodeDuration
  implicit final def encodeDuration: Encoder[Duration] = IRTTimeInstances.encodeDuration
}

object IRTTimeInstances {
  import izumi.fundamentals.platform.time.IzTime.*

  implicit final val decodeInstant: Decoder[Instant] =
    Decoder.instance {
      c =>
        c.as[String].flatMap {
          s =>
            try Right(Instant.parse(s))
            catch {
              case _: DateTimeParseException => Left(DecodingFailure("Instant", c.history))
            }
        }
    }

  implicit final val encodeInstant: Encoder[Instant] = Encoder.instance(time => Json.fromString(time.toString))

  implicit final val decodeZoneId: Decoder[ZoneId] =
    Decoder.instance {
      c =>
        c.as[String].flatMap {
          s =>
            try Right(ZoneId.of(s))
            catch {
              case _: DateTimeException => Left(DecodingFailure("ZoneId", c.history))
            }
        }
    }

  implicit final val encodeZoneId: Encoder[ZoneId] =
    Encoder[String].contramap(_.getId)

  final def decodeLocalDateTime(formatter: DateTimeFormatter): Decoder[LocalDateTime] =
    Decoder.instance {
      c =>
        c.as[String].flatMap {
          s =>
            try Right(LocalDateTime.parse(s, formatter))
            catch {
              case _: DateTimeParseException => Left(DecodingFailure("LocalDateTime", c.history))
            }
        }
    }

  final def encodeLocalDateTime(formatter: DateTimeFormatter): Encoder[LocalDateTime] =
    Encoder.instance(time => Json.fromString(time.format(formatter)))

  implicit final val decodeLocalDateTimeDefault: Decoder[LocalDateTime] = decodeLocalDateTime(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
  implicit final val encodeLocalDateTimeDefault: Encoder[LocalDateTime] = encodeLocalDateTime(ISO_LOCAL_DATE_TIME_3NANO)

  final def decodeZonedDateTime(formatter: DateTimeFormatter): Decoder[ZonedDateTime] =
    Decoder.instance {
      c =>
        c.as[String].flatMap {
          s =>
            try {
              val parsed = ZonedDateTime.parse(s, formatter)
              if (parsed.getZone.getId == "Z") {
                Right(parsed.withZoneSameLocal(IzTime.TZ_UTC))
              } else {
                Right(parsed)
              }
            } catch {
              case _: DateTimeParseException => Left(DecodingFailure("ZonedDateTime", c.history))
            }
        }
    }

  // TODO: this is a temporary solution!
  final def encodeZonedDateTime(formatter: DateTimeFormatter): Encoder[ZonedDateTime] =
    Encoder.instance(time => Json.fromString(time.withZoneSameInstant(IzTime.TZ_UTC).toOffsetDateTime.format(formatter)))

  implicit final val decodeZonedDateTimeDefault: Decoder[ZonedDateTime] = decodeZonedDateTime(DateTimeFormatter.ISO_ZONED_DATE_TIME)
  implicit final val encodeZonedDateTimeDefault: Encoder[ZonedDateTime] = encodeZonedDateTime(ISO_ZONED_DATE_TIME_3NANO)

  final def decodeOffsetDateTime(formatter: DateTimeFormatter): Decoder[OffsetDateTime] =
    Decoder.instance {
      c =>
        c.as[String].flatMap {
          s =>
            try Right(OffsetDateTime.parse(s, formatter))
            catch {
              case _: DateTimeParseException => Left(DecodingFailure("OffsetDateTime", c.history))
            }
        }
    }

  final def encodeOffsetDateTime(formatter: DateTimeFormatter): Encoder[OffsetDateTime] =
    Encoder.instance(time => Json.fromString(time.format(formatter)))

  implicit final val decodeOffsetDateTimeDefault: Decoder[OffsetDateTime] = decodeOffsetDateTime(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
  implicit final val encodeOffsetDateTimeDefault: Encoder[OffsetDateTime] = encodeOffsetDateTime(ISO_OFFSET_DATE_TIME_3NANO)

  final def decodeLocalDate(formatter: DateTimeFormatter): Decoder[LocalDate] =
    Decoder.instance {
      c =>
        c.as[String].flatMap {
          s =>
            try Right(LocalDate.parse(s, formatter))
            catch {
              case _: DateTimeParseException => Left(DecodingFailure("LocalDate", c.history))
            }
        }
    }

  final def encodeLocalDate(formatter: DateTimeFormatter): Encoder[LocalDate] =
    Encoder.instance(time => Json.fromString(time.format(formatter)))

  implicit final val decodeLocalDateDefault: Decoder[LocalDate] = decodeLocalDate(DateTimeFormatter.ISO_LOCAL_DATE)
  implicit final val encodeLocalDateDefault: Encoder[LocalDate] = encodeLocalDate(ISO_LOCAL_DATE)

  final def decodeLocalTime(formatter: DateTimeFormatter): Decoder[LocalTime] =
    Decoder.instance {
      c =>
        c.as[String].flatMap {
          s =>
            try Right(LocalTime.parse(s, formatter))
            catch {
              case _: DateTimeParseException => Left(DecodingFailure("LocalTime", c.history))
            }
        }
    }

  final def encodeLocalTime(formatter: DateTimeFormatter): Encoder[LocalTime] =
    Encoder.instance(time => Json.fromString(time.format(formatter)))

  implicit final val decodeLocalTimeDefault: Decoder[LocalTime] = decodeLocalTime(DateTimeFormatter.ISO_LOCAL_TIME)
  implicit final val encodeLocalTimeDefault: Encoder[LocalTime] = encodeLocalTime(ISO_LOCAL_TIME_3NANO)

  final def decodeOffsetTime(formatter: DateTimeFormatter): Decoder[OffsetTime] =
    Decoder.instance {
      c =>
        c.as[String].flatMap {
          s =>
            try Right(OffsetTime.parse(s, formatter))
            catch {
              case _: DateTimeParseException => Left(DecodingFailure("OffsetTime", c.history))
            }
        }
    }

  final def encodeOffsetTime(formatter: DateTimeFormatter): Encoder[OffsetTime] =
    Encoder.instance(time => Json.fromString(time.format(formatter)))

  implicit final val decodeOffsetTimeDefault: Decoder[OffsetTime] = decodeOffsetTime(DateTimeFormatter.ISO_OFFSET_TIME)
  implicit final val encodeOffsetTimeDefault: Encoder[OffsetTime] = encodeOffsetTime(ISO_OFFSET_TIME_3NANO)

  implicit final val decodePeriod: Decoder[Period] = Decoder.instance {
    c =>
      c.as[String].flatMap {
        s =>
          try Right(Period.parse(s))
          catch {
            case _: DateTimeParseException => Left(DecodingFailure("Period", c.history))
          }
      }
  }

  implicit final val encodePeriod: Encoder[Period] = Encoder.instance {
    period =>
      Json.fromString(period.toString)
  }

  final def decodeYearMonth(formatter: DateTimeFormatter): Decoder[YearMonth] =
    Decoder.instance {
      c =>
        c.as[String].flatMap {
          s =>
            try Right(YearMonth.parse(s, formatter))
            catch {
              case _: DateTimeParseException => Left(DecodingFailure("YearMonth", c.history))
            }
        }
    }

  final def encodeYearMonth(formatter: DateTimeFormatter): Encoder[YearMonth] =
    Encoder.instance(time => Json.fromString(time.format(formatter)))

  private final val yearMonthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

  implicit final val decodeYearMonthDefault: Decoder[YearMonth] = decodeYearMonth(yearMonthFormatter)
  implicit final val encodeYearMonthDefault: Encoder[YearMonth] = encodeYearMonth(yearMonthFormatter)

  implicit final val decodeDuration: Decoder[Duration] =
    Decoder.instance {
      c =>
        c.as[String].flatMap {
          s =>
            try Right(Duration.parse(s))
            catch {
              case _: DateTimeParseException => Left(DecodingFailure("Duration", c.history))
            }
        }
    }

  implicit final val encodeDuration: Encoder[Duration] =
    Encoder.instance(duration => Json.fromString(duration.toString))
}
