// Auto-generated, any modifications may be overwritten in the future.
import {
    NameStruct,
    NameStructSerialized
} from './Name';
import {
    Name_stored_,
    Name_stored_Struct,
    Name_stored_StructSerialized
} from './Name_stored_';

// Name_stored DTO
export class Name_stored implements Name_stored_  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.phase';
    public static readonly ClassName = 'Name_stored';
    public static readonly FullClassName = 'idltest.phase.Name_stored';

    public getPackageName(): string { return Name_stored.PackageName; }
    public getClassName(): string { return Name_stored.ClassName; }
    public getFullClassName(): string { return Name_stored.FullClassName; }

    private _name: string;
    private _bytes: number;

    public get name(): string {
        return this._name;
    }

    public set name(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field name is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field name expects type string, got ' + value);
        }

        this._name = value;
    }

    public get bytes(): number {
        return this._bytes;
    }

    public set bytes(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field bytes is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field bytes expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field bytes is expected to be an integer, got ' + value);
        }

        this._bytes = value;
    }

    constructor(data: Name_storedSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.name = data.name;
        this.bytes = data.bytes;
    }

    public toName_stored_Serialized(): Name_stored_StructSerialized {
        return {
            name: this.name,
            bytes: this.bytes
        };
    }

    public toName_stored_(): Name_stored_Struct {
        return new Name_stored_Struct(this.toName_stored_Serialized());
    }

    public toNameSerialized(): NameStructSerialized {
        return {
            name: this.name
        };
    }

    public toName(): NameStruct {
        return new NameStruct(this.toNameSerialized());
    }

    public loadName_stored_Serialized(slice: Name_stored_StructSerialized) {
        this.name = slice.name;
        this.bytes = slice.bytes;
    }

    public loadName_stored_(slice: Name_stored_Struct) {
        this.loadName_stored_Serialized(slice.serialize());
    }

    public loadNameSerialized(slice: NameStructSerialized) {
        this.name = slice.name;
    }

    public loadName(slice: NameStruct) {
        this.loadNameSerialized(slice.serialize());
    }

    public serialize(): Name_storedSerialized {
        return {
            name: this.name,
            bytes: this.bytes
        };
    }
}

export interface Name_storedSerialized extends Name_stored_StructSerialized  {
    name: string;
    bytes: number;
}

Name_stored_Struct.register(Name_stored.FullClassName, Name_stored);
NameStruct.register(Name_stored.FullClassName, Name_stored);

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorDataObject
} from '../../irt';
Introspector.register(Name_stored.FullClassName, {
        full: Name_stored.FullClassName,
        short: Name_stored.ClassName,
        package: Name_stored.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new Name_stored(),
        fields: [
            {
                name: 'name',
                accessName: 'name',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'bytes',
                accessName: 'bytes',
                type: {intro: IntrospectorTypes.I64}
            }
        ]
    } as IIntrospectorDataObject
);