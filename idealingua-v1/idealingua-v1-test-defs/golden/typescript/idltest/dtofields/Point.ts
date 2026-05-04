// Auto-generated, any modifications may be overwritten in the future.
import {
    WHPair,
    WHPairStruct,
    WHPairStructSerialized
} from './WHPair';
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
export class Point implements Metadata, WHPair  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.dtofields';
    public static readonly ClassName = 'Point';
    public static readonly FullClassName = 'idltest.dtofields.Point';

    public getPackageName(): string { return Point.PackageName; }
    public getClassName(): string { return Point.ClassName; }
    public getFullClassName(): string { return Point.FullClassName; }

    private _w: number;
    private _h: number;
    private _id: string;
    private _name: string;
    private _x: number;
    private _y: number;
    private _ownfield: string;
    private _export: boolean;

    public get w(): number {
        return this._w;
    }

    public set w(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field w is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field w expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field w is expected to be an integer, got ' + value);
        }

        this._w = value;
    }

    public get h(): number {
        return this._h;
    }

    public set h(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field h is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field h expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field h is expected to be an integer, got ' + value);
        }

        this._h = value;
    }

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

    public get ownfield(): string {
        return this._ownfield;
    }

    public set ownfield(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field ownfield is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field ownfield expects type string, got ' + value);
        }

        this._ownfield = value;
    }

    public get export_(): boolean {
        return this._export;
    }

    public set export_(value: boolean) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field export_ is not optional');
        }

        if (typeof value !== 'boolean') {
            throw new Error('Field export_ expects boolean type, got ' + value);
        }

        this._export = value;
    }

    constructor(data: PointSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.w = data.w;
        this.h = data.h;
        this.id = data.id;
        this.name = data.name;
        this.x = data.x;
        this.y = data.y;
        this.ownfield = data.ownfield;
        this.export_ = data.export;
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

    public toWHPairSerialized(): WHPairStructSerialized {
        return {
            w: this.w,
            h: this.h
        };
    }

    public toWHPair(): WHPairStruct {
        return new WHPairStruct(this.toWHPairSerialized());
    }

    public loadMetadataSerialized(slice: MetadataStructSerialized) {
        this.id = slice.id;
        this.name = slice.name;
    }

    public loadMetadata(slice: MetadataStruct) {
        this.loadMetadataSerialized(slice.serialize());
    }

    public loadWHPairSerialized(slice: WHPairStructSerialized) {
        this.w = slice.w;
        this.h = slice.h;
    }

    public loadWHPair(slice: WHPairStruct) {
        this.loadWHPairSerialized(slice.serialize());
    }

    public serialize(): PointSerialized {
        return {
            w: this.w,
            h: this.h,
            id: this.id,
            name: this.name,
            x: this.x,
            y: this.y,
            ownfield: this.ownfield,
            export: this.export_
        };
    }
}

export interface PointSerialized extends MetadataStructSerialized, WHPairStructSerialized  {
    w: number;
    h: number;
    id: string;
    name: string;
    x: number;
    y: number;
    ownfield: string;
    export: boolean;
}

MetadataStruct.register(Point.FullClassName, Point);
WHPairStruct.register(Point.FullClassName, Point);

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
                name: 'w',
                accessName: 'w',
                type: {intro: IntrospectorTypes.I32}
            },
            {
                name: 'h',
                accessName: 'h',
                type: {intro: IntrospectorTypes.I32}
            },
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
            },
            {
                name: 'ownfield',
                accessName: 'ownfield',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'export',
                accessName: 'export_',
                type: {intro: IntrospectorTypes.Bool}
            }
        ]
    } as IIntrospectorDataObject
);