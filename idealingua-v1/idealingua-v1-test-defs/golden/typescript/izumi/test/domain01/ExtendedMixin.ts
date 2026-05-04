// Auto-generated, any modifications may be overwritten in the future.
import {
    PrivateMixinParentStruct,
    PrivateMixinParentStructSerialized
} from './PrivateMixinParent';
import {
    PrivateMixinStruct,
    PrivateMixinStructSerialized
} from './PrivateMixin';
import {
    PrivateMixinPrivateParentStruct,
    PrivateMixinPrivateParentStructSerialized
} from './PrivateMixinPrivateParent';

// ExtendedMixin Interface
export interface ExtendedMixin {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): ExtendedMixinStructSerialized;

    parent_embedded: string;
    parent: string;
    embedded: boolean;
    own: number;
}

export interface ExtendedMixinStructSerialized {
    parent_embedded: string;
    parent: string;
    embedded: boolean;
    own: number;
}

export class ExtendedMixinStruct implements ExtendedMixin {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain01.ExtendedMixin';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'izumi.test.domain01.ExtendedMixin.Struct';

    public getPackageName(): string { return ExtendedMixinStruct.PackageName; }
    public getClassName(): string { return ExtendedMixinStruct.ClassName; }
    public getFullClassName(): string { return ExtendedMixinStruct.FullClassName; }

    private _parent_embedded: string;
    private _parent: string;
    private _embedded: boolean;
    private _own: number;

    public get parent_embedded(): string {
        return this._parent_embedded;
    }

    public set parent_embedded(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field parent_embedded is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field parent_embedded expects type string, got ' + value);
        }

        this._parent_embedded = value;
    }

    public get parent(): string {
        return this._parent;
    }

    public set parent(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field parent is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field parent expects type string, got ' + value);
        }

        this._parent = value;
    }

    public get embedded(): boolean {
        return this._embedded;
    }

    public set embedded(value: boolean) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field embedded is not optional');
        }

        if (typeof value !== 'boolean') {
            throw new Error('Field embedded expects boolean type, got ' + value);
        }

        this._embedded = value;
    }

    public get own(): number {
        return this._own;
    }

    public set own(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field own is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field own expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field own is expected to be an integer, got ' + value);
        }

        if (value < -128) {
            throw new Error('Field own is expected to be not less than -128, got ' + value);
        }

        if (value > 127) {
            throw new Error('Field own is expected to be not greater than 127, got ' + value);
        }

        this._own = value;
    }

    constructor(data: ExtendedMixinStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.parent_embedded = data.parent_embedded;
        this.parent = data.parent;
        this.embedded = data.embedded;
        this.own = data.own;
    }

    public serialize(): ExtendedMixinStructSerialized {
        return {
            parent_embedded: this.parent_embedded,
            parent: this.parent,
            embedded: this.embedded,
            own: this.own
        };
    }

    // Polymorphic section below. If a new type to be registered, use ExtendedMixinStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: ExtendedMixinStruct| ExtendedMixinStructSerialized): ExtendedMixin}} = {
        // This basic registration will happen below [ExtendedMixinStruct.FullClassName]: ExtendedMixinStruct
    };

    public static register(className: string, ctor: {new (data?: ExtendedMixinStruct| ExtendedMixinStructSerialized): ExtendedMixin}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: ExtendedMixinStructSerialized}): ExtendedMixin {
        const polymorphicId = Object.keys(data)[0];
        const ctor = ExtendedMixinStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for ExtendedMixinStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(ExtendedMixinStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in ExtendedMixinStruct._knownPolymorphic;
    }
}

ExtendedMixinStruct.register(ExtendedMixinStruct.FullClassName, ExtendedMixinStruct);

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
Introspector.register('izumi.test.domain01.ExtendedMixin', {
        full: 'izumi.test.domain01.ExtendedMixin',
        short: 'ExtendedMixin',
        package: 'izumi.test.domain01',
        type: IntrospectorTypes.Mixin,
        ctor: () => new ExtendedMixinStruct(),
        fields: [
            {
                name: 'own',
                accessName: 'own',
                type: {intro: IntrospectorTypes.I08}
            }
        ],
        implementations: ExtendedMixinStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(ExtendedMixinStruct.FullClassName, {
        full: ExtendedMixinStruct.FullClassName,
        short: ExtendedMixinStruct.ClassName,
        package: ExtendedMixinStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new ExtendedMixinStruct(),
        fields: [
            {
                name: 'own',
                accessName: 'own',
                type: {intro: IntrospectorTypes.I08}
            }
        ]
    } as IIntrospectorDataObject
);