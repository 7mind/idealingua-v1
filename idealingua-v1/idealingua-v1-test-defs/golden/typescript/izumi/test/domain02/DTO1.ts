// Auto-generated, any modifications may be overwritten in the future.
import {
    TestInterface3,
    TestInterface3Struct,
    TestInterface3StructSerialized
} from './TestInterface3';
import {
    TestValIdentifier
} from '../domain01';
import {
    TestInterface1Struct,
    TestInterface1StructSerialized
} from './TestInterface1';
import {
    TestInterface2,
    TestInterface2Struct,
    TestInterface2StructSerialized
} from './TestInterface2';

// DTO1 DTO
export class DTO1 implements TestInterface2, TestInterface3  {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain02';
    public static readonly ClassName = 'DTO1';
    public static readonly FullClassName = 'izumi.test.domain02.DTO1';

    public getPackageName(): string { return DTO1.PackageName; }
    public getClassName(): string { return DTO1.ClassName; }
    public getFullClassName(): string { return DTO1.FullClassName; }

    private _if1Field_overriden: number;
    private _if1Field_inherited: number;
    private _sameField: number;
    private _sameEverywhereField: number;
    private _fromOtherDomain: TestValIdentifier;
    private _fromOtherDomainDirect: TestValIdentifier;
    private _if3Field: number;
    private _if2Field: number;

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

    constructor(data: DTO1Serialized = undefined) {
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
        this.if2Field = data.if2Field;
    }

    public toTestInterface2Serialized(): TestInterface2StructSerialized {
        return {
            if2Field: this.if2Field,
            sameField: this.sameField,
            sameEverywhereField: this.sameEverywhereField
        };
    }

    public toTestInterface2(): TestInterface2Struct {
        return new TestInterface2Struct(this.toTestInterface2Serialized());
    }

    public toTestInterface3Serialized(): TestInterface3StructSerialized {
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

    public toTestInterface3(): TestInterface3Struct {
        return new TestInterface3Struct(this.toTestInterface3Serialized());
    }

    public toTestInterface1Serialized(): TestInterface1StructSerialized {
        return {
            if1Field_overriden: this.if1Field_overriden,
            if1Field_inherited: this.if1Field_inherited,
            sameField: this.sameField,
            sameEverywhereField: this.sameEverywhereField,
            fromOtherDomain: this.fromOtherDomain.serialize(),
            fromOtherDomainDirect: this.fromOtherDomainDirect.serialize()
        };
    }

    public toTestInterface1(): TestInterface1Struct {
        return new TestInterface1Struct(this.toTestInterface1Serialized());
    }

    public loadTestInterface2Serialized(slice: TestInterface2StructSerialized) {
        this.if2Field = slice.if2Field;
        this.sameField = slice.sameField;
        this.sameEverywhereField = slice.sameEverywhereField;
    }

    public loadTestInterface2(slice: TestInterface2Struct) {
        this.loadTestInterface2Serialized(slice.serialize());
    }

    public loadTestInterface3Serialized(slice: TestInterface3StructSerialized) {
        this.if1Field_overriden = slice.if1Field_overriden;
        this.if1Field_inherited = slice.if1Field_inherited;
        this.sameField = slice.sameField;
        this.sameEverywhereField = slice.sameEverywhereField;
        this.fromOtherDomain = new TestValIdentifier(slice.fromOtherDomain);
        this.fromOtherDomainDirect = new TestValIdentifier(slice.fromOtherDomainDirect);
        this.if3Field = slice.if3Field;
    }

    public loadTestInterface3(slice: TestInterface3Struct) {
        this.loadTestInterface3Serialized(slice.serialize());
    }

    public loadTestInterface1Serialized(slice: TestInterface1StructSerialized) {
        this.if1Field_overriden = slice.if1Field_overriden;
        this.if1Field_inherited = slice.if1Field_inherited;
        this.sameField = slice.sameField;
        this.sameEverywhereField = slice.sameEverywhereField;
        this.fromOtherDomain = new TestValIdentifier(slice.fromOtherDomain);
        this.fromOtherDomainDirect = new TestValIdentifier(slice.fromOtherDomainDirect);
    }

    public loadTestInterface1(slice: TestInterface1Struct) {
        this.loadTestInterface1Serialized(slice.serialize());
    }

    public serialize(): DTO1Serialized {
        return {
            if1Field_overriden: this.if1Field_overriden,
            if1Field_inherited: this.if1Field_inherited,
            sameField: this.sameField,
            sameEverywhereField: this.sameEverywhereField,
            fromOtherDomain: this.fromOtherDomain.serialize(),
            fromOtherDomainDirect: this.fromOtherDomainDirect.serialize(),
            if3Field: this.if3Field,
            if2Field: this.if2Field
        };
    }
}

export interface DTO1Serialized extends TestInterface2StructSerialized, TestInterface3StructSerialized  {
    if1Field_overriden: number;
    if1Field_inherited: number;
    sameField: number;
    sameEverywhereField: number;
    fromOtherDomain: string;
    fromOtherDomainDirect: string;
    if3Field: number;
    if2Field: number;
}

TestInterface2Struct.register(DTO1.FullClassName, DTO1);
TestInterface3Struct.register(DTO1.FullClassName, DTO1);
TestInterface1Struct.register(DTO1.FullClassName, DTO1);

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorDataObject
} from '../../../irt';
Introspector.register(DTO1.FullClassName, {
        full: DTO1.FullClassName,
        short: DTO1.ClassName,
        package: DTO1.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new DTO1(),
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
            },
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
                name: 'if1Field_inherited',
                accessName: 'if1Field_inherited',
                type: {intro: IntrospectorTypes.I32}
            },
            {
                name: 'fromOtherDomain',
                accessName: 'fromOtherDomain',
                type: {intro: IntrospectorTypes.Id, full: 'izumi.test.domain01.TestValIdentifier'} as IIntrospectorUserType
            },
            {
                name: 'fromOtherDomainDirect',
                accessName: 'fromOtherDomainDirect',
                type: {intro: IntrospectorTypes.Id, full: 'izumi.test.domain01.TestValIdentifier'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorDataObject
);