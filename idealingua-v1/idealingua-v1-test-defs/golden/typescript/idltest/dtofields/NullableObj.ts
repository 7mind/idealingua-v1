// Auto-generated, any modifications may be overwritten in the future.
import {
    NullableContent,
    NullableContentStruct,
    NullableContentStructSerialized
} from './NullableContent';

// NullableObj DTO
export class NullableObj implements NullableContent  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.dtofields';
    public static readonly ClassName = 'NullableObj';
    public static readonly FullClassName = 'idltest.dtofields.NullableObj';

    public getPackageName(): string { return NullableObj.PackageName; }
    public getClassName(): string { return NullableObj.ClassName; }
    public getFullClassName(): string { return NullableObj.FullClassName; }

    private _a: number;

    public get a(): number {
        return this._a;
    }

    public set a(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field a is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field a expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field a is expected to be an integer, got ' + value);
        }

        this._a = value;
    }

    constructor(data: NullableObjSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.a = data.a;
    }

    public toNullableContentSerialized(): NullableContentStructSerialized {
        return {
            a: this.a
        };
    }

    public toNullableContent(): NullableContentStruct {
        return new NullableContentStruct(this.toNullableContentSerialized());
    }

    public loadNullableContentSerialized(slice: NullableContentStructSerialized) {
        this.a = slice.a;
    }

    public loadNullableContent(slice: NullableContentStruct) {
        this.loadNullableContentSerialized(slice.serialize());
    }

    public serialize(): NullableObjSerialized {
        return {
            a: this.a
        };
    }
}

export interface NullableObjSerialized extends NullableContentStructSerialized  {
    a: number;
}

NullableContentStruct.register(NullableObj.FullClassName, NullableObj);

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorDataObject
} from '../../irt';
Introspector.register(NullableObj.FullClassName, {
        full: NullableObj.FullClassName,
        short: NullableObj.ClassName,
        package: NullableObj.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new NullableObj(),
        fields: [
            {
                name: 'a',
                accessName: 'a',
                type: {intro: IntrospectorTypes.I32}
            }
        ]
    } as IIntrospectorDataObject
);