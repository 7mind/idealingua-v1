// Auto-generated, any modifications may be overwritten in the future.

// OverlayEmailAttributes DTO
export class OverlayEmailAttributes  {
    // Runtime identification methods
    public static readonly PackageName = 'overlaytest.withoverlay';
    public static readonly ClassName = 'OverlayEmailAttributes';
    public static readonly FullClassName = 'overlaytest.withoverlay.OverlayEmailAttributes';

    public getPackageName(): string { return OverlayEmailAttributes.PackageName; }
    public getClassName(): string { return OverlayEmailAttributes.ClassName; }
    public getFullClassName(): string { return OverlayEmailAttributes.FullClassName; }

    private _disposable: boolean;

    public get disposable(): boolean {
        return this._disposable;
    }

    public set disposable(value: boolean) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field disposable is not optional');
        }

        if (typeof value !== 'boolean') {
            throw new Error('Field disposable expects boolean type, got ' + value);
        }

        this._disposable = value;
    }

    constructor(data: OverlayEmailAttributesSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.disposable = data.disposable;
    }

    public serialize(): OverlayEmailAttributesSerialized {
        return {
            disposable: this.disposable
        };
    }
}

export interface OverlayEmailAttributesSerialized  {
    disposable: boolean;
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
Introspector.register(OverlayEmailAttributes.FullClassName, {
        full: OverlayEmailAttributes.FullClassName,
        short: OverlayEmailAttributes.ClassName,
        package: OverlayEmailAttributes.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new OverlayEmailAttributes(),
        fields: [
            {
                name: 'disposable',
                accessName: 'disposable',
                type: {intro: IntrospectorTypes.Bool}
            }
        ]
    } as IIntrospectorDataObject
);