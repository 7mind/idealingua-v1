// Auto-generated, any modifications may be overwritten in the future.

// TestData1 DTO
export class TestData1  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.datainheritance';
    public static readonly ClassName = 'TestData1';
    public static readonly FullClassName = 'idltest.datainheritance.TestData1';

    public getPackageName(): string { return TestData1.PackageName; }
    public getClassName(): string { return TestData1.ClassName; }
    public getFullClassName(): string { return TestData1.FullClassName; }

    private _str: string;
    private _i32: number;

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

    constructor(data: TestData1Serialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.str = data.str;
        this.i32 = data.i32;
    }

    public serialize(): TestData1Serialized {
        return {
            str: this.str,
            i32: this.i32
        };
    }
}

export interface TestData1Serialized  {
    str: string;
    i32: number;
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
Introspector.register(TestData1.FullClassName, {
        full: TestData1.FullClassName,
        short: TestData1.ClassName,
        package: TestData1.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new TestData1(),
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
            }
        ]
    } as IIntrospectorDataObject
);