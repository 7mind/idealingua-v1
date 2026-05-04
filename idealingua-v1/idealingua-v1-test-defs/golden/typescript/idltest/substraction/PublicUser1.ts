// Auto-generated, any modifications may be overwritten in the future.
import {
    User1Struct,
    User1StructSerialized
} from './User1';

// PublicUser1 Interface
export interface PublicUser1 {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): PublicUser1StructSerialized;

    name: string;
}

export interface PublicUser1StructSerialized {
    name: string;
}

export class PublicUser1Struct implements PublicUser1 {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.substraction.PublicUser1';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.substraction.PublicUser1.Struct';

    public getPackageName(): string { return PublicUser1Struct.PackageName; }
    public getClassName(): string { return PublicUser1Struct.ClassName; }
    public getFullClassName(): string { return PublicUser1Struct.FullClassName; }

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

    constructor(data: PublicUser1StructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.name = data.name;
    }

    public serialize(): PublicUser1StructSerialized {
        return {
            name: this.name
        };
    }

    // Polymorphic section below. If a new type to be registered, use PublicUser1Struct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: PublicUser1Struct| PublicUser1StructSerialized): PublicUser1}} = {
        // This basic registration will happen below [PublicUser1Struct.FullClassName]: PublicUser1Struct
    };

    public static register(className: string, ctor: {new (data?: PublicUser1Struct| PublicUser1StructSerialized): PublicUser1}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: PublicUser1StructSerialized}): PublicUser1 {
        const polymorphicId = Object.keys(data)[0];
        const ctor = PublicUser1Struct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for PublicUser1Struct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(PublicUser1Struct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in PublicUser1Struct._knownPolymorphic;
    }
}

PublicUser1Struct.register(PublicUser1Struct.FullClassName, PublicUser1Struct);

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
Introspector.register('idltest.substraction.PublicUser1', {
        full: 'idltest.substraction.PublicUser1',
        short: 'PublicUser1',
        package: 'idltest.substraction',
        type: IntrospectorTypes.Mixin,
        ctor: () => new PublicUser1Struct(),
        fields: [

        ],
        implementations: PublicUser1Struct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(PublicUser1Struct.FullClassName, {
        full: PublicUser1Struct.FullClassName,
        short: PublicUser1Struct.ClassName,
        package: PublicUser1Struct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new PublicUser1Struct(),
        fields: [

        ]
    } as IIntrospectorDataObject
);