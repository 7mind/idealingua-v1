// Auto-generated, any modifications may be overwritten in the future.

// OverlayUserAttributes DTO
export class OverlayUserAttributes  {
    // Runtime identification methods
    public static readonly PackageName = 'overlaytest.withoverlay';
    public static readonly ClassName = 'OverlayUserAttributes';
    public static readonly FullClassName = 'overlaytest.withoverlay.OverlayUserAttributes';

    public getPackageName(): string { return OverlayUserAttributes.PackageName; }
    public getClassName(): string { return OverlayUserAttributes.ClassName; }
    public getFullClassName(): string { return OverlayUserAttributes.FullClassName; }

    private _name: string;
    private _surname: string;

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

    public get surname(): string {
        return this._surname;
    }

    public set surname(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field surname is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field surname expects type string, got ' + value);
        }

        this._surname = value;
    }

    constructor(data: OverlayUserAttributesSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.name = data.name;
        this.surname = data.surname;
    }

    public serialize(): OverlayUserAttributesSerialized {
        return {
            name: this.name,
            surname: this.surname
        };
    }
}

export interface OverlayUserAttributesSerialized  {
    name: string;
    surname: string;
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
Introspector.register(OverlayUserAttributes.FullClassName, {
        full: OverlayUserAttributes.FullClassName,
        short: OverlayUserAttributes.ClassName,
        package: OverlayUserAttributes.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new OverlayUserAttributes(),
        fields: [
            {
                name: 'name',
                accessName: 'name',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'surname',
                accessName: 'surname',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);