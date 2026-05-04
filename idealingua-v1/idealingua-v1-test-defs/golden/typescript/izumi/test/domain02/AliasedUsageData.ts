// Auto-generated, any modifications may be overwritten in the future.
import {
    TestObject,
    TestObjectSerialized,
    GoAliasEnumTest
} from '../domain01';

// AliasedUsageData DTO
export class AliasedUsageData  {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain02';
    public static readonly ClassName = 'AliasedUsageData';
    public static readonly FullClassName = 'izumi.test.domain02.AliasedUsageData';

    public getPackageName(): string { return AliasedUsageData.PackageName; }
    public getClassName(): string { return AliasedUsageData.ClassName; }
    public getFullClassName(): string { return AliasedUsageData.FullClassName; }

    private _testObj: TestObject;
    private _enumField: GoAliasEnumTest;

    public get testObj(): TestObject {
        return this._testObj;
    }

    public set testObj(value: TestObject) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field testObj is not optional');
        }
        this._testObj = value;
    }

    public get enumField(): GoAliasEnumTest {
        return this._enumField;
    }

    public set enumField(value: GoAliasEnumTest) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field enumField is not optional');
        }
        this._enumField = value;
    }

    constructor(data: AliasedUsageDataSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.testObj = new TestObject(data.testObj);
        this.enumField = GoAliasEnumTest[data.enumField as keyof typeof GoAliasEnumTest];
    }

    public serialize(): AliasedUsageDataSerialized {
        return {
            testObj: this.testObj.serialize(),
            enumField: GoAliasEnumTest[this.enumField]
        };
    }
}

export interface AliasedUsageDataSerialized  {
    testObj: TestObjectSerialized;
    enumField: string;
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
Introspector.register(AliasedUsageData.FullClassName, {
        full: AliasedUsageData.FullClassName,
        short: AliasedUsageData.ClassName,
        package: AliasedUsageData.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new AliasedUsageData(),
        fields: [
            {
                name: 'testObj',
                accessName: 'testObj',
                type: {intro: IntrospectorTypes.Data, full: 'izumi.test.domain01.TestObject'} as IIntrospectorUserType
            },
            {
                name: 'enumField',
                accessName: 'enumField',
                type: {intro: IntrospectorTypes.Enum, full: 'izumi.test.domain01.GoAliasEnumTest'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorDataObject
);