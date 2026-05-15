
// moment's TS .d.ts declares `export = moment`, which interacts poorly with
// every TS import form when you need to compile against a matrix of (module,
// esModuleInterop) settings:
//
//   - CJS, esModuleInterop=false:  `* as moment`  → namespace IS the function.
//   - CJS, esModuleInterop=true:   `* as moment`  → `{default: fn}` (NOT callable).
//   - ESM, esModuleInterop=false:  `* as moment`  → namespace; `moment(...)` rejects at runtime.
//   - ESM, any interop:            `moment = require(...)` → TS1202 (CJS-only syntax).
//
// This module is consumed under at least three combos: the upstream IRT
// publish (tsconfig.json — CJS+interop=false AND tsconfig.es.json — ESM+
// interop=false), the regression-harness sample-app build (CJS+interop=false),
// and downstream npm consumers (any combination). No single import form is
// callable across all of them. Resolve at runtime: take the function from
// either the namespace itself (interop=false case) or its `.default` (interop=
// true case). The cast restores moment's static-typed call signature.
import * as momentNs from 'moment';
const moment = ((momentNs as any).default ?? momentNs) as typeof import('moment');

export class Formatter {
    public static readonly DATETIME_FORMATS = [
        'YYYY-MM-DDTHH:mm:ssZ',
        'YYYY-MM-DDTHH:mm:ss.SZ',
        'YYYY-MM-DDTHH:mm:ss.SSZ',
        'YYYY-MM-DDTHH:mm:ss.SSSZ',
        'YYYY-MM-DDTHH:mm:ss.SSSSZ',
        'YYYY-MM-DDTHH:mm:ss.SSSSSZ',
        'YYYY-MM-DDTHH:mm:ss.SSSSSSZ',
        'YYYY-MM-DDTHH:mm:ss.SSSSSSSZ',
        'YYYY-MM-DDTHH:mm:ss.SSSSSSSSZ',
        'YYYY-MM-DDTHH:mm:ss.SSSSSSSSSZ',
        'YYYY-MM-DDTHH:mm:ss',
        'YYYY-MM-DDTHH:mm:ss.S',
        'YYYY-MM-DDTHH:mm:ss.SS',
        'YYYY-MM-DDTHH:mm:ss.SSS',
        'YYYY-MM-DDTHH:mm:ss.SSSS',
        'YYYY-MM-DDTHH:mm:ss.SSSSS',
        'YYYY-MM-DDTHH:mm:ss.SSSSSS',
        'YYYY-MM-DDTHH:mm:ss.SSSSSSS',
        'YYYY-MM-DDTHH:mm:ss.SSSSSSSS',
        'YYYY-MM-DDTHH:mm:ss.SSSSSSSSS',
    ];

    public static readTime(value: string): Date {
        return moment(value, 'HH:mm:ss.SSS').toDate();
    }

    public static writeTime(value: Date): string {
        return moment(value).format('HH:mm:ss.SSS');
    }

    public static readDate(value: string): Date {
        return moment(value, 'YYYY-MM-DD').toDate();
    }

    public static writeDate(value: Date): string {
        return moment(value).format('YYYY-MM-DD');
    }

    public static readDateTime(value: string, utc: boolean = false): Date {
        const regionIndex = value.indexOf('[');
        if (regionIndex >= 0) {
            // For the time being, we just ignore [Europe/Dublin] kind of regions
            value = value.substring(0, regionIndex);
        }

        const res = moment(value, Formatter.DATETIME_FORMATS);
        return utc ? res.utc().toDate() : res.toDate();
    }

    public static readZoneDateTime(value: string): Date {
        return Formatter.readDateTime(value);
    }

    public static writeZoneDateTime(value: Date): string {
        return moment(value).format('YYYY-MM-DDTHH:mm:ss.SSSZ');
    }

    public static readLocalDateTime(value: string): Date {
        return Formatter.readDateTime(value);
    }

    public static writeLocalDateTime(value: Date): string {
        return moment(value).format('YYYY-MM-DDTHH:mm:ss.SSS');
    }

    public static readUTCDateTime(value: string): Date {
        return Formatter.readDateTime(value, true);
    }

    public static writeUTCDateTime(value: Date): string {
        return moment(value).utc().format('YYYY-MM-DDTHH:mm:ss.SSSZ');
    }
}
