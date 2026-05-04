// Auto-generated, any modifications may be overwritten in the future.

// M0 Interface
export interface M0 {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): M0StructSerialized;

    value: string;
}

export interface M0StructSerialized {
    value: string;
}

export class M0Struct implements M0 {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.clones.M0';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.clones.M0.Struct';

    public getPackageName(): string { return M0Struct.PackageName; }
    public getClassName(): string { return M0Struct.ClassName; }
    public getFullClassName(): string { return M0Struct.FullClassName; }

    private _value: string;

    public get value(): string {
        return this._value;
    }

    public set value(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field value is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field value expects type string, got ' + value);
        }

        this._value = value;
    }

    constructor(data: M0StructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.value = data.value;
    }

    public serialize(): M0StructSerialized {
        return {
            value: this.value
        };
    }

    // Polymorphic section below. If a new type to be registered, use M0Struct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: M0Struct| M0StructSerialized): M0}} = {
        // This basic registration will happen below [M0Struct.FullClassName]: M0Struct
    };

    public static register(className: string, ctor: {new (data?: M0Struct| M0StructSerialized): M0}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: M0StructSerialized}): M0 {
        const polymorphicId = Object.keys(data)[0];
        const ctor = M0Struct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for M0Struct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(M0Struct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in M0Struct._knownPolymorphic;
    }
}

M0Struct.register(M0Struct.FullClassName, M0Struct);

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
Introspector.register('idltest.clones.M0', {
        full: 'idltest.clones.M0',
        short: 'M0',
        package: 'idltest.clones',
        type: IntrospectorTypes.Mixin,
        ctor: () => new M0Struct(),
        fields: [
            {
                name: 'value',
                accessName: 'value',
                type: {intro: IntrospectorTypes.Str}
            }
        ],
        implementations: M0Struct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(M0Struct.FullClassName, {
        full: M0Struct.FullClassName,
        short: M0Struct.ClassName,
        package: M0Struct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new M0Struct(),
        fields: [
            {
                name: 'value',
                accessName: 'value',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);