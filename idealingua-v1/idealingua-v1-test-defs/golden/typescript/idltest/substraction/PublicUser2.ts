// Auto-generated, any modifications may be overwritten in the future.
import {
    PersonalAttributesStruct,
    PersonalAttributesStructSerialized
} from './PersonalAttributes';

// PublicUser2 DTO
export class PublicUser2  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.substraction';
    public static readonly ClassName = 'PublicUser2';
    public static readonly FullClassName = 'idltest.substraction.PublicUser2';

    public getPackageName(): string { return PublicUser2.PackageName; }
    public getClassName(): string { return PublicUser2.ClassName; }
    public getFullClassName(): string { return PublicUser2.FullClassName; }

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

    constructor(data: PublicUser2Serialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.name = data.name;
    }

    public serialize(): PublicUser2Serialized {
        return {
            name: this.name
        };
    }
}

export interface PublicUser2Serialized  {
    name: string;
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
Introspector.register(PublicUser2.FullClassName, {
        full: PublicUser2.FullClassName,
        short: PublicUser2.ClassName,
        package: PublicUser2.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new PublicUser2(),
        fields: [
            {
                name: 'name',
                accessName: 'name',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);