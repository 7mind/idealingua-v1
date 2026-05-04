// Auto-generated, any modifications may be overwritten in the future.

// Identifiable Interface
export interface Identifiable {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): IdentifiableStructSerialized;

    id: string;
}

export interface IdentifiableStructSerialized {
    id: string;
}

export class IdentifiableStruct implements Identifiable {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.upcasts.Identifiable';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.upcasts.Identifiable.Struct';

    public getPackageName(): string { return IdentifiableStruct.PackageName; }
    public getClassName(): string { return IdentifiableStruct.ClassName; }
    public getFullClassName(): string { return IdentifiableStruct.FullClassName; }

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

    constructor(data: IdentifiableStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.id = data.id;
    }

    public serialize(): IdentifiableStructSerialized {
        return {
            id: this.id
        };
    }

    // Polymorphic section below. If a new type to be registered, use IdentifiableStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: IdentifiableStruct| IdentifiableStructSerialized): Identifiable}} = {
        // This basic registration will happen below [IdentifiableStruct.FullClassName]: IdentifiableStruct
    };

    public static register(className: string, ctor: {new (data?: IdentifiableStruct| IdentifiableStructSerialized): Identifiable}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: IdentifiableStructSerialized}): Identifiable {
        const polymorphicId = Object.keys(data)[0];
        const ctor = IdentifiableStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for IdentifiableStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(IdentifiableStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in IdentifiableStruct._knownPolymorphic;
    }
}

IdentifiableStruct.register(IdentifiableStruct.FullClassName, IdentifiableStruct);

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
Introspector.register('idltest.upcasts.Identifiable', {
        full: 'idltest.upcasts.Identifiable',
        short: 'Identifiable',
        package: 'idltest.upcasts',
        type: IntrospectorTypes.Mixin,
        ctor: () => new IdentifiableStruct(),
        fields: [
            {
                name: 'id',
                accessName: 'id',
                type: {intro: IntrospectorTypes.Uid}
            }
        ],
        implementations: IdentifiableStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(IdentifiableStruct.FullClassName, {
        full: IdentifiableStruct.FullClassName,
        short: IdentifiableStruct.ClassName,
        package: IdentifiableStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new IdentifiableStruct(),
        fields: [
            {
                name: 'id',
                accessName: 'id',
                type: {intro: IntrospectorTypes.Uid}
            }
        ]
    } as IIntrospectorDataObject
);