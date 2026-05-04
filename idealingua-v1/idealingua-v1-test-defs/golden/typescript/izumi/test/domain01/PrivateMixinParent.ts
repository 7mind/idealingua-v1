// Auto-generated, any modifications may be overwritten in the future.

// PrivateMixinParent Interface
export interface PrivateMixinParent {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): PrivateMixinParentStructSerialized;

    parent: string;
}

export interface PrivateMixinParentStructSerialized {
    parent: string;
}

export class PrivateMixinParentStruct implements PrivateMixinParent {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain01.PrivateMixinParent';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'izumi.test.domain01.PrivateMixinParent.Struct';

    public getPackageName(): string { return PrivateMixinParentStruct.PackageName; }
    public getClassName(): string { return PrivateMixinParentStruct.ClassName; }
    public getFullClassName(): string { return PrivateMixinParentStruct.FullClassName; }

    private _parent: string;

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

    constructor(data: PrivateMixinParentStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.parent = data.parent;
    }

    public serialize(): PrivateMixinParentStructSerialized {
        return {
            parent: this.parent
        };
    }

    // Polymorphic section below. If a new type to be registered, use PrivateMixinParentStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: PrivateMixinParentStruct| PrivateMixinParentStructSerialized): PrivateMixinParent}} = {
        // This basic registration will happen below [PrivateMixinParentStruct.FullClassName]: PrivateMixinParentStruct
    };

    public static register(className: string, ctor: {new (data?: PrivateMixinParentStruct| PrivateMixinParentStructSerialized): PrivateMixinParent}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: PrivateMixinParentStructSerialized}): PrivateMixinParent {
        const polymorphicId = Object.keys(data)[0];
        const ctor = PrivateMixinParentStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for PrivateMixinParentStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(PrivateMixinParentStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in PrivateMixinParentStruct._knownPolymorphic;
    }
}

PrivateMixinParentStruct.register(PrivateMixinParentStruct.FullClassName, PrivateMixinParentStruct);

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
Introspector.register('izumi.test.domain01.PrivateMixinParent', {
        full: 'izumi.test.domain01.PrivateMixinParent',
        short: 'PrivateMixinParent',
        package: 'izumi.test.domain01',
        type: IntrospectorTypes.Mixin,
        ctor: () => new PrivateMixinParentStruct(),
        fields: [
            {
                name: 'parent',
                accessName: 'parent',
                type: {intro: IntrospectorTypes.Str}
            }
        ],
        implementations: PrivateMixinParentStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(PrivateMixinParentStruct.FullClassName, {
        full: PrivateMixinParentStruct.FullClassName,
        short: PrivateMixinParentStruct.ClassName,
        package: PrivateMixinParentStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new PrivateMixinParentStruct(),
        fields: [
            {
                name: 'parent',
                accessName: 'parent',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);