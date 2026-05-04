// Auto-generated, any modifications may be overwritten in the future.

// TestMixin Interface
export interface TestMixin {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): TestMixinStructSerialized;
}
export interface TestMixinStructSerialized {
}

export class TestMixinStruct implements TestMixin {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.syntax.TestMixin';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.syntax.TestMixin.Struct';

    public getPackageName(): string { return TestMixinStruct.PackageName; }
    public getClassName(): string { return TestMixinStruct.ClassName; }
    public getFullClassName(): string { return TestMixinStruct.FullClassName; }

    constructor(data: TestMixinStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

    }

    public serialize(): TestMixinStructSerialized {
        return {
        };
    }

    // Polymorphic section below. If a new type to be registered, use TestMixinStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: TestMixinStruct| TestMixinStructSerialized): TestMixin}} = {
        // This basic registration will happen below [TestMixinStruct.FullClassName]: TestMixinStruct
    };

    public static register(className: string, ctor: {new (data?: TestMixinStruct| TestMixinStructSerialized): TestMixin}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: TestMixinStructSerialized}): TestMixin {
        const polymorphicId = Object.keys(data)[0];
        const ctor = TestMixinStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for TestMixinStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(TestMixinStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in TestMixinStruct._knownPolymorphic;
    }
}

TestMixinStruct.register(TestMixinStruct.FullClassName, TestMixinStruct);

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorMixinObject,
    IIntrospectorDataObject
} from '../../irt';
Introspector.register('idltest.syntax.TestMixin', {
        full: 'idltest.syntax.TestMixin',
        short: 'TestMixin',
        package: 'idltest.syntax',
        type: IntrospectorTypes.Mixin,
        ctor: () => new TestMixinStruct(),
        fields: [

        ],
        implementations: TestMixinStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(TestMixinStruct.FullClassName, {
        full: TestMixinStruct.FullClassName,
        short: TestMixinStruct.ClassName,
        package: TestMixinStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new TestMixinStruct(),
        fields: [

        ]
    } as IIntrospectorDataObject
);