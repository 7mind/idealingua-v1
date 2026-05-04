// Auto-generated, any modifications may be overwritten in the future.

// TestInterface2 Interface
export interface TestInterface2 {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): TestInterface2StructSerialized;

    if2Field: number;
    sameField: number;
    sameEverywhereField: number;
}

export interface TestInterface2StructSerialized {
    if2Field: number;
    sameField: number;
    sameEverywhereField: number;
}

export class TestInterface2Struct implements TestInterface2 {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain02.TestInterface2';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'izumi.test.domain02.TestInterface2.Struct';

    public getPackageName(): string { return TestInterface2Struct.PackageName; }
    public getClassName(): string { return TestInterface2Struct.ClassName; }
    public getFullClassName(): string { return TestInterface2Struct.FullClassName; }

    private _if2Field: number;
    private _sameField: number;
    private _sameEverywhereField: number;

    public get if2Field(): number {
        return this._if2Field;
    }

    public set if2Field(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field if2Field is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field if2Field expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field if2Field is expected to be an integer, got ' + value);
        }

        this._if2Field = value;
    }

    public get sameField(): number {
        return this._sameField;
    }

    public set sameField(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field sameField is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field sameField expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field sameField is expected to be an integer, got ' + value);
        }

        this._sameField = value;
    }

    public get sameEverywhereField(): number {
        return this._sameEverywhereField;
    }

    public set sameEverywhereField(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field sameEverywhereField is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field sameEverywhereField expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field sameEverywhereField is expected to be an integer, got ' + value);
        }

        this._sameEverywhereField = value;
    }

    constructor(data: TestInterface2StructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.if2Field = data.if2Field;
        this.sameField = data.sameField;
        this.sameEverywhereField = data.sameEverywhereField;
    }

    public serialize(): TestInterface2StructSerialized {
        return {
            if2Field: this.if2Field,
            sameField: this.sameField,
            sameEverywhereField: this.sameEverywhereField
        };
    }

    // Polymorphic section below. If a new type to be registered, use TestInterface2Struct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: TestInterface2Struct| TestInterface2StructSerialized): TestInterface2}} = {
        // This basic registration will happen below [TestInterface2Struct.FullClassName]: TestInterface2Struct
    };

    public static register(className: string, ctor: {new (data?: TestInterface2Struct| TestInterface2StructSerialized): TestInterface2}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: TestInterface2StructSerialized}): TestInterface2 {
        const polymorphicId = Object.keys(data)[0];
        const ctor = TestInterface2Struct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for TestInterface2Struct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(TestInterface2Struct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in TestInterface2Struct._knownPolymorphic;
    }
}

TestInterface2Struct.register(TestInterface2Struct.FullClassName, TestInterface2Struct);

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorMixinObject,
    IIntrospectorDataObject
} from '../../../irt';
Introspector.register('izumi.test.domain02.TestInterface2', {
        full: 'izumi.test.domain02.TestInterface2',
        short: 'TestInterface2',
        package: 'izumi.test.domain02',
        type: IntrospectorTypes.Mixin,
        ctor: () => new TestInterface2Struct(),
        fields: [
            {
                name: 'if2Field',
                accessName: 'if2Field',
                type: {intro: IntrospectorTypes.I64}
            },
            {
                name: 'sameField',
                accessName: 'sameField',
                type: {intro: IntrospectorTypes.I64}
            },
            {
                name: 'sameEverywhereField',
                accessName: 'sameEverywhereField',
                type: {intro: IntrospectorTypes.I64}
            }
        ],
        implementations: TestInterface2Struct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(TestInterface2Struct.FullClassName, {
        full: TestInterface2Struct.FullClassName,
        short: TestInterface2Struct.ClassName,
        package: TestInterface2Struct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new TestInterface2Struct(),
        fields: [
            {
                name: 'if2Field',
                accessName: 'if2Field',
                type: {intro: IntrospectorTypes.I64}
            },
            {
                name: 'sameField',
                accessName: 'sameField',
                type: {intro: IntrospectorTypes.I64}
            },
            {
                name: 'sameEverywhereField',
                accessName: 'sameEverywhereField',
                type: {intro: IntrospectorTypes.I64}
            }
        ]
    } as IIntrospectorDataObject
);