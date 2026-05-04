// Auto-generated, any modifications may be overwritten in the future.
import {
    RTestEnum
} from './RTestEnum';

// RTestMixin Interface
export interface RTestMixin {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): RTestMixinStructSerialized;

    a: RTestEnum;
}

export interface RTestMixinStructSerialized {
    a: string;
}

export class RTestMixinStruct implements RTestMixin {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain01.RTestMixin';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'izumi.test.domain01.RTestMixin.Struct';

    public getPackageName(): string { return RTestMixinStruct.PackageName; }
    public getClassName(): string { return RTestMixinStruct.ClassName; }
    public getFullClassName(): string { return RTestMixinStruct.FullClassName; }

    private _a: RTestEnum;

    public get a(): RTestEnum {
        return this._a;
    }

    public set a(value: RTestEnum) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field a is not optional');
        }
        this._a = value;
    }

    constructor(data: RTestMixinStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.a = RTestEnum[data.a as keyof typeof RTestEnum];
    }

    public serialize(): RTestMixinStructSerialized {
        return {
            a: RTestEnum[this.a]
        };
    }

    // Polymorphic section below. If a new type to be registered, use RTestMixinStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: RTestMixinStruct| RTestMixinStructSerialized): RTestMixin}} = {
        // This basic registration will happen below [RTestMixinStruct.FullClassName]: RTestMixinStruct
    };

    public static register(className: string, ctor: {new (data?: RTestMixinStruct| RTestMixinStructSerialized): RTestMixin}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: RTestMixinStructSerialized}): RTestMixin {
        const polymorphicId = Object.keys(data)[0];
        const ctor = RTestMixinStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for RTestMixinStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(RTestMixinStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in RTestMixinStruct._knownPolymorphic;
    }
}

RTestMixinStruct.register(RTestMixinStruct.FullClassName, RTestMixinStruct);

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
Introspector.register('izumi.test.domain01.RTestMixin', {
        full: 'izumi.test.domain01.RTestMixin',
        short: 'RTestMixin',
        package: 'izumi.test.domain01',
        type: IntrospectorTypes.Mixin,
        ctor: () => new RTestMixinStruct(),
        fields: [
            {
                name: 'a',
                accessName: 'a',
                type: {intro: IntrospectorTypes.Enum, full: 'izumi.test.domain01.RTestEnum'} as IIntrospectorUserType
            }
        ],
        implementations: RTestMixinStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(RTestMixinStruct.FullClassName, {
        full: RTestMixinStruct.FullClassName,
        short: RTestMixinStruct.ClassName,
        package: RTestMixinStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new RTestMixinStruct(),
        fields: [
            {
                name: 'a',
                accessName: 'a',
                type: {intro: IntrospectorTypes.Enum, full: 'izumi.test.domain01.RTestEnum'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorDataObject
);