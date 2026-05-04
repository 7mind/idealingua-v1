// Auto-generated, any modifications may be overwritten in the future.

// Pair2 Interface
export interface Pair2 {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): Pair2StructSerialized;

    y: string;
    x: string;
}

export interface Pair2StructSerialized {
    y: string;
    x: string;
}

export class Pair2Struct implements Pair2 {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain02.Pair2';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'izumi.test.domain02.Pair2.Struct';

    public getPackageName(): string { return Pair2Struct.PackageName; }
    public getClassName(): string { return Pair2Struct.ClassName; }
    public getFullClassName(): string { return Pair2Struct.FullClassName; }

    private _y: string;
    private _x: string;

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

    constructor(data: Pair2StructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.y = data.y;
        this.x = data.x;
    }

    public serialize(): Pair2StructSerialized {
        return {
            y: this.y,
            x: this.x
        };
    }

    // Polymorphic section below. If a new type to be registered, use Pair2Struct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: Pair2Struct| Pair2StructSerialized): Pair2}} = {
        // This basic registration will happen below [Pair2Struct.FullClassName]: Pair2Struct
    };

    public static register(className: string, ctor: {new (data?: Pair2Struct| Pair2StructSerialized): Pair2}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: Pair2StructSerialized}): Pair2 {
        const polymorphicId = Object.keys(data)[0];
        const ctor = Pair2Struct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for Pair2Struct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(Pair2Struct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in Pair2Struct._knownPolymorphic;
    }
}

Pair2Struct.register(Pair2Struct.FullClassName, Pair2Struct);

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
Introspector.register('izumi.test.domain02.Pair2', {
        full: 'izumi.test.domain02.Pair2',
        short: 'Pair2',
        package: 'izumi.test.domain02',
        type: IntrospectorTypes.Mixin,
        ctor: () => new Pair2Struct(),
        fields: [
            {
                name: 'y',
                accessName: 'y',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'x',
                accessName: 'x',
                type: {intro: IntrospectorTypes.Str}
            }
        ],
        implementations: Pair2Struct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(Pair2Struct.FullClassName, {
        full: Pair2Struct.FullClassName,
        short: Pair2Struct.ClassName,
        package: Pair2Struct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new Pair2Struct(),
        fields: [
            {
                name: 'y',
                accessName: 'y',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'x',
                accessName: 'x',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);