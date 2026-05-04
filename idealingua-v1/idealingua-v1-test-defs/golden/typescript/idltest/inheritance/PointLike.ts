// Auto-generated, any modifications may be overwritten in the future.
import {
    Metadata,
    MetadataStruct,
    MetadataStructSerialized
} from './Metadata';
import {
    IntPairStruct,
    IntPairStructSerialized
} from './IntPair';

// PointLike Interface
export interface PointLike extends Metadata {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): PointLikeStructSerialized;

    id: string;
    name: string;
    x: number;
    y: number;
}

export interface PointLikeStructSerialized extends MetadataStructSerialized {
    id: string;
    name: string;
    x: number;
    y: number;
}

export class PointLikeStruct implements PointLike {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.inheritance.PointLike';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.inheritance.PointLike.Struct';

    public getPackageName(): string { return PointLikeStruct.PackageName; }
    public getClassName(): string { return PointLikeStruct.ClassName; }
    public getFullClassName(): string { return PointLikeStruct.FullClassName; }

    private _id: string;
    private _name: string;
    private _x: number;
    private _y: number;

    public get id(): string {
        return this._id;
    }

    public set id(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field id is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field id expects type string, got ' + value);
        }

        this._id = value;
    }

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

    constructor(data: PointLikeStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.id = data.id;
        this.name = data.name;
        this.x = data.x;
        this.y = data.y;
    }

    public serialize(): PointLikeStructSerialized {
        return {
            id: this.id,
            name: this.name,
            x: this.x,
            y: this.y
        };
    }

    // Polymorphic section below. If a new type to be registered, use PointLikeStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: PointLikeStruct| PointLikeStructSerialized): PointLike}} = {
        // This basic registration will happen below [PointLikeStruct.FullClassName]: PointLikeStruct
    };

    public static register(className: string, ctor: {new (data?: PointLikeStruct| PointLikeStructSerialized): PointLike}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: PointLikeStructSerialized}): PointLike {
        const polymorphicId = Object.keys(data)[0];
        const ctor = PointLikeStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for PointLikeStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(PointLikeStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in PointLikeStruct._knownPolymorphic;
    }
}

PointLikeStruct.register(PointLikeStruct.FullClassName, PointLikeStruct);
MetadataStruct.register(PointLikeStruct.FullClassName, PointLikeStruct);

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
Introspector.register('idltest.inheritance.PointLike', {
        full: 'idltest.inheritance.PointLike',
        short: 'PointLike',
        package: 'idltest.inheritance',
        type: IntrospectorTypes.Mixin,
        ctor: () => new PointLikeStruct(),
        fields: [

        ],
        implementations: PointLikeStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(PointLikeStruct.FullClassName, {
        full: PointLikeStruct.FullClassName,
        short: PointLikeStruct.ClassName,
        package: PointLikeStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new PointLikeStruct(),
        fields: [

        ]
    } as IIntrospectorDataObject
);