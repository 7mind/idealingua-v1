// Auto-generated, any modifications may be overwritten in the future.
import {
    OverlayUserAttributes,
    OverlayUserAttributesSerialized
} from './OverlayUserAttributes';

// User DTO
export class User  {
    // Runtime identification methods
    public static readonly PackageName = 'overlaytest.withoverlay';
    public static readonly ClassName = 'User';
    public static readonly FullClassName = 'overlaytest.withoverlay.User';

    public getPackageName(): string { return User.PackageName; }
    public getClassName(): string { return User.ClassName; }
    public getFullClassName(): string { return User.FullClassName; }

    private _id: string;
    private _attributes: OverlayUserAttributes;

    public get id(): string {
        return this._id;
    }

    public set id(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field id is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field id expects type string, got ' + value);
        }

        if (!value.match('^[0-9a-fA-f]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$')) {
            throw new Error('Field id expects guid format, got ' + value);
        }

        this._id = value;
    }

    public get attributes(): OverlayUserAttributes {
        return this._attributes;
    }

    public set attributes(value: OverlayUserAttributes) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field attributes is not optional');
        }
        this._attributes = value;
    }

    constructor(data: UserSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.id = data.id;
        this.attributes = new OverlayUserAttributes(data.attributes);
    }

    public serialize(): UserSerialized {
        return {
            id: this.id,
            attributes: this.attributes.serialize()
        };
    }
}

export interface UserSerialized  {
    id: string;
    attributes: OverlayUserAttributesSerialized;
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
Introspector.register(User.FullClassName, {
        full: User.FullClassName,
        short: User.ClassName,
        package: User.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new User(),
        fields: [
            {
                name: 'id',
                accessName: 'id',
                type: {intro: IntrospectorTypes.Uid}
            },
            {
                name: 'attributes',
                accessName: 'attributes',
                type: {intro: IntrospectorTypes.Data, full: 'overlaytest.withoverlay.OverlayUserAttributes'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorDataObject
);