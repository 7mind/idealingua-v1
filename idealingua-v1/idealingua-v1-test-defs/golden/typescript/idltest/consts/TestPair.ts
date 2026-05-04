// Auto-generated, any modifications may be overwritten in the future.

// TestPair DTO
export class TestPair  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.consts';
    public static readonly ClassName = 'TestPair';
    public static readonly FullClassName = 'idltest.consts.TestPair';

    public getPackageName(): string { return TestPair.PackageName; }
    public getClassName(): string { return TestPair.ClassName; }
    public getFullClassName(): string { return TestPair.FullClassName; }

    private _value: number;
    private _name: string;

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

        this._value = value;
    }

    public get name(): string {
        return this._name;
    }

    public set name(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field name is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field name expects type string, got ' + value);
        }

        this._name = value;
    }

    constructor(data: TestPairSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.value = data.value;
        this.name = data.name;
    }

    public serialize(): TestPairSerialized {
        return {
            value: this.value,
            name: this.name
        };
    }
}

export interface TestPairSerialized  {
    value: number;
    name: string;
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
Introspector.register(TestPair.FullClassName, {
        full: TestPair.FullClassName,
        short: TestPair.ClassName,
        package: TestPair.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new TestPair(),
        fields: [
            {
                name: 'value',
                accessName: 'value',
                type: {intro: IntrospectorTypes.I32}
            },
            {
                name: 'name',
                accessName: 'name',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);