import dayjs from "dayjs";
import customParseFormat from "dayjs/plugin/customParseFormat";
import utc from "dayjs/plugin/utc";

dayjs.extend(customParseFormat);
dayjs.extend(utc);

export class Formatter {
  public static readTime(value: string): Date {
    return dayjs(value, "HH:mm:ss.SSS").toDate();
  }

  public static writeTime(value: Date): string {
    return dayjs(value).format("HH:mm:ss.SSS");
  }

  public static readDate(value: string): Date {
    return dayjs(value, "YYYY-MM-DD").toDate();
  }

  public static writeDate(value: Date): string {
    return dayjs(value).format("YYYY-MM-DD");
  }

  public static readDateTime(value: string, utcMode: boolean = false): Date {
    const regionIndex = value.indexOf("[");
    if (regionIndex >= 0) {
      // For the time being, we just ignore [Europe/Dublin] kind of regions
      value = value.substring(0, regionIndex);
    }

    const dt = dayjs(value);
    if (!dt.isValid()) {
      throw new Error(`Invalid date format for value ${value}`);
    }

    return utcMode ? dt.utc().toDate() : dt.toDate();
  }

  public static readZoneDateTime(value: string): Date {
    return Formatter.readDateTime(value);
  }

  public static writeZoneDateTime(value: Date): string {
    return dayjs(value).format("YYYY-MM-DDTHH:mm:ss.SSSZ");
  }

  public static readLocalDateTime(value: string): Date {
    return Formatter.readDateTime(value);
  }

  public static writeLocalDateTime(value: Date): string {
    return dayjs(value).format("YYYY-MM-DDTHH:mm:ss.SSS");
  }

  public static readUTCDateTime(value: string): Date {
    return Formatter.readDateTime(value, true);
  }

  public static writeUTCDateTime(value: Date): string {
    return dayjs(value).utc().format("YYYY-MM-DDTHH:mm:ss.SSS[Z]");
  }
}
