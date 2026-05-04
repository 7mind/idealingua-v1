// Auto-generated, any modifications may be overwritten in the future.

export class AdtTestID implements IAdtTestID {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.algebraics';
    public static readonly ClassName = 'AdtTestID';
    public static readonly FullClassName = 'idltest.algebraics.AdtTestID';

    public getPackageName(): string { return AdtTestID.PackageName; }
    public getClassName(): string { return AdtTestID.ClassName; }
    public getFullClassName(): string { return AdtTestID.FullClassName; }

    private _id: string;

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

        this._id = value;
    }

    constructor(data: string | IAdtTestID = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        if (typeof data === 'string') {
            if (!data.startsWith('AdtTestID#')) {
                throw new Error('Identifier must start with AdtTestID, got ' + data);
            }
            const parts = data.substr(data.indexOf('#') + 1).split(':');
            this.id = decodeURIComponent(parts[0]);
        } else {
            this.id = data.id;
        }
    }

    public toString(): string {
        const suffix = encodeURIComponent(this.id);
        return 'AdtTestID#' + suffix;
    }

    public serialize(): string {
        return this.toString();
    }
}

export interface IAdtTestID {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): string;

    id: string;
}

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorIdObject
} from '../../irt';
Introspector.register(AdtTestID.FullClassName, {
        full: AdtTestID.FullClassName,
        short: AdtTestID.ClassName,
        package: AdtTestID.PackageName,
        type: IntrospectorTypes.Id,
        ctor: () => new AdtTestID(),
        fields: [
            {
                name: 'id',
                accessName: 'id',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorIdObject
);