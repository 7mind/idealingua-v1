// Auto-generated, any modifications may be overwritten in the future.
import {
    IA1,
    IA1Struct,
    IA1StructSerialized
} from './IA1';

// IA2 Interface
export interface IA2 extends IA1 {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): IA2StructSerialized;

    Int: number;
}

export interface IA2StructSerialized extends IA1StructSerialized {
    Int: number;
}

export class IA2Struct implements IA2 {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.inheritance.IA2';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.inheritance.IA2.Struct';

    public getPackageName(): string { return IA2Struct.PackageName; }
    public getClassName(): string { return IA2Struct.ClassName; }
    public getFullClassName(): string { return IA2Struct.FullClassName; }

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

    constructor(data: IA2StructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.Int = data.Int;
    }

    public serialize(): IA2StructSerialized {
        return {
            Int: this.Int
        };
    }

    // Polymorphic section below. If a new type to be registered, use IA2Struct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: IA2Struct| IA2StructSerialized): IA2}} = {
        // This basic registration will happen below [IA2Struct.FullClassName]: IA2Struct
    };

    public static register(className: string, ctor: {new (data?: IA2Struct| IA2StructSerialized): IA2}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: IA2StructSerialized}): IA2 {
        const polymorphicId = Object.keys(data)[0];
        const ctor = IA2Struct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for IA2Struct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(IA2Struct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in IA2Struct._knownPolymorphic;
    }
}

IA2Struct.register(IA2Struct.FullClassName, IA2Struct);
IA1Struct.register(IA2Struct.FullClassName, IA2Struct);

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
Introspector.register('idltest.inheritance.IA2', {
        full: 'idltest.inheritance.IA2',
        short: 'IA2',
        package: 'idltest.inheritance',
        type: IntrospectorTypes.Mixin,
        ctor: () => new IA2Struct(),
        fields: [
            {
                name: 'Int',
                accessName: 'Int',
                type: {intro: IntrospectorTypes.I32}
            }
        ],
        implementations: IA2Struct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(IA2Struct.FullClassName, {
        full: IA2Struct.FullClassName,
        short: IA2Struct.ClassName,
        package: IA2Struct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new IA2Struct(),
        fields: [
            {
                name: 'Int',
                accessName: 'Int',
                type: {intro: IntrospectorTypes.I32}
            }
        ]
    } as IIntrospectorDataObject
);