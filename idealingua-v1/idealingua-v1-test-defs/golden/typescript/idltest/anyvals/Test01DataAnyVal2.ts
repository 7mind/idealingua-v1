// Auto-generated, any modifications may be overwritten in the future.
import {
    Test01MixinAnyValStruct,
    Test01MixinAnyValStructSerialized
} from './Test01MixinAnyVal';

// Test01DataAnyVal2 DTO
export class Test01DataAnyVal2  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.anyvals';
    public static readonly ClassName = 'Test01DataAnyVal2';
    public static readonly FullClassName = 'idltest.anyvals.Test01DataAnyVal2';

    public getPackageName(): string { return Test01DataAnyVal2.PackageName; }
    public getClassName(): string { return Test01DataAnyVal2.ClassName; }
    public getFullClassName(): string { return Test01DataAnyVal2.FullClassName; }

    private _value: string;
    private _someInt: number;

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

    public get someInt(): number {
        return this._someInt;
    }

    public set someInt(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field someInt is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field someInt expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field someInt is expected to be an integer, got ' + value);
        }

        if (value < -128) {
            throw new Error('Field someInt is expected to be not less than -128, got ' + value);
        }

        if (value > 127) {
            throw new Error('Field someInt is expected to be not greater than 127, got ' + value);
        }

        this._someInt = value;
    }

    constructor(data: Test01DataAnyVal2Serialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.value = data.value;
        this.someInt = data.someInt;
    }

    public serialize(): Test01DataAnyVal2Serialized {
        return {
            value: this.value,
            someInt: this.someInt
        };
    }
}

export interface Test01DataAnyVal2Serialized  {
    value: string;
    someInt: number;
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
Introspector.register(Test01DataAnyVal2.FullClassName, {
        full: Test01DataAnyVal2.FullClassName,
        short: Test01DataAnyVal2.ClassName,
        package: Test01DataAnyVal2.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new Test01DataAnyVal2(),
        fields: [
            {
                name: 'value',
                accessName: 'value',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'someInt',
                accessName: 'someInt',
                type: {intro: IntrospectorTypes.I08}
            }
        ]
    } as IIntrospectorDataObject
);