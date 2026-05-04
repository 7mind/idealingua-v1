// Auto-generated, any modifications may be overwritten in the future.
import {
    RTestEnum
} from '../domain01';

// TestDataWithAlias DTO
export class TestDataWithAlias  {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain02';
    public static readonly ClassName = 'TestDataWithAlias';
    public static readonly FullClassName = 'izumi.test.domain02.TestDataWithAlias';

    public getPackageName(): string { return TestDataWithAlias.PackageName; }
    public getClassName(): string { return TestDataWithAlias.ClassName; }
    public getFullClassName(): string { return TestDataWithAlias.FullClassName; }

    private _a: RTestEnum;

    public get a(): RTestEnum {
        return this._a;
    }

    public set a(value: RTestEnum) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field a is not optional');
        }
        this._a = value;
    }

    constructor(data: TestDataWithAliasSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.a = RTestEnum[data.a as keyof typeof RTestEnum];
    }

    public serialize(): TestDataWithAliasSerialized {
        return {
            a: RTestEnum[this.a]
        };
    }
}

export interface TestDataWithAliasSerialized  {
    a: string;
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
Introspector.register(TestDataWithAlias.FullClassName, {
        full: TestDataWithAlias.FullClassName,
        short: TestDataWithAlias.ClassName,
        package: TestDataWithAlias.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new TestDataWithAlias(),
        fields: [
            {
                name: 'a',
                accessName: 'a',
                type: {intro: IntrospectorTypes.Enum, full: 'izumi.test.domain01.RTestEnum'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorDataObject
);