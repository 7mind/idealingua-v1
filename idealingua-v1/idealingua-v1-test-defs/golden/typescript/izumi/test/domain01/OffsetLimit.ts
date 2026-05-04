// Auto-generated, any modifications may be overwritten in the future.

// OffsetLimit DTO
export class OffsetLimit  {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain01';
    public static readonly ClassName = 'OffsetLimit';
    public static readonly FullClassName = 'izumi.test.domain01.OffsetLimit';

    public getPackageName(): string { return OffsetLimit.PackageName; }
    public getClassName(): string { return OffsetLimit.ClassName; }
    public getFullClassName(): string { return OffsetLimit.FullClassName; }

    private _offset: number;
    private _limit: number;

    public get offset(): number {
        return this._offset;
    }

    public set offset(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field offset is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field offset expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field offset is expected to be an integer, got ' + value);
        }

        this._offset = value;
    }

    public get limit(): number {
        return this._limit;
    }

    public set limit(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field limit is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field limit expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field limit is expected to be an integer, got ' + value);
        }

        if (value < -32768) {
            throw new Error('Field limit is expected to be not less than -32768, got ' + value);
        }

        if (value > 32767) {
            throw new Error('Field limit is expected to be not greater than 32767, got ' + value);
        }

        this._limit = value;
    }

    constructor(data: OffsetLimitSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.offset = data.offset;
        this.limit = data.limit;
    }

    public serialize(): OffsetLimitSerialized {
        return {
            offset: this.offset,
            limit: this.limit
        };
    }
}

export interface OffsetLimitSerialized  {
    offset: number;
    limit: number;
}

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorDataObject
} from '../../../irt';
Introspector.register(OffsetLimit.FullClassName, {
        full: OffsetLimit.FullClassName,
        short: OffsetLimit.ClassName,
        package: OffsetLimit.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new OffsetLimit(),
        fields: [
            {
                name: 'offset',
                accessName: 'offset',
                type: {intro: IntrospectorTypes.I32}
            },
            {
                name: 'limit',
                accessName: 'limit',
                type: {intro: IntrospectorTypes.I16}
            }
        ]
    } as IIntrospectorDataObject
);