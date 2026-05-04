// Auto-generated, any modifications may be overwritten in the future.

// Test00Data2AnyVal DTO
export class Test00Data2AnyVal  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.anyvals';
    public static readonly ClassName = 'Test00Data2AnyVal';
    public static readonly FullClassName = 'idltest.anyvals.Test00Data2AnyVal';

    public getPackageName(): string { return Test00Data2AnyVal.PackageName; }
    public getClassName(): string { return Test00Data2AnyVal.ClassName; }
    public getFullClassName(): string { return Test00Data2AnyVal.FullClassName; }

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

    constructor(data: Test00Data2AnyValSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.value = data.value;
        this.someInt = data.someInt;
    }

    public serialize(): Test00Data2AnyValSerialized {
        return {
            value: this.value,
            someInt: this.someInt
        };
    }
}

export interface Test00Data2AnyValSerialized  {
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
Introspector.register(Test00Data2AnyVal.FullClassName, {
        full: Test00Data2AnyVal.FullClassName,
        short: Test00Data2AnyVal.ClassName,
        package: Test00Data2AnyVal.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new Test00Data2AnyVal(),
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