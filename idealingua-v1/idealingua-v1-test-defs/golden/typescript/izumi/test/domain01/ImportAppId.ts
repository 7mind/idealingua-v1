// Auto-generated, any modifications may be overwritten in the future.

export class ImportAppId implements IImportAppId {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain01';
    public static readonly ClassName = 'ImportAppId';
    public static readonly FullClassName = 'izumi.test.domain01.ImportAppId';

    public getPackageName(): string { return ImportAppId.PackageName; }
    public getClassName(): string { return ImportAppId.ClassName; }
    public getFullClassName(): string { return ImportAppId.FullClassName; }

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

        if (!value.match('^[0-9a-fA-f]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$')) {
            throw new Error('Field id expects guid format, got ' + value);
        }

        this._id = value;
    }

    constructor(data: string | IImportAppId = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        if (typeof data === 'string') {
            if (!data.startsWith('ImportAppId#')) {
                throw new Error('Identifier must start with ImportAppId, got ' + data);
            }
            const parts = data.substr(data.indexOf('#') + 1).split(':');
            this.id = decodeURIComponent(parts[0]);
        } else {
            this.id = data.id;
        }
    }

    public toString(): string {
        const suffix = encodeURIComponent(this.id);
        return 'ImportAppId#' + suffix;
    }

    public serialize(): string {
        return this.toString();
    }
}

export interface IImportAppId {
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
} from '../../../irt';
Introspector.register(ImportAppId.FullClassName, {
        full: ImportAppId.FullClassName,
        short: ImportAppId.ClassName,
        package: ImportAppId.PackageName,
        type: IntrospectorTypes.Id,
        ctor: () => new ImportAppId(),
        fields: [
            {
                name: 'id',
                accessName: 'id',
                type: {intro: IntrospectorTypes.Uid}
            }
        ]
    } as IIntrospectorIdObject
);