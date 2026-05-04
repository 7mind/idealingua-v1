// Auto-generated, any modifications may be overwritten in the future.

// Name Interface
export interface Name {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): NameStructSerialized;

    name: string;
}

export interface NameStructSerialized {
    name: string;
}

export class NameStruct implements Name {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.phase.Name';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.phase.Name.Struct';

    public getPackageName(): string { return NameStruct.PackageName; }
    public getClassName(): string { return NameStruct.ClassName; }
    public getFullClassName(): string { return NameStruct.FullClassName; }

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

    constructor(data: NameStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.name = data.name;
    }

    public serialize(): NameStructSerialized {
        return {
            name: this.name
        };
    }

    // Polymorphic section below. If a new type to be registered, use NameStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: NameStruct| NameStructSerialized): Name}} = {
        // This basic registration will happen below [NameStruct.FullClassName]: NameStruct
    };

    public static register(className: string, ctor: {new (data?: NameStruct| NameStructSerialized): Name}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: NameStructSerialized}): Name {
        const polymorphicId = Object.keys(data)[0];
        const ctor = NameStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for NameStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(NameStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in NameStruct._knownPolymorphic;
    }
}

NameStruct.register(NameStruct.FullClassName, NameStruct);

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
Introspector.register('idltest.phase.Name', {
        full: 'idltest.phase.Name',
        short: 'Name',
        package: 'idltest.phase',
        type: IntrospectorTypes.Mixin,
        ctor: () => new NameStruct(),
        fields: [
            {
                name: 'name',
                accessName: 'name',
                type: {intro: IntrospectorTypes.Str}
            }
        ],
        implementations: NameStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(NameStruct.FullClassName, {
        full: NameStruct.FullClassName,
        short: NameStruct.ClassName,
        package: NameStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new NameStruct(),
        fields: [
            {
                name: 'name',
                accessName: 'name',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);