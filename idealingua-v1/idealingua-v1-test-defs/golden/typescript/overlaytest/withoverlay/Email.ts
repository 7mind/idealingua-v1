// Auto-generated, any modifications may be overwritten in the future.
import {
    OverlayEmailAttributes,
    OverlayEmailAttributesSerialized
} from './OverlayEmailAttributes';

// Email DTO
export class Email  {
    // Runtime identification methods
    public static readonly PackageName = 'overlaytest.withoverlay';
    public static readonly ClassName = 'Email';
    public static readonly FullClassName = 'overlaytest.withoverlay.Email';

    public getPackageName(): string { return Email.PackageName; }
    public getClassName(): string { return Email.ClassName; }
    public getFullClassName(): string { return Email.FullClassName; }

    private _attributes: OverlayEmailAttributes;

    public get attributes(): OverlayEmailAttributes {
        return this._attributes;
    }

    public set attributes(value: OverlayEmailAttributes) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field attributes is not optional');
        }
        this._attributes = value;
    }

    constructor(data: EmailSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.attributes = new OverlayEmailAttributes(data.attributes);
    }

    public serialize(): EmailSerialized {
        return {
            attributes: this.attributes.serialize()
        };
    }
}

export interface EmailSerialized  {
    attributes: OverlayEmailAttributesSerialized;
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
Introspector.register(Email.FullClassName, {
        full: Email.FullClassName,
        short: Email.ClassName,
        package: Email.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new Email(),
        fields: [
            {
                name: 'attributes',
                accessName: 'attributes',
                type: {intro: IntrospectorTypes.Data, full: 'overlaytest.withoverlay.OverlayEmailAttributes'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorDataObject
);