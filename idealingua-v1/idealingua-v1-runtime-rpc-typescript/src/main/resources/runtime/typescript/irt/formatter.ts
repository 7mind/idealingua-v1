import { Temporal } from "@js-temporal/polyfill";

export class Formatter {
  public static readTime(value: string) {
    const plainTime = Temporal.PlainTime.from(value);
    const today = Temporal.Now.plainDateISO();
    return new Date(
      today
        .toPlainDateTime(plainTime)
        .toZonedDateTime(Temporal.Now.timeZoneId()).epochMilliseconds,
    );
  }

  public static writeTime(value: Date) {
    const zdt = Temporal.Instant.fromEpochMilliseconds(
      value.getTime(),
    ).toZonedDateTimeISO(Temporal.Now.timeZoneId());
    return zdt.toPlainTime().toString({ fractionalSecondDigits: 3 });
  }

  public static readDate(value: string) {
    const plainDate = Temporal.PlainDate.from(value);
    return new Date(
      plainDate
        .toPlainDateTime({ hour: 0, minute: 0, second: 0 })
        .toZonedDateTime(Temporal.Now.timeZoneId()).epochMilliseconds,
    );
  }

  public static writeDate(value: Date) {
    const zdt = Temporal.Instant.fromEpochMilliseconds(
      value.getTime(),
    ).toZonedDateTimeISO(Temporal.Now.timeZoneId());
    return zdt.toPlainDate().toString();
  }

  public static readDateTime(value: string, utcMode: boolean = false) {
    const regionIndex = value.indexOf("[");
    if (regionIndex >= 0) {
      // For the time being, we just ignore [Europe/Dublin] kind of regions
      value = value.substring(0, regionIndex);
    }

    const timeZone = utcMode ? "UTC" : Temporal.Now.timeZoneId();
    let epochMs: number;

    if (value.includes("Z") || value.match(/[+-]\d{2}:\d{2}$/)) {
      epochMs = Temporal.Instant.from(value).epochMilliseconds;
    } else {
      epochMs =
        Temporal.PlainDateTime.from(value).toZonedDateTime(
          timeZone,
        ).epochMilliseconds;
    }

    return new Date(epochMs);
  }

  public static readZoneDateTime(value: string) {
    return Formatter.readDateTime(value);
  }

  public static writeZoneDateTime(value: Date) {
    const zdt = Temporal.Instant.fromEpochMilliseconds(
      value.getTime(),
    ).toZonedDateTimeISO(Temporal.Now.timeZoneId());
    return zdt.toString({ fractionalSecondDigits: 3, timeZoneName: "never" });
  }

  public static readLocalDateTime(value: string) {
    return Formatter.readDateTime(value);
  }

  public static writeLocalDateTime(value: Date) {
    const zdt = Temporal.Instant.fromEpochMilliseconds(
      value.getTime(),
    ).toZonedDateTimeISO(Temporal.Now.timeZoneId());
    return zdt.toPlainDateTime().toString({ fractionalSecondDigits: 3 });
  }

  public static readUTCDateTime(value: string) {
    return Formatter.readDateTime(value, true);
  }

  public static writeUTCDateTime(value: Date) {
    const instant = Temporal.Instant.fromEpochMilliseconds(value.getTime());
    return instant.toString({ fractionalSecondDigits: 3 });
  }
}
