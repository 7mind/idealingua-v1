// Auto-generated, any modifications may be overwritten in the future.
import {
    Name,
    NameStruct,
    NameStructSerialized
} from './Name';

// Name_incoming DTO
export class Name_incoming implements Name  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.phase';
    public static readonly ClassName = 'Name_incoming';
    public static readonly FullClassName = 'idltest.phase.Name_incoming';

    public getPackageName(): string { return Name_incoming.PackageName; }
    public getClassName(): string { return Name_incoming.ClassName; }
    public getFullClassName(): string { return Name_incoming.FullClassName; }

    private _name: string;

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

    constructor(data: Name_incomingSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.name = data.name;
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

    public serialize(): Name_incomingSerialized {
        return {
            name: this.name
        };
    }
}

export interface Name_incomingSerialized extends NameStructSerialized  {
    name: string;
}

NameStruct.register(Name_incoming.FullClassName, Name_incoming);

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorDataObject
} from '../../irt';
Introspector.register(Name_incoming.FullClassName, {
        full: Name_incoming.FullClassName,
        short: Name_incoming.ClassName,
        package: Name_incoming.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new Name_incoming(),
        fields: [
            {
                name: 'name',
                accessName: 'name',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);