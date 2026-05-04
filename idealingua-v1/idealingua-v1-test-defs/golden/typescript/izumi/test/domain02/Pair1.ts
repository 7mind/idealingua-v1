// Auto-generated, any modifications may be overwritten in the future.

// Pair1 Interface
export interface Pair1 {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): Pair1StructSerialized;

    x: string;
    y: string;
}

export interface Pair1StructSerialized {
    x: string;
    y: string;
}

export class Pair1Struct implements Pair1 {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain02.Pair1';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'izumi.test.domain02.Pair1.Struct';

    public getPackageName(): string { return Pair1Struct.PackageName; }
    public getClassName(): string { return Pair1Struct.ClassName; }
    public getFullClassName(): string { return Pair1Struct.FullClassName; }

    private _x: string;
    private _y: string;

    public get x(): string {
        return this._x;
    }

    public set x(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field x is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field x expects type string, got ' + value);
        }

        this._x = value;
    }

    public get y(): string {
        return this._y;
    }

    public set y(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field y is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field y expects type string, got ' + value);
        }

        this._y = value;
    }

    constructor(data: Pair1StructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.x = data.x;
        this.y = data.y;
    }

    public serialize(): Pair1StructSerialized {
        return {
            x: this.x,
            y: this.y
        };
    }

    // Polymorphic section below. If a new type to be registered, use Pair1Struct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: Pair1Struct| Pair1StructSerialized): Pair1}} = {
        // This basic registration will happen below [Pair1Struct.FullClassName]: Pair1Struct
    };

    public static register(className: string, ctor: {new (data?: Pair1Struct| Pair1StructSerialized): Pair1}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: Pair1StructSerialized}): Pair1 {
        const polymorphicId = Object.keys(data)[0];
        const ctor = Pair1Struct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for Pair1Struct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(Pair1Struct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in Pair1Struct._knownPolymorphic;
    }
}

Pair1Struct.register(Pair1Struct.FullClassName, Pair1Struct);

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorMixinObject,
    IIntrospectorDataObject
} from '../../../irt';
Introspector.register('izumi.test.domain02.Pair1', {
        full: 'izumi.test.domain02.Pair1',
        short: 'Pair1',
        package: 'izumi.test.domain02',
        type: IntrospectorTypes.Mixin,
        ctor: () => new Pair1Struct(),
        fields: [
            {
                name: 'x',
                accessName: 'x',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'y',
                accessName: 'y',
                type: {intro: IntrospectorTypes.Str}
            }
        ],
        implementations: Pair1Struct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(Pair1Struct.FullClassName, {
        full: Pair1Struct.FullClassName,
        short: Pair1Struct.ClassName,
        package: Pair1Struct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new Pair1Struct(),
        fields: [
            {
                name: 'x',
                accessName: 'x',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'y',
                accessName: 'y',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);