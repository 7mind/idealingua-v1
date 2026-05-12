// Auto-generated, any modifications may be overwritten in the future.
import { Formatter } from '../../irt';
import {
    CouponDataStruct,
    CouponDataStructSerialized
} from './CouponData';

// Coupon DTO
export class Coupon  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.datainheritancetransitive';
    public static readonly ClassName = 'Coupon';
    public static readonly FullClassName = 'idltest.datainheritancetransitive.Coupon';

    public getPackageName(): string { return Coupon.PackageName; }
    public getClassName(): string { return Coupon.ClassName; }
    public getFullClassName(): string { return Coupon.FullClassName; }

    private _validFrom: Date | undefined;
    private _validTill: Date | undefined;
    private _code: string;
    private _id: string;

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

    public get id(): string {
        return this._id;
    }

    public set id(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field id is not optional');
        }
        this._id = value;
    }

    constructor(data: CouponSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.validFromAsString = typeof data.validFrom !== 'undefined' ? data.validFrom : undefined;
        this.validTillAsString = typeof data.validTill !== 'undefined' ? data.validTill : undefined;
        this.code = data.code;
        this.id = data.id;
    }

    public serialize(): CouponSerialized {
        return {
            validFrom: typeof this.validFrom !== 'undefined' ? this.validFromAsString : undefined,
            validTill: typeof this.validTill !== 'undefined' ? this.validTillAsString : undefined,
            code: this.code,
            id: this.id
        };
    }
}

export interface CouponSerialized  {
    validFrom: string | undefined;
    validTill: string | undefined;
    code: string;
    id: string;
}

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorDataObject
} from '../../irt';
Introspector.register(Coupon.FullClassName, {
        full: Coupon.FullClassName,
        short: Coupon.ClassName,
        package: Coupon.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new Coupon(),
        fields: [
            {
                name: 'id',
                accessName: 'id',
                type: {intro: IntrospectorTypes.Str}
            },
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