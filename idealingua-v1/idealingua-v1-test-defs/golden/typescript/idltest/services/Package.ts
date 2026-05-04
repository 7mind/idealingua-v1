// Auto-generated, any modifications may be overwritten in the future.

// Package DTO
export class Package  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.services';
    public static readonly ClassName = 'Package';
    public static readonly FullClassName = 'idltest.services.Package';

    public getPackageName(): string { return Package.PackageName; }
    public getClassName(): string { return Package.ClassName; }
    public getFullClassName(): string { return Package.FullClassName; }

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

    constructor(data: PackageSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.name = data.name;
    }

    public serialize(): PackageSerialized {
        return {
            name: this.name
        };
    }
}

export interface PackageSerialized  {
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
Introspector.register(Package.FullClassName, {
        full: Package.FullClassName,
        short: Package.ClassName,
        package: Package.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new Package(),
        fields: [
            {
                name: 'name',
                accessName: 'name',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);