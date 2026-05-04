// Auto-generated, any modifications may be overwritten in the future.
import {
    Test01MixinAnyVal,
    Test01MixinAnyValStruct,
    Test01MixinAnyValStructSerialized
} from './Test01MixinAnyVal';

// Test01DataAnyVal1 DTO
export class Test01DataAnyVal1 implements Test01MixinAnyVal  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.anyvals';
    public static readonly ClassName = 'Test01DataAnyVal1';
    public static readonly FullClassName = 'idltest.anyvals.Test01DataAnyVal1';

    public getPackageName(): string { return Test01DataAnyVal1.PackageName; }
    public getClassName(): string { return Test01DataAnyVal1.ClassName; }
    public getFullClassName(): string { return Test01DataAnyVal1.FullClassName; }

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

    constructor(data: Test01DataAnyVal1Serialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.value = data.value;
        this.someInt = data.someInt;
    }

    public toTest01MixinAnyValSerialized(): Test01MixinAnyValStructSerialized {
        return {
            value: this.value
        };
    }

    public toTest01MixinAnyVal(): Test01MixinAnyValStruct {
        return new Test01MixinAnyValStruct(this.toTest01MixinAnyValSerialized());
    }

    public loadTest01MixinAnyValSerialized(slice: Test01MixinAnyValStructSerialized) {
        this.value = slice.value;
    }

    public loadTest01MixinAnyVal(slice: Test01MixinAnyValStruct) {
        this.loadTest01MixinAnyValSerialized(slice.serialize());
    }

    public serialize(): Test01DataAnyVal1Serialized {
        return {
            value: this.value,
            someInt: this.someInt
        };
    }
}

export interface Test01DataAnyVal1Serialized extends Test01MixinAnyValStructSerialized  {
    value: string;
    someInt: number;
}

Test01MixinAnyValStruct.register(Test01DataAnyVal1.FullClassName, Test01DataAnyVal1);

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorDataObject
} from '../../irt';
Introspector.register(Test01DataAnyVal1.FullClassName, {
        full: Test01DataAnyVal1.FullClassName,
        short: Test01DataAnyVal1.ClassName,
        package: Test01DataAnyVal1.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new Test01DataAnyVal1(),
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