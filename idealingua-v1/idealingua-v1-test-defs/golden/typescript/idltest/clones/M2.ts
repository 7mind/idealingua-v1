// Auto-generated, any modifications may be overwritten in the future.
import {
    M0Struct,
    M0StructSerialized
} from './M0';

// M2 Interface
export interface M2 {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): M2StructSerialized;

    value: string;
    str: string;
    i32: number;
}

export interface M2StructSerialized {
    value: string;
    str: string;
    i32: number;
}

export class M2Struct implements M2 {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.clones.M2';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.clones.M2.Struct';

    public getPackageName(): string { return M2Struct.PackageName; }
    public getClassName(): string { return M2Struct.ClassName; }
    public getFullClassName(): string { return M2Struct.FullClassName; }

    private _value: string;
    private _str: string;
    private _i32: number;

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

    public get str(): string {
        return this._str;
    }

    public set str(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field str is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field str expects type string, got ' + value);
        }

        this._str = value;
    }

    public get i32(): number {
        return this._i32;
    }

    public set i32(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field i32 is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field i32 expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field i32 is expected to be an integer, got ' + value);
        }

        this._i32 = value;
    }

    constructor(data: M2StructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.value = data.value;
        this.str = data.str;
        this.i32 = data.i32;
    }

    public serialize(): M2StructSerialized {
        return {
            value: this.value,
            str: this.str,
            i32: this.i32
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
Introspector.register('idltest.clones.M2', {
        full: 'idltest.clones.M2',
        short: 'M2',
        package: 'idltest.clones',
        type: IntrospectorTypes.Mixin,
        ctor: () => new M2Struct(),
        fields: [
            {
                name: 'str',
                accessName: 'str',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'i32',
                accessName: 'i32',
                type: {intro: IntrospectorTypes.I32}
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
                name: 'str',
                accessName: 'str',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'i32',
                accessName: 'i32',
                type: {intro: IntrospectorTypes.I32}
            }
        ]
    } as IIntrospectorDataObject
);