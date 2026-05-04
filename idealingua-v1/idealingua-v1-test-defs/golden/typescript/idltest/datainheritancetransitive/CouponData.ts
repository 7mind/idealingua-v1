// Auto-generated, any modifications may be overwritten in the future.
import { Formatter } from '../../irt';

// CouponData Interface
export interface CouponData {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): CouponDataStructSerialized;

    validFrom: Date | undefined;
    validTill: Date | undefined;
    code: string;
}

export interface CouponDataStructSerialized {
    validFrom: string | undefined;
    validTill: string | undefined;
    code: string;
}

export class CouponDataStruct implements CouponData {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.datainheritancetransitive.CouponData';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.datainheritancetransitive.CouponData.Struct';

    public getPackageName(): string { return CouponDataStruct.PackageName; }
    public getClassName(): string { return CouponDataStruct.ClassName; }
    public getFullClassName(): string { return CouponDataStruct.FullClassName; }

    private _validFrom: Date | undefined;
    private _validTill: Date | undefined;
    private _code: string;

    public get validFrom(): Date | undefined {
        return this._validFrom;
    }

    public set validFrom(value: Date | undefined) {
        if (typeof value === 'undefined' || value === null) {
            this._validFrom = undefined;
            return;
        }

        if (!(value instanceof Date)) {
            throw new Error('Field validFrom expects type Date, got ' + value);
        }
        this._validFrom = value;
    }

    public get validFromAsString(): string | undefined {
        if (!this._validFrom) {
            return undefined;
        }
        return Formatter.writeLocalDateTime(this._validFrom);
    }

    public set validFromAsString(value: string | undefined) {
        if (typeof value !== 'string') {
            this._validFrom = undefined;
            return;
        }
        this._validFrom = Formatter.readLocalDateTime(value);
    }

    public get validTill(): Date | undefined {
        return this._validTill;
    }

    public set validTill(value: Date | undefined) {
        if (typeof value === 'undefined' || value === null) {
            this._validTill = undefined;
            return;
        }

        if (!(value instanceof Date)) {
            throw new Error('Field validTill expects type Date, got ' + value);
        }
        this._validTill = value;
    }

    public get validTillAsString(): string | undefined {
        if (!this._validTill) {
            return undefined;
        }
        return Formatter.writeLocalDateTime(this._validTill);
    }

    public set validTillAsString(value: string | undefined) {
        if (typeof value !== 'string') {
            this._validTill = undefined;
            return;
        }
        this._validTill = Formatter.readLocalDateTime(value);
    }

    public get code(): string {
        return this._code;
    }

    public set code(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field code is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field code expects type string, got ' + value);
        }

        this._code = value;
    }

    constructor(data: CouponDataStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.validFromAsString = typeof data.validFrom !== 'undefined' ? data.validFrom : undefined;
        this.validTillAsString = typeof data.validTill !== 'undefined' ? data.validTill : undefined;
        this.code = data.code;
    }

    public serialize(): CouponDataStructSerialized {
        return {
            validFrom: typeof this.validFrom !== 'undefined' ? this.validFromAsString : undefined,
            validTill: typeof this.validTill !== 'undefined' ? this.validTillAsString : undefined,
            code: this.code
        };
    }

    // Polymorphic section below. If a new type to be registered, use CouponDataStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: CouponDataStruct| CouponDataStructSerialized): CouponData}} = {
        // This basic registration will happen below [CouponDataStruct.FullClassName]: CouponDataStruct
    };

    public static register(className: string, ctor: {new (data?: CouponDataStruct| CouponDataStructSerialized): CouponData}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: CouponDataStructSerialized}): CouponData {
        const polymorphicId = Object.keys(data)[0];
        const ctor = CouponDataStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for CouponDataStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(CouponDataStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in CouponDataStruct._knownPolymorphic;
    }
}

CouponDataStruct.register(CouponDataStruct.FullClassName, CouponDataStruct);

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorMixinObject,
    IIntrospectorDataObject
} from '../../irt';
Introspector.register('idltest.datainheritancetransitive.CouponData', {
        full: 'idltest.datainheritancetransitive.CouponData',
        short: 'CouponData',
        package: 'idltest.datainheritancetransitive',
        type: IntrospectorTypes.Mixin,
        ctor: () => new CouponDataStruct(),
        fields: [
            {
                name: 'validFrom',
                accessName: 'validFrom',
                type: {intro: IntrospectorTypes.Opt, value: {intro: IntrospectorTypes.Tsl}} as IIntrospectorGenericType
            },
            {
                name: 'validTill',
                accessName: 'validTill',
                type: {intro: IntrospectorTypes.Opt, value: {intro: IntrospectorTypes.Tsl}} as IIntrospectorGenericType
            },
            {
                name: 'code',
                accessName: 'code',
                type: {intro: IntrospectorTypes.Str}
            }
        ],
        implementations: CouponDataStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(CouponDataStruct.FullClassName, {
        full: CouponDataStruct.FullClassName,
        short: CouponDataStruct.ClassName,
        package: CouponDataStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new CouponDataStruct(),
        fields: [
            {
                name: 'validFrom',
                accessName: 'validFrom',
                type: {intro: IntrospectorTypes.Opt, value: {intro: IntrospectorTypes.Tsl}} as IIntrospectorGenericType
            },
            {
                name: 'validTill',
                accessName: 'validTill',
                type: {intro: IntrospectorTypes.Opt, value: {intro: IntrospectorTypes.Tsl}} as IIntrospectorGenericType
            },
            {
                name: 'code',
                accessName: 'code',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);