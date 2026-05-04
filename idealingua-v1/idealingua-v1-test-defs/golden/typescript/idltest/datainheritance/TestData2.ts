// Auto-generated, any modifications may be overwritten in the future.

// TestData2 DTO
export class TestData2  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.datainheritance';
    public static readonly ClassName = 'TestData2';
    public static readonly FullClassName = 'idltest.datainheritance.TestData2';

    public getPackageName(): string { return TestData2.PackageName; }
    public getClassName(): string { return TestData2.ClassName; }
    public getFullClassName(): string { return TestData2.FullClassName; }

    private _str: string;
    private _i32: number;
    private _value: number;

    public get str(): string {
        return this._str;
    }

    public set str(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field str is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field str expects type string, got ' + value);
        }

        this._str = value;
    }

    public get i32(): number {
        return this._i32;
    }

    public set i32(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field i32 is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field i32 expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field i32 is expected to be an integer, got ' + value);
        }

        this._i32 = value;
    }

    public get value(): number {
        return this._value;
    }

    public set value(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field value is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field value expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field value is expected to be an integer, got ' + value);
        }

        if (value < -128) {
            throw new Error('Field value is expected to be not less than -128, got ' + value);
        }

        if (value > 127) {
            throw new Error('Field value is expected to be not greater than 127, got ' + value);
        }

        this._value = value;
    }

    constructor(data: TestData2Serialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.str = data.str;
        this.i32 = data.i32;
        this.value = data.value;
    }

    public serialize(): TestData2Serialized {
        return {
            str: this.str,
            i32: this.i32,
            value: this.value
        };
    }
}

export interface TestData2Serialized  {
    str: string;
    i32: number;
    value: number;
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
Introspector.register(TestData2.FullClassName, {
        full: TestData2.FullClassName,
        short: TestData2.ClassName,
        package: TestData2.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new TestData2(),
        fields: [
            {
                name: 'str',
                accessName: 'str',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'i32',
                accessName: 'i32',
                type: {intro: IntrospectorTypes.I32}
            },
            {
                name: 'value',
                accessName: 'value',
                type: {intro: IntrospectorTypes.I08}
            }
        ]
    } as IIntrospectorDataObject
);