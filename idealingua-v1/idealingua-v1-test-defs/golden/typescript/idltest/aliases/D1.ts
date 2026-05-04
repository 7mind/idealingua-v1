// Auto-generated, any modifications may be overwritten in the future.
import {
    M1,
    M1Struct,
    M1StructSerialized
} from './M1';
import {
    M2,
    M2Struct,
    M2StructSerialized
} from '../aliases2';

// D1 DTO
export class D1 implements M1, M2  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.aliases';
    public static readonly ClassName = 'D1';
    public static readonly FullClassName = 'idltest.aliases.D1';

    public getPackageName(): string { return D1.PackageName; }
    public getClassName(): string { return D1.ClassName; }
    public getFullClassName(): string { return D1.FullClassName; }

    private _value: string;
    private _f2: string;

    public get value(): string {
        return this._value;
    }

    public set value(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field value is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field value expects type string, got ' + value);
        }

        this._value = value;
    }

    public get f2(): string {
        return this._f2;
    }

    public set f2(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field f2 is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field f2 expects type string, got ' + value);
        }

        this._f2 = value;
    }

    constructor(data: D1Serialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.value = data.value;
        this.f2 = data.f2;
    }

    public toM1Serialized(): M1StructSerialized {
        return {
            value: this.value
        };
    }

    public toM1(): M1Struct {
        return new M1Struct(this.toM1Serialized());
    }

    public toM2Serialized(): M2StructSerialized {
        return {
            f2: this.f2
        };
    }

    public toM2(): M2Struct {
        return new M2Struct(this.toM2Serialized());
    }

    public loadM1Serialized(slice: M1StructSerialized) {
        this.value = slice.value;
    }

    public loadM1(slice: M1Struct) {
        this.loadM1Serialized(slice.serialize());
    }

    public loadM2Serialized(slice: M2StructSerialized) {
        this.f2 = slice.f2;
    }

    public loadM2(slice: M2Struct) {
        this.loadM2Serialized(slice.serialize());
    }

    public serialize(): D1Serialized {
        return {
            value: this.value,
            f2: this.f2
        };
    }
}

export interface D1Serialized extends M1StructSerialized, M2StructSerialized  {
    value: string;
    f2: string;
}

M1Struct.register(D1.FullClassName, D1);
M2Struct.register(D1.FullClassName, D1);

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorDataObject
} from '../../irt';
Introspector.register(D1.FullClassName, {
        full: D1.FullClassName,
        short: D1.ClassName,
        package: D1.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new D1(),
        fields: [
            {
                name: 'value',
                accessName: 'value',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'f2',
                accessName: 'f2',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);