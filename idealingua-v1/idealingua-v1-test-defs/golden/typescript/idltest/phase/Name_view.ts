// Auto-generated, any modifications may be overwritten in the future.
import {
    LengthInBytesStruct,
    LengthInBytesStructSerialized
} from './LengthInBytes';
import {
    Name,
    NameStruct,
    NameStructSerialized
} from './Name';
import {
    Name_stored_Struct,
    Name_stored_StructSerialized
} from './Name_stored_';

// Name_view DTO
export class Name_view implements Name  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.phase';
    public static readonly ClassName = 'Name_view';
    public static readonly FullClassName = 'idltest.phase.Name_view';

    public getPackageName(): string { return Name_view.PackageName; }
    public getClassName(): string { return Name_view.ClassName; }
    public getFullClassName(): string { return Name_view.FullClassName; }

    private _bytes: number;
    private _name: string;
    private _relatives: Name[];

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

    public get relatives(): Name[] {
        return this._relatives;
    }

    public set relatives(value: Name[]) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field relatives is not optional');
        }
        this._relatives = value;
    }

    constructor(data: Name_viewSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            this.relatives = [];
            return;
        }

        this.bytes = data.bytes;
        this.name = data.name;
        this.relatives = data.relatives.map(e => { return NameStruct.create(e); });
    }

    public toNameSerialized(): NameStructSerialized {
        return {
            name: this.name
        };
    }

    public toName(): NameStruct {
        return new NameStruct(this.toNameSerialized());
    }

    public loadNameSerialized(slice: NameStructSerialized) {
        this.name = slice.name;
    }

    public loadName(slice: NameStruct) {
        this.loadNameSerialized(slice.serialize());
    }

    public serialize(): Name_viewSerialized {
        return {
            bytes: this.bytes,
            name: this.name,
            relatives: this.relatives.map(e => { return {[e.getFullClassName()]: e.serialize()}; })
        };
    }
}

export interface Name_viewSerialized extends NameStructSerialized  {
    bytes: number;
    name: string;
    relatives: {[key: string]: NameStructSerialized}[];
}

NameStruct.register(Name_view.FullClassName, Name_view);

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorDataObject
} from '../../irt';
Introspector.register(Name_view.FullClassName, {
        full: Name_view.FullClassName,
        short: Name_view.ClassName,
        package: Name_view.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new Name_view(),
        fields: [
            {
                name: 'relatives',
                accessName: 'relatives',
                type: {intro: IntrospectorTypes.List, value: {intro: IntrospectorTypes.Mixin, full: 'idltest.phase.Name'} as IIntrospectorUserType} as IIntrospectorGenericType
            },
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