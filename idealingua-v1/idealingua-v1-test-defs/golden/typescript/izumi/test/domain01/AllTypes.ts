// Auto-generated, any modifications may be overwritten in the future.
import { Formatter } from '../../../irt';
import {
    GoAliasEnumTest
} from './GoAliasEnumTest';

// AllTypes Interface
export interface AllTypes {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): AllTypesStructSerialized;

    b: boolean;
    s: string;
    int8: number;
    int16: number;
    int32: number;
    int64: number;
    f: number;
    d: number;
    uuid: string;
    ts: Date;
    tslocal: Date;
    tsuni: Date;
    time: Date;
    date: Date;
    uint8: number;
    uint16: number;
    uint32: number;
    uint64: number;
    list: AllTypes[];
    another: AllTypes[];
    selfMap: {[key: string]: AllTypes};
    enumMap: {[key: string]: GoAliasEnumTest};
    option: AllTypes | undefined;
    selfSet: AllTypes[];
    optionDate: Date | undefined;
    optionTime: Date | undefined;
}

export interface AllTypesStructSerialized {
    b: boolean;
    s: string;
    int8: number;
    int16: number;
    int32: number;
    int64: number;
    f: number;
    d: number;
    uuid: string;
    ts: string;
    tslocal: string;
    tsuni: string;
    time: string;
    date: string;
    uint8: number;
    uint16: number;
    uint32: number;
    uint64: number;
    list: {[key: string]: AllTypesStructSerialized}[];
    another: {[key: string]: AllTypesStructSerialized}[];
    selfMap: {[key: string]: {[key: string]: AllTypesStructSerialized}};
    enumMap: {[key: string]: string};
    option: {[key: string]: AllTypesStructSerialized} | undefined;
    selfSet: {[key: string]: AllTypesStructSerialized}[];
    optionDate: string | undefined;
    optionTime: string | undefined;
}

export class AllTypesStruct implements AllTypes {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain01.AllTypes';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'izumi.test.domain01.AllTypes.Struct';

    public getPackageName(): string { return AllTypesStruct.PackageName; }
    public getClassName(): string { return AllTypesStruct.ClassName; }
    public getFullClassName(): string { return AllTypesStruct.FullClassName; }

    private _b: boolean;
    private _s: string;
    private _int8: number;
    private _int16: number;
    private _int32: number;
    private _int64: number;
    private _f: number;
    private _d: number;
    private _uuid: string;
    private _ts: Date;
    private _tslocal: Date;
    private _tsuni: Date;
    private _time: Date;
    private _date: Date;
    private _uint8: number;
    private _uint16: number;
    private _uint32: number;
    private _uint64: number;
    private _list: AllTypes[];
    private _another: AllTypes[];
    private _selfMap: {[key: string]: AllTypes};
    private _enumMap: {[key: string]: GoAliasEnumTest};
    private _option: AllTypes | undefined;
    private _selfSet: AllTypes[];
    private _optionDate: Date | undefined;
    private _optionTime: Date | undefined;

    public get b(): boolean {
        return this._b;
    }

    public set b(value: boolean) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field b is not optional');
        }

        if (typeof value !== 'boolean') {
            throw new Error('Field b expects boolean type, got ' + value);
        }

        this._b = value;
    }

    public get s(): string {
        return this._s;
    }

    public set s(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field s is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field s expects type string, got ' + value);
        }

        this._s = value;
    }

    public get int8(): number {
        return this._int8;
    }

    public set int8(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field int8 is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field int8 expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field int8 is expected to be an integer, got ' + value);
        }

        if (value < -128) {
            throw new Error('Field int8 is expected to be not less than -128, got ' + value);
        }

        if (value > 127) {
            throw new Error('Field int8 is expected to be not greater than 127, got ' + value);
        }

        this._int8 = value;
    }

    public get int16(): number {
        return this._int16;
    }

    public set int16(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field int16 is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field int16 expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field int16 is expected to be an integer, got ' + value);
        }

        if (value < -32768) {
            throw new Error('Field int16 is expected to be not less than -32768, got ' + value);
        }

        if (value > 32767) {
            throw new Error('Field int16 is expected to be not greater than 32767, got ' + value);
        }

        this._int16 = value;
    }

    public get int32(): number {
        return this._int32;
    }

    public set int32(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field int32 is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field int32 expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field int32 is expected to be an integer, got ' + value);
        }

        this._int32 = value;
    }

    public get int64(): number {
        return this._int64;
    }

    public set int64(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field int64 is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field int64 expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field int64 is expected to be an integer, got ' + value);
        }

        this._int64 = value;
    }

    public get f(): number {
        // Precision: 32
        return this._f;
    }

    public set f(value: number) {
        // Precision: 32
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field f is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field f expects type number, got ' + value);
        }

        this._f = value;
    }

    public get d(): number {
        // Precision: 64
        return this._d;
    }

    public set d(value: number) {
        // Precision: 64
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field d is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field d expects type number, got ' + value);
        }

        this._d = value;
    }

    public get uuid(): string {
        return this._uuid;
    }

    public set uuid(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field uuid is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field uuid expects type string, got ' + value);
        }

        if (!value.match('^[0-9a-fA-f]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$')) {
            throw new Error('Field uuid expects guid format, got ' + value);
        }

        this._uuid = value;
    }

    public get ts(): Date {
        return this._ts;
    }

    public set ts(value: Date) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field ts is not optional');
        }

        if (!(value instanceof Date)) {
            throw new Error('Field ts expects type Date, got ' + value);
        }
        this._ts = value;
    }

    public get tsAsString(): string {
        return Formatter.writeZoneDateTime(this._ts);
    }

    public set tsAsString(value: string) {
        if (typeof value !== 'string') {
            throw new Error('tsAsString expects type string, got ' + value);
        }
        this._ts = Formatter.readZoneDateTime(value);
    }

    public get tslocal(): Date {
        return this._tslocal;
    }

    public set tslocal(value: Date) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field tslocal is not optional');
        }

        if (!(value instanceof Date)) {
            throw new Error('Field tslocal expects type Date, got ' + value);
        }
        this._tslocal = value;
    }

    public get tslocalAsString(): string {
        return Formatter.writeLocalDateTime(this._tslocal);
    }

    public set tslocalAsString(value: string) {
        if (typeof value !== 'string') {
            throw new Error('tslocalAsString expects type string, got ' + value);
        }
        this._tslocal = Formatter.readLocalDateTime(value);
    }

    public get tsuni(): Date {
        return this._tsuni;
    }

    public set tsuni(value: Date) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field tsuni is not optional');
        }

        if (!(value instanceof Date)) {
            throw new Error('Field tsuni expects type Date, got ' + value);
        }
        this._tsuni = value;
    }

    public get tsuniAsString(): string {
        return Formatter.writeUTCDateTime(this._tsuni);
    }

    public set tsuniAsString(value: string) {
        if (typeof value !== 'string') {
            throw new Error('tsuniAsString expects type string, got ' + value);
        }
        this._tsuni = Formatter.readUTCDateTime(value);
    }

    public get time(): Date {
        return this._time;
    }

    public set time(value: Date) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field time is not optional');
        }

        if (!(value instanceof Date)) {
            throw new Error('Field time expects type Date, got ' + value);
        }

        this._time = value;
    }

    public get timeAsString(): string {
        return Formatter.writeTime(this._time);
    }

    public set timeAsString(value: string) {
        if (typeof value !== 'string') {
            throw new Error('timeAsString expects type string, got ' + value);
        }

        this._time = Formatter.readTime(value);
    }

    public get date(): Date {
        return this._date;
    }

    public set date(value: Date) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field date is not optional');
        }

        if (!(value instanceof Date)) {
            throw new Error('Field date expects type Date, got ' + value);
        }
        this._date = value;
    }

    public get dateAsString(): string {
        return Formatter.writeDate(this._date);
    }

    public set dateAsString(value: string) {
        if (typeof value !== 'string') {
            throw new Error('dateAsString expects type string, got ' + value);
        }
        this._date = Formatter.readDate(value);
    }

    public get uint8(): number {
        return this._uint8;
    }

    public set uint8(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field uint8 is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field uint8 expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field uint8 is expected to be an integer, got ' + value);
        }

        if (value < 0) {
            throw new Error('Field uint8 is expected to be not less than 0, got ' + value);
        }

        if (value > 255) {
            throw new Error('Field uint8 is expected to be not greater than 255, got ' + value);
        }

        this._uint8 = value;
    }

    public get uint16(): number {
        return this._uint16;
    }

    public set uint16(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field uint16 is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field uint16 expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field uint16 is expected to be an integer, got ' + value);
        }

        if (value < 0) {
            throw new Error('Field uint16 is expected to be not less than 0, got ' + value);
        }

        if (value > 65535) {
            throw new Error('Field uint16 is expected to be not greater than 65535, got ' + value);
        }

        this._uint16 = value;
    }

    public get uint32(): number {
        return this._uint32;
    }

    public set uint32(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field uint32 is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field uint32 expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field uint32 is expected to be an integer, got ' + value);
        }

        if (value < 0) {
            throw new Error('Field uint32 is expected to be not less than 0, got ' + value);
        }

        if (value > 4294967295) {
            throw new Error('Field uint32 is expected to be not greater than 4294967295, got ' + value);
        }

        this._uint32 = value;
    }

    public get uint64(): number {
        return this._uint64;
    }

    public set uint64(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field uint64 is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field uint64 expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field uint64 is expected to be an integer, got ' + value);
        }

        if (value < 0) {
            throw new Error('Field uint64 is expected to be not less than 0, got ' + value);
        }

        if (value > 18446744073709551615) {
            throw new Error('Field uint64 is expected to be not greater than 18446744073709551615, got ' + value);
        }

        this._uint64 = value;
    }

    public get list(): AllTypes[] {
        return this._list;
    }

    public set list(value: AllTypes[]) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field list is not optional');
        }
        this._list = value;
    }

    public get another(): AllTypes[] {
        return this._another;
    }

    public set another(value: AllTypes[]) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field another is not optional');
        }
        this._another = value;
    }

    public get selfMap(): {[key: string]: AllTypes} {
        return this._selfMap;
    }

    public set selfMap(value: {[key: string]: AllTypes}) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field selfMap is not optional');
        }
        this._selfMap = value;
    }

    public get enumMap(): {[key: string]: GoAliasEnumTest} {
        return this._enumMap;
    }

    public set enumMap(value: {[key: string]: GoAliasEnumTest}) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field enumMap is not optional');
        }
        this._enumMap = value;
    }

    public get option(): AllTypes | undefined {
        return this._option;
    }

    public set option(value: AllTypes | undefined) {
        if (typeof value === 'undefined' || value === null) {
            this._option = undefined;
            return;
        }
        this._option = value;
    }

    public get selfSet(): AllTypes[] {
        return this._selfSet;
    }

    public set selfSet(value: AllTypes[]) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field selfSet is not optional');
        }
        this._selfSet = value;
    }

    public get optionDate(): Date | undefined {
        return this._optionDate;
    }

    public set optionDate(value: Date | undefined) {
        if (typeof value === 'undefined' || value === null) {
            this._optionDate = undefined;
            return;
        }

        if (!(value instanceof Date)) {
            throw new Error('Field optionDate expects type Date, got ' + value);
        }
        this._optionDate = value;
    }

    public get optionDateAsString(): string | undefined {
        if (!this._optionDate) {
            return undefined;
        }
        return Formatter.writeLocalDateTime(this._optionDate);
    }

    public set optionDateAsString(value: string | undefined) {
        if (typeof value !== 'string') {
            this._optionDate = undefined;
            return;
        }
        this._optionDate = Formatter.readLocalDateTime(value);
    }

    public get optionTime(): Date | undefined {
        return this._optionTime;
    }

    public set optionTime(value: Date | undefined) {
        if (typeof value === 'undefined' || value === null) {
            this._optionTime = undefined;
            return;
        }

        if (!(value instanceof Date)) {
            throw new Error('Field optionTime expects type Date, got ' + value);
        }

        this._optionTime = value;
    }

    public get optionTimeAsString(): string | undefined {
        if (!this._optionTime) {
            return undefined;
        }
        return Formatter.writeTime(this._optionTime);
    }

    public set optionTimeAsString(value: string | undefined) {
        if (typeof value !== 'string') {
            this._optionTime = undefined;
            return;
        }

        this._optionTime = Formatter.readTime(value);
    }

    constructor(data: AllTypesStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            this.list = [];
            this.another = [];
            this.selfMap = {};
            this.enumMap = {};
            this.selfSet = [];
            return;
        }

        this.b = data.b;
        this.s = data.s;
        this.int8 = data.int8;
        this.int16 = data.int16;
        this.int32 = data.int32;
        this.int64 = data.int64;
        this.f = data.f;
        this.d = data.d;
        this.uuid = data.uuid;
        this.tsAsString = data.ts;
        this.tslocalAsString = data.tslocal;
        this.tsuniAsString = data.tsuni;
        this.timeAsString = data.time;
        this.dateAsString = data.date;
        this.uint8 = data.uint8;
        this.uint16 = data.uint16;
        this.uint32 = data.uint32;
        this.uint64 = data.uint64;
        this.list = data.list.map(e => { return AllTypesStruct.create(e); });
        this.another = data.another.map(e => { return AllTypesStruct.create(e); });
        this.selfMap = Object.keys(data.selfMap).reduce<any>((previous, current) => {previous[current] = AllTypesStruct.create(data.selfMap[current as any]); return previous; }, {});
        this.enumMap = Object.keys(data.enumMap).reduce<any>((previous, current) => {previous[current] = GoAliasEnumTest[data.enumMap[current as any] as keyof typeof GoAliasEnumTest]; return previous; }, {});
        this.option = typeof data.option !== 'undefined' ? AllTypesStruct.create(data.option) : undefined;
        this.selfSet = data.selfSet.map(e => { return AllTypesStruct.create(e); });
        this.optionDateAsString = typeof data.optionDate !== 'undefined' ? data.optionDate : undefined;
        this.optionTimeAsString = typeof data.optionTime !== 'undefined' ? data.optionTime : undefined;
    }

    public serialize(): AllTypesStructSerialized {
        return {
            b: this.b,
            s: this.s,
            int8: this.int8,
            int16: this.int16,
            int32: this.int32,
            int64: this.int64,
            f: this.f,
            d: this.d,
            uuid: this.uuid,
            ts: this.tsAsString,
            tslocal: this.tslocalAsString,
            tsuni: this.tsuniAsString,
            time: this.timeAsString,
            date: this.dateAsString,
            uint8: this.uint8,
            uint16: this.uint16,
            uint32: this.uint32,
            uint64: this.uint64,
            list: this.list.map(e => { return {[e.getFullClassName()]: e.serialize()}; }),
            another: this.another.map(e => { return {[e.getFullClassName()]: e.serialize()}; }),
            selfMap: Object.keys(this.selfMap).reduce<any>((previous, current) => {previous[current] = {[this.selfMap[current as any].getFullClassName()]: this.selfMap[current as any].serialize()}; return previous; }, {}),
            enumMap: Object.keys(this.enumMap).reduce<any>((previous, current) => {previous[current] = GoAliasEnumTest[this.enumMap[current as any]]; return previous; }, {}),
            option: typeof this.option !== 'undefined' ? {[this.option.getFullClassName()]: this.option.serialize()} : undefined,
            selfSet: this.selfSet.map(e => { return {[e.getFullClassName()]: e.serialize()}; }),
            optionDate: typeof this.optionDate !== 'undefined' ? this.optionDateAsString : undefined,
            optionTime: typeof this.optionTime !== 'undefined' ? this.optionTimeAsString : undefined
        };
    }

    // Polymorphic section below. If a new type to be registered, use AllTypesStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: AllTypesStruct| AllTypesStructSerialized): AllTypes}} = {
        // This basic registration will happen below [AllTypesStruct.FullClassName]: AllTypesStruct
    };

    public static register(className: string, ctor: {new (data?: AllTypesStruct| AllTypesStructSerialized): AllTypes}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: AllTypesStructSerialized}): AllTypes {
        const polymorphicId = Object.keys(data)[0];
        const ctor = AllTypesStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for AllTypesStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(AllTypesStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in AllTypesStruct._knownPolymorphic;
    }
}

AllTypesStruct.register(AllTypesStruct.FullClassName, AllTypesStruct);

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorMixinObject,
    IIntrospectorDataObject
} from '../../../irt';
Introspector.register('izumi.test.domain01.AllTypes', {
        full: 'izumi.test.domain01.AllTypes',
        short: 'AllTypes',
        package: 'izumi.test.domain01',
        type: IntrospectorTypes.Mixin,
        ctor: () => new AllTypesStruct(),
        fields: [
            {
                name: 'b',
                accessName: 'b',
                type: {intro: IntrospectorTypes.Bool}
            },
            {
                name: 's',
                accessName: 's',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'int8',
                accessName: 'int8',
                type: {intro: IntrospectorTypes.I08}
            },
            {
                name: 'int16',
                accessName: 'int16',
                type: {intro: IntrospectorTypes.I16}
            },
            {
                name: 'int32',
                accessName: 'int32',
                type: {intro: IntrospectorTypes.I32}
            },
            {
                name: 'int64',
                accessName: 'int64',
                type: {intro: IntrospectorTypes.I64}
            },
            {
                name: 'f',
                accessName: 'f',
                type: {intro: IntrospectorTypes.F32}
            },
            {
                name: 'd',
                accessName: 'd',
                type: {intro: IntrospectorTypes.F64}
            },
            {
                name: 'uuid',
                accessName: 'uuid',
                type: {intro: IntrospectorTypes.Uid}
            },
            {
                name: 'ts',
                accessName: 'ts',
                type: {intro: IntrospectorTypes.Tsz}
            },
            {
                name: 'tslocal',
                accessName: 'tslocal',
                type: {intro: IntrospectorTypes.Tsl}
            },
            {
                name: 'tsuni',
                accessName: 'tsuni',
                type: {intro: IntrospectorTypes.Tsu}
            },
            {
                name: 'time',
                accessName: 'time',
                type: {intro: IntrospectorTypes.Time}
            },
            {
                name: 'date',
                accessName: 'date',
                type: {intro: IntrospectorTypes.Date}
            },
            {
                name: 'uint8',
                accessName: 'uint8',
                type: {intro: IntrospectorTypes.U08}
            },
            {
                name: 'uint16',
                accessName: 'uint16',
                type: {intro: IntrospectorTypes.U16}
            },
            {
                name: 'uint32',
                accessName: 'uint32',
                type: {intro: IntrospectorTypes.U32}
            },
            {
                name: 'uint64',
                accessName: 'uint64',
                type: {intro: IntrospectorTypes.U64}
            },
            {
                name: 'list',
                accessName: 'list',
                type: {intro: IntrospectorTypes.List, value: {intro: IntrospectorTypes.Mixin, full: 'izumi.test.domain01.AllTypes'} as IIntrospectorUserType} as IIntrospectorGenericType
            },
            {
                name: 'another',
                accessName: 'another',
                type: {intro: IntrospectorTypes.List, value: {intro: IntrospectorTypes.Mixin, full: 'izumi.test.domain01.AllTypes'} as IIntrospectorUserType} as IIntrospectorGenericType
            },
            {
                name: 'selfMap',
                accessName: 'selfMap',
                type: {intro: IntrospectorTypes.Map, key: {intro: IntrospectorTypes.Str}, value: {intro: IntrospectorTypes.Mixin, full: 'izumi.test.domain01.AllTypes'} as IIntrospectorUserType} as IIntrospectorMapType
            },
            {
                name: 'enumMap',
                accessName: 'enumMap',
                type: {intro: IntrospectorTypes.Map, key: {intro: IntrospectorTypes.Str}, value: {intro: IntrospectorTypes.Enum, full: 'izumi.test.domain01.GoAliasEnumTest'} as IIntrospectorUserType} as IIntrospectorMapType
            },
            {
                name: 'option',
                accessName: 'option',
                type: {intro: IntrospectorTypes.Opt, value: {intro: IntrospectorTypes.Mixin, full: 'izumi.test.domain01.AllTypes'} as IIntrospectorUserType} as IIntrospectorGenericType
            },
            {
                name: 'selfSet',
                accessName: 'selfSet',
                type: {intro: IntrospectorTypes.Set, value: {intro: IntrospectorTypes.Mixin, full: 'izumi.test.domain01.AllTypes'} as IIntrospectorUserType} as IIntrospectorGenericType
            },
            {
                name: 'optionDate',
                accessName: 'optionDate',
                type: {intro: IntrospectorTypes.Opt, value: {intro: IntrospectorTypes.Tsl}} as IIntrospectorGenericType
            },
            {
                name: 'optionTime',
                accessName: 'optionTime',
                type: {intro: IntrospectorTypes.Opt, value: {intro: IntrospectorTypes.Time}} as IIntrospectorGenericType
            }
        ],
        implementations: AllTypesStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(AllTypesStruct.FullClassName, {
        full: AllTypesStruct.FullClassName,
        short: AllTypesStruct.ClassName,
        package: AllTypesStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new AllTypesStruct(),
        fields: [
            {
                name: 'b',
                accessName: 'b',
                type: {intro: IntrospectorTypes.Bool}
            },
            {
                name: 's',
                accessName: 's',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'int8',
                accessName: 'int8',
                type: {intro: IntrospectorTypes.I08}
            },
            {
                name: 'int16',
                accessName: 'int16',
                type: {intro: IntrospectorTypes.I16}
            },
            {
                name: 'int32',
                accessName: 'int32',
                type: {intro: IntrospectorTypes.I32}
            },
            {
                name: 'int64',
                accessName: 'int64',
                type: {intro: IntrospectorTypes.I64}
            },
            {
                name: 'f',
                accessName: 'f',
                type: {intro: IntrospectorTypes.F32}
            },
            {
                name: 'd',
                accessName: 'd',
                type: {intro: IntrospectorTypes.F64}
            },
            {
                name: 'uuid',
                accessName: 'uuid',
                type: {intro: IntrospectorTypes.Uid}
            },
            {
                name: 'ts',
                accessName: 'ts',
                type: {intro: IntrospectorTypes.Tsz}
            },
            {
                name: 'tslocal',
                accessName: 'tslocal',
                type: {intro: IntrospectorTypes.Tsl}
            },
            {
                name: 'tsuni',
                accessName: 'tsuni',
                type: {intro: IntrospectorTypes.Tsu}
            },
            {
                name: 'time',
                accessName: 'time',
                type: {intro: IntrospectorTypes.Time}
            },
            {
                name: 'date',
                accessName: 'date',
                type: {intro: IntrospectorTypes.Date}
            },
            {
                name: 'uint8',
                accessName: 'uint8',
                type: {intro: IntrospectorTypes.U08}
            },
            {
                name: 'uint16',
                accessName: 'uint16',
                type: {intro: IntrospectorTypes.U16}
            },
            {
                name: 'uint32',
                accessName: 'uint32',
                type: {intro: IntrospectorTypes.U32}
            },
            {
                name: 'uint64',
                accessName: 'uint64',
                type: {intro: IntrospectorTypes.U64}
            },
            {
                name: 'list',
                accessName: 'list',
                type: {intro: IntrospectorTypes.List, value: {intro: IntrospectorTypes.Mixin, full: 'izumi.test.domain01.AllTypes'} as IIntrospectorUserType} as IIntrospectorGenericType
            },
            {
                name: 'another',
                accessName: 'another',
                type: {intro: IntrospectorTypes.List, value: {intro: IntrospectorTypes.Mixin, full: 'izumi.test.domain01.AllTypes'} as IIntrospectorUserType} as IIntrospectorGenericType
            },
            {
                name: 'selfMap',
                accessName: 'selfMap',
                type: {intro: IntrospectorTypes.Map, key: {intro: IntrospectorTypes.Str}, value: {intro: IntrospectorTypes.Mixin, full: 'izumi.test.domain01.AllTypes'} as IIntrospectorUserType} as IIntrospectorMapType
            },
            {
                name: 'enumMap',
                accessName: 'enumMap',
                type: {intro: IntrospectorTypes.Map, key: {intro: IntrospectorTypes.Str}, value: {intro: IntrospectorTypes.Enum, full: 'izumi.test.domain01.GoAliasEnumTest'} as IIntrospectorUserType} as IIntrospectorMapType
            },
            {
                name: 'option',
                accessName: 'option',
                type: {intro: IntrospectorTypes.Opt, value: {intro: IntrospectorTypes.Mixin, full: 'izumi.test.domain01.AllTypes'} as IIntrospectorUserType} as IIntrospectorGenericType
            },
            {
                name: 'selfSet',
                accessName: 'selfSet',
                type: {intro: IntrospectorTypes.Set, value: {intro: IntrospectorTypes.Mixin, full: 'izumi.test.domain01.AllTypes'} as IIntrospectorUserType} as IIntrospectorGenericType
            },
            {
                name: 'optionDate',
                accessName: 'optionDate',
                type: {intro: IntrospectorTypes.Opt, value: {intro: IntrospectorTypes.Tsl}} as IIntrospectorGenericType
            },
            {
                name: 'optionTime',
                accessName: 'optionTime',
                type: {intro: IntrospectorTypes.Opt, value: {intro: IntrospectorTypes.Time}} as IIntrospectorGenericType
            }
        ]
    } as IIntrospectorDataObject
);