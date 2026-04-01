import { describe, it, expect } from "vitest";
import { Formatter } from "../formatter";

describe("Formatter", () => {
  describe("Time mapping", () => {
    it("should read and write time correctly", () => {
      const expectedTime = "12:34:56.789";
      const date = Formatter.readTime(expectedTime);
      expect(date).toBeInstanceOf(Date);

      const writeResult = Formatter.writeTime(date);
      expect(writeResult).toBe(expectedTime);
    });
  });

  describe("Date mapping", () => {
    it("should read and write date correctly", () => {
      const expectedDate = "2026-04-01";
      const date = Formatter.readDate(expectedDate);
      expect(date).toBeInstanceOf(Date);

      const writeResult = Formatter.writeDate(date);
      expect(writeResult).toBe(expectedDate);
    });
  });

  describe("DateTime mapping", () => {
    it("should strip region context like [Europe/Dublin] during parsing", () => {
      const input = "2026-04-01T11:37:27.123Z[Europe/Dublin]";
      const date = Formatter.readDateTime(input);
      expect(date).toBeInstanceOf(Date);

      const expectedDate = Formatter.readDateTime("2026-04-01T11:37:27.123Z");
      expect(date.getTime()).toBe(expectedDate.getTime());
    });

    it("should properly fall back to standard formatting array", () => {
      const input = "2026-04-01T11:37:27.123Z";
      const date = Formatter.readDateTime(input);
      expect(date).toBeInstanceOf(Date);
    });

    it("should correctly parse with or without UTC mode", () => {
      const input = "2026-04-01T11:37:27.123Z";
      const dateLocal = Formatter.readDateTime(input, false);
      const dateUtc = Formatter.readDateTime(input, true);

      expect(dateLocal).toBeInstanceOf(Date);
      expect(dateUtc).toBeInstanceOf(Date);
      expect(dateLocal.getTime()).toBe(dateUtc.getTime());
    });
  });

  describe("ZoneDateTime mapping", () => {
    it("should read and write ZoneDateTime correctly", () => {
      const input = "2026-04-01T11:37:27.123+03:00";
      const date = Formatter.readZoneDateTime(input);
      expect(date).toBeInstanceOf(Date);

      const written = Formatter.writeZoneDateTime(date);
      expect(written).toMatch(
        /\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}[+-Z]/,
      );
    });
  });

  describe("LocalDateTime mapping", () => {
    it("should read and write LocalDateTime correctly", () => {
      const localStr = "2026-04-01T11:37:27.123";
      const date = Formatter.readLocalDateTime(localStr);
      expect(date).toBeInstanceOf(Date);

      const writeResult = Formatter.writeLocalDateTime(date);
      expect(writeResult).toBe(localStr);
    });
  });

  describe("UTCDateTime mapping", () => {
    it("should read and write UTCDateTime correctly", () => {
      const utcStr = "2026-04-01T11:37:27.123Z";
      const date = Formatter.readUTCDateTime(utcStr);
      expect(date).toBeInstanceOf(Date);

      const writeResult = Formatter.writeUTCDateTime(date);
      expect(writeResult).toBe(utcStr);
    });
  });
});
