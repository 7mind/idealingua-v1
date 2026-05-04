// Auto-generated, any modifications may be overwritten in the future.

// IntPair Interface
export interface IntPair {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): IntPairStructSerialized;

    x: number;
    y: number;
}

export interface IntPairStructSerialized {
    x: number;
    y: number;
}

export class IntPairStruct implements IntPair {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.dtofields.IntPair';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.dtofields.IntPair.Struct';

    public getPackageName(): string { return IntPairStruct.PackageName; }
    public getClassName(): string { return IntPairStruct.ClassName; }
    public getFullClassName(): string { return IntPairStruct.FullClassName; }

    private _x: number;
    private _y: number;

    public get x(): number {
        return this._x;
    }

    public set x(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field x is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field x expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field x is expected to be an integer, got ' + value);
        }

        this._x = value;
    }

    public get y(): number {
        return this._y;
    }

    public set y(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field y is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field y expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field y is expected to be an integer, got ' + value);
        }

        this._y = value;
    }

    constructor(data: IntPairStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.x = data.x;
        this.y = data.y;
    }

    public serialize(): IntPairStructSerialized {
        return {
            x: this.x,
            y: this.y
        };
    }

    // Polymorphic section below. If a new type to be registered, use IntPairStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: IntPairStruct| IntPairStructSerialized): IntPair}} = {
        // This basic registration will happen below [IntPairStruct.FullClassName]: IntPairStruct
    };

    public static register(className: string, ctor: {new (data?: IntPairStruct| IntPairStructSerialized): IntPair}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: IntPairStructSerialized}): IntPair {
        const polymorphicId = Object.keys(data)[0];
        const ctor = IntPairStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for IntPairStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(IntPairStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in IntPairStruct._knownPolymorphic;
    }
}

IntPairStruct.register(IntPairStruct.FullClassName, IntPairStruct);

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
Introspector.register('idltest.dtofields.IntPair', {
        full: 'idltest.dtofields.IntPair',
        short: 'IntPair',
        package: 'idltest.dtofields',
        type: IntrospectorTypes.Mixin,
        ctor: () => new IntPairStruct(),
        fields: [
            {
                name: 'x',
                accessName: 'x',
                type: {intro: IntrospectorTypes.I32}
            },
            {
                name: 'y',
                accessName: 'y',
                type: {intro: IntrospectorTypes.I32}
            }
        ],
        implementations: IntPairStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(IntPairStruct.FullClassName, {
        full: IntPairStruct.FullClassName,
        short: IntPairStruct.ClassName,
        package: IntPairStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new IntPairStruct(),
        fields: [
            {
                name: 'x',
                accessName: 'x',
                type: {intro: IntrospectorTypes.I32}
            },
            {
                name: 'y',
                accessName: 'y',
                type: {intro: IntrospectorTypes.I32}
            }
        ]
    } as IIntrospectorDataObject
);