// Auto-generated, any modifications may be overwritten in the future.

// M1 Interface
export interface M1 {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): M1StructSerialized;

    value: string;
}

export interface M1StructSerialized {
    value: string;
}

export class M1Struct implements M1 {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.aliases.M1';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.aliases.M1.Struct';

    public getPackageName(): string { return M1Struct.PackageName; }
    public getClassName(): string { return M1Struct.ClassName; }
    public getFullClassName(): string { return M1Struct.FullClassName; }

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

    constructor(data: M1StructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.value = data.value;
    }

    public serialize(): M1StructSerialized {
        return {
            value: this.value
        };
    }

    // Polymorphic section below. If a new type to be registered, use M1Struct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: M1Struct| M1StructSerialized): M1}} = {
        // This basic registration will happen below [M1Struct.FullClassName]: M1Struct
    };

    public static register(className: string, ctor: {new (data?: M1Struct| M1StructSerialized): M1}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: M1StructSerialized}): M1 {
        const polymorphicId = Object.keys(data)[0];
        const ctor = M1Struct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for M1Struct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(M1Struct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in M1Struct._knownPolymorphic;
    }
}

M1Struct.register(M1Struct.FullClassName, M1Struct);

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
Introspector.register('idltest.aliases.M1', {
        full: 'idltest.aliases.M1',
        short: 'M1',
        package: 'idltest.aliases',
        type: IntrospectorTypes.Mixin,
        ctor: () => new M1Struct(),
        fields: [
            {
                name: 'value',
                accessName: 'value',
                type: {intro: IntrospectorTypes.Str}
            }
        ],
        implementations: M1Struct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(M1Struct.FullClassName, {
        full: M1Struct.FullClassName,
        short: M1Struct.ClassName,
        package: M1Struct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new M1Struct(),
        fields: [
            {
                name: 'value',
                accessName: 'value',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);