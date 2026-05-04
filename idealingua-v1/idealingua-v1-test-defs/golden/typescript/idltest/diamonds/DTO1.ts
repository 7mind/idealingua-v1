// Auto-generated, any modifications may be overwritten in the future.
import {
    TestInterface3Struct,
    TestInterface3StructSerialized
} from './TestInterface3';
import {
    TestInterface1Struct,
    TestInterface1StructSerialized
} from './TestInterface1';
import {
    TestInterface2Struct,
    TestInterface2StructSerialized
} from './TestInterface2';

// DTO1 DTO
export class DTO1  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.diamonds';
    public static readonly ClassName = 'DTO1';
    public static readonly FullClassName = 'idltest.diamonds.DTO1';

    public getPackageName(): string { return DTO1.PackageName; }
    public getClassName(): string { return DTO1.ClassName; }
    public getFullClassName(): string { return DTO1.FullClassName; }

    private _if1Field_overriden: number;
    private _if1Field_inherited: number;
    private _if3Field: number;
    private _if2Field: number;
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

    constructor(data: DTO1Serialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.if1Field_overriden = data.if1Field_overriden;
        this.if1Field_inherited = data.if1Field_inherited;
        this.if3Field = data.if3Field;
        this.if2Field = data.if2Field;
        this.sameField = data.sameField;
        this.sameEverywhereField = data.sameEverywhereField;
    }

    public serialize(): DTO1Serialized {
        return {
            if1Field_overriden: this.if1Field_overriden,
            if1Field_inherited: this.if1Field_inherited,
            if3Field: this.if3Field,
            if2Field: this.if2Field,
            sameField: this.sameField,
            sameEverywhereField: this.sameEverywhereField
        };
    }
}

export interface DTO1Serialized  {
    if1Field_overriden: number;
    if1Field_inherited: number;
    if3Field: number;
    if2Field: number;
    sameField: number;
    sameEverywhereField: number;
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
Introspector.register(DTO1.FullClassName, {
        full: DTO1.FullClassName,
        short: DTO1.ClassName,
        package: DTO1.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new DTO1(),
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
                name: 'if3Field',
                accessName: 'if3Field',
                type: {intro: IntrospectorTypes.I64}
            },
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