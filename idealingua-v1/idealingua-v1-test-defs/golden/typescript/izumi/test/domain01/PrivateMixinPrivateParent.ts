// Auto-generated, any modifications may be overwritten in the future.

// PrivateMixinPrivateParent Interface
export interface PrivateMixinPrivateParent {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): PrivateMixinPrivateParentStructSerialized;

    parent_embedded: string;
}

export interface PrivateMixinPrivateParentStructSerialized {
    parent_embedded: string;
}

export class PrivateMixinPrivateParentStruct implements PrivateMixinPrivateParent {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain01.PrivateMixinPrivateParent';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'izumi.test.domain01.PrivateMixinPrivateParent.Struct';

    public getPackageName(): string { return PrivateMixinPrivateParentStruct.PackageName; }
    public getClassName(): string { return PrivateMixinPrivateParentStruct.ClassName; }
    public getFullClassName(): string { return PrivateMixinPrivateParentStruct.FullClassName; }

    private _parent_embedded: string;

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

    constructor(data: PrivateMixinPrivateParentStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.parent_embedded = data.parent_embedded;
    }

    public serialize(): PrivateMixinPrivateParentStructSerialized {
        return {
            parent_embedded: this.parent_embedded
        };
    }

    // Polymorphic section below. If a new type to be registered, use PrivateMixinPrivateParentStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: PrivateMixinPrivateParentStruct| PrivateMixinPrivateParentStructSerialized): PrivateMixinPrivateParent}} = {
        // This basic registration will happen below [PrivateMixinPrivateParentStruct.FullClassName]: PrivateMixinPrivateParentStruct
    };

    public static register(className: string, ctor: {new (data?: PrivateMixinPrivateParentStruct| PrivateMixinPrivateParentStructSerialized): PrivateMixinPrivateParent}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: PrivateMixinPrivateParentStructSerialized}): PrivateMixinPrivateParent {
        const polymorphicId = Object.keys(data)[0];
        const ctor = PrivateMixinPrivateParentStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for PrivateMixinPrivateParentStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(PrivateMixinPrivateParentStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in PrivateMixinPrivateParentStruct._knownPolymorphic;
    }
}

PrivateMixinPrivateParentStruct.register(PrivateMixinPrivateParentStruct.FullClassName, PrivateMixinPrivateParentStruct);

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
Introspector.register('izumi.test.domain01.PrivateMixinPrivateParent', {
        full: 'izumi.test.domain01.PrivateMixinPrivateParent',
        short: 'PrivateMixinPrivateParent',
        package: 'izumi.test.domain01',
        type: IntrospectorTypes.Mixin,
        ctor: () => new PrivateMixinPrivateParentStruct(),
        fields: [
            {
                name: 'parent_embedded',
                accessName: 'parent_embedded',
                type: {intro: IntrospectorTypes.Str}
            }
        ],
        implementations: PrivateMixinPrivateParentStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(PrivateMixinPrivateParentStruct.FullClassName, {
        full: PrivateMixinPrivateParentStruct.FullClassName,
        short: PrivateMixinPrivateParentStruct.ClassName,
        package: PrivateMixinPrivateParentStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new PrivateMixinPrivateParentStruct(),
        fields: [
            {
                name: 'parent_embedded',
                accessName: 'parent_embedded',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);