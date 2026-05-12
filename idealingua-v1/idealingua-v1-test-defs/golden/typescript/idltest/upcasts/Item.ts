// Auto-generated, any modifications may be overwritten in the future.
import {
    ItemContentStruct,
    ItemContentStructSerialized
} from './ItemContent';
import {
    IdentifiableStruct,
    IdentifiableStructSerialized
} from './Identifiable';

// Item DTO
export class Item  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.upcasts';
    public static readonly ClassName = 'Item';
    public static readonly FullClassName = 'idltest.upcasts.Item';

    public getPackageName(): string { return Item.PackageName; }
    public getClassName(): string { return Item.ClassName; }
    public getFullClassName(): string { return Item.FullClassName; }

    private _id: string;
    private _name: string;
    private _price: number;

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

        if (!value.match('^[0-9a-fA-f]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$')) {
            throw new Error('Field id expects guid format, got ' + value);
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

    public get price(): number {
        return this._price;
    }

    public set price(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field price is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field price expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field price is expected to be an integer, got ' + value);
        }

        this._price = value;
    }

    constructor(data: ItemSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.id = data.id;
        this.name = data.name;
        this.price = data.price;
    }

    public serialize(): ItemSerialized {
        return {
            id: this.id,
            name: this.name,
            price: this.price
        };
    }
}

export interface ItemSerialized  {
    id: string;
    name: string;
    price: number;
}

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorDataObject
} from '../../irt';
Introspector.register(Item.FullClassName, {
        full: Item.FullClassName,
        short: Item.ClassName,
        package: Item.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new Item(),
        fields: [
            {
                name: 'price',
                accessName: 'price',
                type: {intro: IntrospectorTypes.I32}
            },
            {
                name: 'name',
                accessName: 'name',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'id',
                accessName: 'id',
                type: {intro: IntrospectorTypes.Uid}
            }
        ]
    } as IIntrospectorDataObject
);