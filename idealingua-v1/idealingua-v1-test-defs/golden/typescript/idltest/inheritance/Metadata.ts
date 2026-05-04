// Auto-generated, any modifications may be overwritten in the future.

// Metadata Interface
export interface Metadata {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): MetadataStructSerialized;

    id: string;
    name: string;
}

export interface MetadataStructSerialized {
    id: string;
    name: string;
}

export class MetadataStruct implements Metadata {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.inheritance.Metadata';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.inheritance.Metadata.Struct';

    public getPackageName(): string { return MetadataStruct.PackageName; }
    public getClassName(): string { return MetadataStruct.ClassName; }
    public getFullClassName(): string { return MetadataStruct.FullClassName; }

    private _id: string;
    private _name: string;

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

    constructor(data: MetadataStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.id = data.id;
        this.name = data.name;
    }

    public serialize(): MetadataStructSerialized {
        return {
            id: this.id,
            name: this.name
        };
    }

    // Polymorphic section below. If a new type to be registered, use MetadataStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: MetadataStruct| MetadataStructSerialized): Metadata}} = {
        // This basic registration will happen below [MetadataStruct.FullClassName]: MetadataStruct
    };

    public static register(className: string, ctor: {new (data?: MetadataStruct| MetadataStructSerialized): Metadata}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: MetadataStructSerialized}): Metadata {
        const polymorphicId = Object.keys(data)[0];
        const ctor = MetadataStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for MetadataStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(MetadataStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in MetadataStruct._knownPolymorphic;
    }
}

MetadataStruct.register(MetadataStruct.FullClassName, MetadataStruct);

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorMixinObject,
    IIntrospectorDataObject
} from '../../irt';
Introspector.register('idltest.inheritance.Metadata', {
        full: 'idltest.inheritance.Metadata',
        short: 'Metadata',
        package: 'idltest.inheritance',
        type: IntrospectorTypes.Mixin,
        ctor: () => new MetadataStruct(),
        fields: [
            {
                name: 'id',
                accessName: 'id',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'name',
                accessName: 'name',
                type: {intro: IntrospectorTypes.Str}
            }
        ],
        implementations: MetadataStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(MetadataStruct.FullClassName, {
        full: MetadataStruct.FullClassName,
        short: MetadataStruct.ClassName,
        package: MetadataStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new MetadataStruct(),
        fields: [
            {
                name: 'id',
                accessName: 'id',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'name',
                accessName: 'name',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);