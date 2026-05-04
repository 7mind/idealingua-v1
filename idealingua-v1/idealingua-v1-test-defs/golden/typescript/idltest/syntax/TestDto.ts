// Auto-generated, any modifications may be overwritten in the future.
import {
    TestMixin,
    TestMixinStruct,
    TestMixinStructSerialized
} from './TestMixin';

// TestDto DTO
export class TestDto  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.syntax';
    public static readonly ClassName = 'TestDto';
    public static readonly FullClassName = 'idltest.syntax.TestDto';

    public getPackageName(): string { return TestDto.PackageName; }
    public getClassName(): string { return TestDto.ClassName; }
    public getFullClassName(): string { return TestDto.FullClassName; }

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

    constructor(data: TestDtoSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.value = TestMixinStruct.create(data.value);
    }

    public serialize(): TestDtoSerialized {
        return {
            value: {[this.value.getFullClassName()]: this.value.serialize()}
        };
    }
}

export interface TestDtoSerialized  {
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
Introspector.register(TestDto.FullClassName, {
        full: TestDto.FullClassName,
        short: TestDto.ClassName,
        package: TestDto.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new TestDto(),
        fields: [
            {
                name: 'value',
                accessName: 'value',
                type: {intro: IntrospectorTypes.Mixin, full: 'idltest.syntax.TestMixin'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorDataObject
);