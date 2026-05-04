// Auto-generated, any modifications may be overwritten in the future.
import {
    IdentifiableStruct,
    IdentifiableStructSerialized
} from './Identifiable';

// ItemContent Interface
export interface ItemContent {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): ItemContentStructSerialized;

    id: string;
    name: string;
}

export interface ItemContentStructSerialized {
    id: string;
    name: string;
}

export class ItemContentStruct implements ItemContent {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.upcasts.ItemContent';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.upcasts.ItemContent.Struct';

    public getPackageName(): string { return ItemContentStruct.PackageName; }
    public getClassName(): string { return ItemContentStruct.ClassName; }
    public getFullClassName(): string { return ItemContentStruct.FullClassName; }

    private _id: string;
    private _name: string;

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

    constructor(data: ItemContentStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.id = data.id;
        this.name = data.name;
    }

    public serialize(): ItemContentStructSerialized {
        return {
            id: this.id,
            name: this.name
        };
    }

    // Polymorphic section below. If a new type to be registered, use ItemContentStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: ItemContentStruct| ItemContentStructSerialized): ItemContent}} = {
        // This basic registration will happen below [ItemContentStruct.FullClassName]: ItemContentStruct
    };

    public static register(className: string, ctor: {new (data?: ItemContentStruct| ItemContentStructSerialized): ItemContent}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: ItemContentStructSerialized}): ItemContent {
        const polymorphicId = Object.keys(data)[0];
        const ctor = ItemContentStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for ItemContentStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(ItemContentStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in ItemContentStruct._knownPolymorphic;
    }
}

ItemContentStruct.register(ItemContentStruct.FullClassName, ItemContentStruct);

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
Introspector.register('idltest.upcasts.ItemContent', {
        full: 'idltest.upcasts.ItemContent',
        short: 'ItemContent',
        package: 'idltest.upcasts',
        type: IntrospectorTypes.Mixin,
        ctor: () => new ItemContentStruct(),
        fields: [
            {
                name: 'name',
                accessName: 'name',
                type: {intro: IntrospectorTypes.Str}
            }
        ],
        implementations: ItemContentStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(ItemContentStruct.FullClassName, {
        full: ItemContentStruct.FullClassName,
        short: ItemContentStruct.ClassName,
        package: ItemContentStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new ItemContentStruct(),
        fields: [
            {
                name: 'name',
                accessName: 'name',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);