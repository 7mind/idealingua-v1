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

// Point DTO
export class Point implements Metadata  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.inheritance';
    public static readonly ClassName = 'Point';
    public static readonly FullClassName = 'idltest.inheritance.Point';

    public getPackageName(): string { return Point.PackageName; }
    public getClassName(): string { return Point.ClassName; }
    public getFullClassName(): string { return Point.FullClassName; }

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

    constructor(data: PointSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.id = data.id;
        this.name = data.name;
        this.x = data.x;
        this.y = data.y;
    }

    public toMetadataSerialized(): MetadataStructSerialized {
        return {
            id: this.id,
            name: this.name
        };
    }

    public toMetadata(): MetadataStruct {
        return new MetadataStruct(this.toMetadataSerialized());
    }

    public loadMetadataSerialized(slice: MetadataStructSerialized) {
        this.id = slice.id;
        this.name = slice.name;
    }

    public loadMetadata(slice: MetadataStruct) {
        this.loadMetadataSerialized(slice.serialize());
    }

    public serialize(): PointSerialized {
        return {
            id: this.id,
            name: this.name,
            x: this.x,
            y: this.y
        };
    }
}

export interface PointSerialized extends MetadataStructSerialized  {
    id: string;
    name: string;
    x: number;
    y: number;
}

MetadataStruct.register(Point.FullClassName, Point);

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorDataObject
} from '../../irt';
Introspector.register(Point.FullClassName, {
        full: Point.FullClassName,
        short: Point.ClassName,
        package: Point.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new Point(),
        fields: [
            {
                name: 'id',
                accessName: 'id',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'name',
                accessName: 'name',
                type: {intro: IntrospectorTypes.Str}
            },
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