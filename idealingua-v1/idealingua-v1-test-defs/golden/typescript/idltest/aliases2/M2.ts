// Auto-generated, any modifications may be overwritten in the future.

// M2 Interface
export interface M2 {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): M2StructSerialized;

    f2: string;
}

export interface M2StructSerialized {
    f2: string;
}

export class M2Struct implements M2 {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.aliases2.M2';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.aliases2.M2.Struct';

    public getPackageName(): string { return M2Struct.PackageName; }
    public getClassName(): string { return M2Struct.ClassName; }
    public getFullClassName(): string { return M2Struct.FullClassName; }

    private _f2: string;

    public get f2(): string {
        return this._f2;
    }

    public set f2(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field f2 is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field f2 expects type string, got ' + value);
        }

        this._f2 = value;
    }

    constructor(data: M2StructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.f2 = data.f2;
    }

    public serialize(): M2StructSerialized {
        return {
            f2: this.f2
        };
    }

    // Polymorphic section below. If a new type to be registered, use M2Struct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: M2Struct| M2StructSerialized): M2}} = {
        // This basic registration will happen below [M2Struct.FullClassName]: M2Struct
    };

    public static register(className: string, ctor: {new (data?: M2Struct| M2StructSerialized): M2}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: M2StructSerialized}): M2 {
        const polymorphicId = Object.keys(data)[0];
        const ctor = M2Struct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for M2Struct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(M2Struct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in M2Struct._knownPolymorphic;
    }
}

M2Struct.register(M2Struct.FullClassName, M2Struct);

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
Introspector.register('idltest.aliases2.M2', {
        full: 'idltest.aliases2.M2',
        short: 'M2',
        package: 'idltest.aliases2',
        type: IntrospectorTypes.Mixin,
        ctor: () => new M2Struct(),
        fields: [
            {
                name: 'f2',
                accessName: 'f2',
                type: {intro: IntrospectorTypes.Str}
            }
        ],
        implementations: M2Struct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(M2Struct.FullClassName, {
        full: M2Struct.FullClassName,
        short: M2Struct.ClassName,
        package: M2Struct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new M2Struct(),
        fields: [
            {
                name: 'f2',
                accessName: 'f2',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);