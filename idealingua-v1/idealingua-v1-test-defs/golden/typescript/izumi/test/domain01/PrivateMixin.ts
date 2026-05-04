// Auto-generated, any modifications may be overwritten in the future.
import {
    PrivateMixinParent,
    PrivateMixinParentStruct,
    PrivateMixinParentStructSerialized
} from './PrivateMixinParent';
import {
    PrivateMixinPrivateParentStruct,
    PrivateMixinPrivateParentStructSerialized
} from './PrivateMixinPrivateParent';

// PrivateMixin Interface
export interface PrivateMixin extends PrivateMixinParent {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): PrivateMixinStructSerialized;

    parent_embedded: string;
    parent: string;
    embedded: boolean;
}

export interface PrivateMixinStructSerialized extends PrivateMixinParentStructSerialized {
    parent_embedded: string;
    parent: string;
    embedded: boolean;
}

export class PrivateMixinStruct implements PrivateMixin {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain01.PrivateMixin';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'izumi.test.domain01.PrivateMixin.Struct';

    public getPackageName(): string { return PrivateMixinStruct.PackageName; }
    public getClassName(): string { return PrivateMixinStruct.ClassName; }
    public getFullClassName(): string { return PrivateMixinStruct.FullClassName; }

    private _parent_embedded: string;
    private _parent: string;
    private _embedded: boolean;

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

    constructor(data: PrivateMixinStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.parent_embedded = data.parent_embedded;
        this.parent = data.parent;
        this.embedded = data.embedded;
    }

    public serialize(): PrivateMixinStructSerialized {
        return {
            parent_embedded: this.parent_embedded,
            parent: this.parent,
            embedded: this.embedded
        };
    }

    // Polymorphic section below. If a new type to be registered, use PrivateMixinStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: PrivateMixinStruct| PrivateMixinStructSerialized): PrivateMixin}} = {
        // This basic registration will happen below [PrivateMixinStruct.FullClassName]: PrivateMixinStruct
    };

    public static register(className: string, ctor: {new (data?: PrivateMixinStruct| PrivateMixinStructSerialized): PrivateMixin}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: PrivateMixinStructSerialized}): PrivateMixin {
        const polymorphicId = Object.keys(data)[0];
        const ctor = PrivateMixinStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for PrivateMixinStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(PrivateMixinStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in PrivateMixinStruct._knownPolymorphic;
    }
}

PrivateMixinStruct.register(PrivateMixinStruct.FullClassName, PrivateMixinStruct);
PrivateMixinParentStruct.register(PrivateMixinStruct.FullClassName, PrivateMixinStruct);

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
Introspector.register('izumi.test.domain01.PrivateMixin', {
        full: 'izumi.test.domain01.PrivateMixin',
        short: 'PrivateMixin',
        package: 'izumi.test.domain01',
        type: IntrospectorTypes.Mixin,
        ctor: () => new PrivateMixinStruct(),
        fields: [
            {
                name: 'embedded',
                accessName: 'embedded',
                type: {intro: IntrospectorTypes.Bool}
            }
        ],
        implementations: PrivateMixinStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(PrivateMixinStruct.FullClassName, {
        full: PrivateMixinStruct.FullClassName,
        short: PrivateMixinStruct.ClassName,
        package: PrivateMixinStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new PrivateMixinStruct(),
        fields: [
            {
                name: 'embedded',
                accessName: 'embedded',
                type: {intro: IntrospectorTypes.Bool}
            }
        ]
    } as IIntrospectorDataObject
);