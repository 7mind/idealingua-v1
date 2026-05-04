// Auto-generated, any modifications may be overwritten in the future.

// TestInterface1 Interface
export interface TestInterface1 {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): TestInterface1StructSerialized;

    if1Field_overriden: number;
    if1Field_inherited: number;
    sameField: number;
    sameEverywhereField: number;
}

export interface TestInterface1StructSerialized {
    if1Field_overriden: number;
    if1Field_inherited: number;
    sameField: number;
    sameEverywhereField: number;
}

export class TestInterface1Struct implements TestInterface1 {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.diamonds.TestInterface1';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.diamonds.TestInterface1.Struct';

    public getPackageName(): string { return TestInterface1Struct.PackageName; }
    public getClassName(): string { return TestInterface1Struct.ClassName; }
    public getFullClassName(): string { return TestInterface1Struct.FullClassName; }

    private _if1Field_overriden: number;
    private _if1Field_inherited: number;
    private _sameField: number;
    private _sameEverywhereField: number;

    public get if1Field_overriden(): number {
        return this._if1Field_overriden;
    }

    public set if1Field_overriden(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field if1Field_overriden is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field if1Field_overriden expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field if1Field_overriden is expected to be an integer, got ' + value);
        }

        this._if1Field_overriden = value;
    }

    public get if1Field_inherited(): number {
        return this._if1Field_inherited;
    }

    public set if1Field_inherited(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field if1Field_inherited is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field if1Field_inherited expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field if1Field_inherited is expected to be an integer, got ' + value);
        }

        this._if1Field_inherited = value;
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

    constructor(data: TestInterface1StructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.if1Field_overriden = data.if1Field_overriden;
        this.if1Field_inherited = data.if1Field_inherited;
        this.sameField = data.sameField;
        this.sameEverywhereField = data.sameEverywhereField;
    }

    public serialize(): TestInterface1StructSerialized {
        return {
            if1Field_overriden: this.if1Field_overriden,
            if1Field_inherited: this.if1Field_inherited,
            sameField: this.sameField,
            sameEverywhereField: this.sameEverywhereField
        };
    }

    // Polymorphic section below. If a new type to be registered, use TestInterface1Struct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: TestInterface1Struct| TestInterface1StructSerialized): TestInterface1}} = {
        // This basic registration will happen below [TestInterface1Struct.FullClassName]: TestInterface1Struct
    };

    public static register(className: string, ctor: {new (data?: TestInterface1Struct| TestInterface1StructSerialized): TestInterface1}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: TestInterface1StructSerialized}): TestInterface1 {
        const polymorphicId = Object.keys(data)[0];
        const ctor = TestInterface1Struct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for TestInterface1Struct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(TestInterface1Struct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in TestInterface1Struct._knownPolymorphic;
    }
}

TestInterface1Struct.register(TestInterface1Struct.FullClassName, TestInterface1Struct);

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
Introspector.register('idltest.diamonds.TestInterface1', {
        full: 'idltest.diamonds.TestInterface1',
        short: 'TestInterface1',
        package: 'idltest.diamonds',
        type: IntrospectorTypes.Mixin,
        ctor: () => new TestInterface1Struct(),
        fields: [
            {
                name: 'if1Field_overriden',
                accessName: 'if1Field_overriden',
                type: {intro: IntrospectorTypes.I32}
            },
            {
                name: 'if1Field_inherited',
                accessName: 'if1Field_inherited',
                type: {intro: IntrospectorTypes.I32}
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
        implementations: TestInterface1Struct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(TestInterface1Struct.FullClassName, {
        full: TestInterface1Struct.FullClassName,
        short: TestInterface1Struct.ClassName,
        package: TestInterface1Struct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new TestInterface1Struct(),
        fields: [
            {
                name: 'if1Field_overriden',
                accessName: 'if1Field_overriden',
                type: {intro: IntrospectorTypes.I32}
            },
            {
                name: 'if1Field_inherited',
                accessName: 'if1Field_inherited',
                type: {intro: IntrospectorTypes.I32}
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