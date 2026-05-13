// Auto-generated, any modifications may be overwritten in the future.

// BlobHolder DTO
export class BlobHolder  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.blobtest';
    public static readonly ClassName = 'BlobHolder';
    public static readonly FullClassName = 'idltest.blobtest.BlobHolder';

    public getPackageName(): string { return BlobHolder.PackageName; }
    public getClassName(): string { return BlobHolder.ClassName; }
    public getFullClassName(): string { return BlobHolder.FullClassName; }

    private _payload: string;
    private _label: string;

    public get payload(): string {
        return this._payload;
    }

    public set payload(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field payload is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field payload expects type string, got ' + value);
        }

        this._payload = value;
    }

    public get label(): string {
        return this._label;
    }

    public set label(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field label is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field label expects type string, got ' + value);
        }

        this._label = value;
    }

    constructor(data: BlobHolderSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.payload = data.payload;
        this.label = data.label;
    }

    public serialize(): BlobHolderSerialized {
        return {
            payload: this.payload,
            label: this.label
        };
    }
}

export interface BlobHolderSerialized  {
    payload: string;
    label: string;
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
Introspector.register(BlobHolder.FullClassName, {
        full: BlobHolder.FullClassName,
        short: BlobHolder.ClassName,
        package: BlobHolder.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new BlobHolder(),
        fields: [
            {
                name: 'payload',
                accessName: 'payload',
                type: {intro: IntrospectorTypes.Blob}
            },
            {
                name: 'label',
                accessName: 'label',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);