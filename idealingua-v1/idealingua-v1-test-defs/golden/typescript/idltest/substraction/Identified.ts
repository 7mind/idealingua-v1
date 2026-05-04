// Auto-generated, any modifications may be overwritten in the future.

// Identified Interface
export interface Identified {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): IdentifiedStructSerialized;

    id: string;
}

export interface IdentifiedStructSerialized {
    id: string;
}

export class IdentifiedStruct implements Identified {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.substraction.Identified';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.substraction.Identified.Struct';

    public getPackageName(): string { return IdentifiedStruct.PackageName; }
    public getClassName(): string { return IdentifiedStruct.ClassName; }
    public getFullClassName(): string { return IdentifiedStruct.FullClassName; }

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

    constructor(data: IdentifiedStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.id = data.id;
    }

    public serialize(): IdentifiedStructSerialized {
        return {
            id: this.id
        };
    }

    // Polymorphic section below. If a new type to be registered, use IdentifiedStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: IdentifiedStruct| IdentifiedStructSerialized): Identified}} = {
        // This basic registration will happen below [IdentifiedStruct.FullClassName]: IdentifiedStruct
    };

    public static register(className: string, ctor: {new (data?: IdentifiedStruct| IdentifiedStructSerialized): Identified}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: IdentifiedStructSerialized}): Identified {
        const polymorphicId = Object.keys(data)[0];
        const ctor = IdentifiedStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for IdentifiedStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(IdentifiedStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in IdentifiedStruct._knownPolymorphic;
    }
}

IdentifiedStruct.register(IdentifiedStruct.FullClassName, IdentifiedStruct);

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
Introspector.register('idltest.substraction.Identified', {
        full: 'idltest.substraction.Identified',
        short: 'Identified',
        package: 'idltest.substraction',
        type: IntrospectorTypes.Mixin,
        ctor: () => new IdentifiedStruct(),
        fields: [
            {
                name: 'id',
                accessName: 'id',
                type: {intro: IntrospectorTypes.Str}
            }
        ],
        implementations: IdentifiedStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(IdentifiedStruct.FullClassName, {
        full: IdentifiedStruct.FullClassName,
        short: IdentifiedStruct.ClassName,
        package: IdentifiedStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new IdentifiedStruct(),
        fields: [
            {
                name: 'id',
                accessName: 'id',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);