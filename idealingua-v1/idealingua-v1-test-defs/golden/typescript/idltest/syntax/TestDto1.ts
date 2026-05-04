// Auto-generated, any modifications may be overwritten in the future.
import {
    TestMixin,
    TestMixinStruct,
    TestMixinStructSerialized
} from './TestMixin';

// TestDto1 DTO
export class TestDto1  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.syntax';
    public static readonly ClassName = 'TestDto1';
    public static readonly FullClassName = 'idltest.syntax.TestDto1';

    public getPackageName(): string { return TestDto1.PackageName; }
    public getClassName(): string { return TestDto1.ClassName; }
    public getFullClassName(): string { return TestDto1.FullClassName; }

    private _value: TestMixin;

    public get value(): TestMixin {
        return this._value;
    }

    public set value(value: TestMixin) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field value is not optional');
        }
        this._value = value;
    }

    constructor(data: TestDto1Serialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.value = TestMixinStruct.create(data.value);
    }

    public serialize(): TestDto1Serialized {
        return {
            value: {[this.value.getFullClassName()]: this.value.serialize()}
        };
    }
}

export interface TestDto1Serialized  {
    value: {[key: string]: TestMixinStructSerialized};
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
Introspector.register(TestDto1.FullClassName, {
        full: TestDto1.FullClassName,
        short: TestDto1.ClassName,
        package: TestDto1.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new TestDto1(),
        fields: [
            {
                name: 'value',
                accessName: 'value',
                type: {intro: IntrospectorTypes.Mixin, full: 'idltest.syntax.TestMixin'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorDataObject
);