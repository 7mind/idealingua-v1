// Auto-generated, any modifications may be overwritten in the future.
import {
    TestValIdentifier
} from '../domain01';
import {
    TestInterface1,
    TestInterface1Struct,
    TestInterface1StructSerialized
} from './TestInterface1';

// TestInterface3 Interface
export interface TestInterface3 extends TestInterface1 {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): TestInterface3StructSerialized;

    if1Field_overriden: number;
    if1Field_inherited: number;
    sameField: number;
    sameEverywhereField: number;
    fromOtherDomain: TestValIdentifier;
    fromOtherDomainDirect: TestValIdentifier;
    if3Field: number;
}

export interface TestInterface3StructSerialized extends TestInterface1StructSerialized {
    if1Field_overriden: number;
    if1Field_inherited: number;
    sameField: number;
    sameEverywhereField: number;
    fromOtherDomain: string;
    fromOtherDomainDirect: string;
    if3Field: number;
}

export class TestInterface3Struct implements TestInterface3 {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain02.TestInterface3';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'izumi.test.domain02.TestInterface3.Struct';

    public getPackageName(): string { return TestInterface3Struct.PackageName; }
    public getClassName(): string { return TestInterface3Struct.ClassName; }
    public getFullClassName(): string { return TestInterface3Struct.FullClassName; }

    private _if1Field_overriden: number;
    private _if1Field_inherited: number;
    private _sameField: number;
    private _sameEverywhereField: number;
    private _fromOtherDomain: TestValIdentifier;
    private _fromOtherDomainDirect: TestValIdentifier;
    private _if3Field: number;

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

    public get fromOtherDomain(): TestValIdentifier {
        return this._fromOtherDomain;
    }

    public set fromOtherDomain(value: TestValIdentifier) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field fromOtherDomain is not optional');
        }
        this._fromOtherDomain = value;
    }

    public get fromOtherDomainDirect(): TestValIdentifier {
        return this._fromOtherDomainDirect;
    }

    public set fromOtherDomainDirect(value: TestValIdentifier) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field fromOtherDomainDirect is not optional');
        }
        this._fromOtherDomainDirect = value;
    }

    public get if3Field(): number {
        return this._if3Field;
    }

    public set if3Field(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field if3Field is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field if3Field expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field if3Field is expected to be an integer, got ' + value);
        }

        this._if3Field = value;
    }

    constructor(data: TestInterface3StructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.if1Field_overriden = data.if1Field_overriden;
        this.if1Field_inherited = data.if1Field_inherited;
        this.sameField = data.sameField;
        this.sameEverywhereField = data.sameEverywhereField;
        this.fromOtherDomain = new TestValIdentifier(data.fromOtherDomain);
        this.fromOtherDomainDirect = new TestValIdentifier(data.fromOtherDomainDirect);
        this.if3Field = data.if3Field;
    }

    public serialize(): TestInterface3StructSerialized {
        return {
            if1Field_overriden: this.if1Field_overriden,
            if1Field_inherited: this.if1Field_inherited,
            sameField: this.sameField,
            sameEverywhereField: this.sameEverywhereField,
            fromOtherDomain: this.fromOtherDomain.serialize(),
            fromOtherDomainDirect: this.fromOtherDomainDirect.serialize(),
            if3Field: this.if3Field
        };
    }

    // Polymorphic section below. If a new type to be registered, use TestInterface3Struct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: TestInterface3Struct| TestInterface3StructSerialized): TestInterface3}} = {
        // This basic registration will happen below [TestInterface3Struct.FullClassName]: TestInterface3Struct
    };

    public static register(className: string, ctor: {new (data?: TestInterface3Struct| TestInterface3StructSerialized): TestInterface3}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: TestInterface3StructSerialized}): TestInterface3 {
        const polymorphicId = Object.keys(data)[0];
        const ctor = TestInterface3Struct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for TestInterface3Struct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(TestInterface3Struct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in TestInterface3Struct._knownPolymorphic;
    }
}

TestInterface3Struct.register(TestInterface3Struct.FullClassName, TestInterface3Struct);
TestInterface1Struct.register(TestInterface3Struct.FullClassName, TestInterface3Struct);

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
Introspector.register('izumi.test.domain02.TestInterface3', {
        full: 'izumi.test.domain02.TestInterface3',
        short: 'TestInterface3',
        package: 'izumi.test.domain02',
        type: IntrospectorTypes.Mixin,
        ctor: () => new TestInterface3Struct(),
        fields: [
            {
                name: 'if1Field_overriden',
                accessName: 'if1Field_overriden',
                type: {intro: IntrospectorTypes.I32}
            },
            {
                name: 'if3Field',
                accessName: 'if3Field',
                type: {intro: IntrospectorTypes.I64}
            },
            {
                name: 'sameEverywhereField',
                accessName: 'sameEverywhereField',
                type: {intro: IntrospectorTypes.I64}
            }
        ],
        implementations: TestInterface3Struct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(TestInterface3Struct.FullClassName, {
        full: TestInterface3Struct.FullClassName,
        short: TestInterface3Struct.ClassName,
        package: TestInterface3Struct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new TestInterface3Struct(),
        fields: [
            {
                name: 'if1Field_overriden',
                accessName: 'if1Field_overriden',
                type: {intro: IntrospectorTypes.I32}
            },
            {
                name: 'if3Field',
                accessName: 'if3Field',
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