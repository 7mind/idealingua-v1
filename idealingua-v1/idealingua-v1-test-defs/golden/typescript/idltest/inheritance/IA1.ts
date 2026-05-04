// Auto-generated, any modifications may be overwritten in the future.

// IA1 Interface
export interface IA1 {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): IA1StructSerialized;

    Int: number;
}

export interface IA1StructSerialized {
    Int: number;
}

export class IA1Struct implements IA1 {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.inheritance.IA1';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.inheritance.IA1.Struct';

    public getPackageName(): string { return IA1Struct.PackageName; }
    public getClassName(): string { return IA1Struct.ClassName; }
    public getFullClassName(): string { return IA1Struct.FullClassName; }

    private _Int: number;

    public get Int(): number {
        return this._Int;
    }

    public set Int(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field Int is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field Int expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field Int is expected to be an integer, got ' + value);
        }

        this._Int = value;
    }

    constructor(data: IA1StructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.Int = data.Int;
    }

    public serialize(): IA1StructSerialized {
        return {
            Int: this.Int
        };
    }

    // Polymorphic section below. If a new type to be registered, use IA1Struct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: IA1Struct| IA1StructSerialized): IA1}} = {
        // This basic registration will happen below [IA1Struct.FullClassName]: IA1Struct
    };

    public static register(className: string, ctor: {new (data?: IA1Struct| IA1StructSerialized): IA1}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: IA1StructSerialized}): IA1 {
        const polymorphicId = Object.keys(data)[0];
        const ctor = IA1Struct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for IA1Struct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(IA1Struct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in IA1Struct._knownPolymorphic;
    }
}

IA1Struct.register(IA1Struct.FullClassName, IA1Struct);

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
Introspector.register('idltest.inheritance.IA1', {
        full: 'idltest.inheritance.IA1',
        short: 'IA1',
        package: 'idltest.inheritance',
        type: IntrospectorTypes.Mixin,
        ctor: () => new IA1Struct(),
        fields: [
            {
                name: 'Int',
                accessName: 'Int',
                type: {intro: IntrospectorTypes.I32}
            }
        ],
        implementations: IA1Struct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(IA1Struct.FullClassName, {
        full: IA1Struct.FullClassName,
        short: IA1Struct.ClassName,
        package: IA1Struct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new IA1Struct(),
        fields: [
            {
                name: 'Int',
                accessName: 'Int',
                type: {intro: IntrospectorTypes.I32}
            }
        ]
    } as IIntrospectorDataObject
);