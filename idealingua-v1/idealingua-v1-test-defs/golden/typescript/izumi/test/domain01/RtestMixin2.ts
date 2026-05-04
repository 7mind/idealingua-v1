// Auto-generated, any modifications may be overwritten in the future.
import {
    RTestMixin,
    RTestMixinStruct,
    RTestMixinStructSerialized
} from './RTestMixin';

// RtestMixin2 Interface
export interface RtestMixin2 {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): RtestMixin2StructSerialized;

    b: RTestMixin;
}

export interface RtestMixin2StructSerialized {
    b: {[key: string]: RTestMixinStructSerialized};
}

export class RtestMixin2Struct implements RtestMixin2 {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain01.RtestMixin2';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'izumi.test.domain01.RtestMixin2.Struct';

    public getPackageName(): string { return RtestMixin2Struct.PackageName; }
    public getClassName(): string { return RtestMixin2Struct.ClassName; }
    public getFullClassName(): string { return RtestMixin2Struct.FullClassName; }

    private _b: RTestMixin;

    public get b(): RTestMixin {
        return this._b;
    }

    public set b(value: RTestMixin) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field b is not optional');
        }
        this._b = value;
    }

    constructor(data: RtestMixin2StructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.b = RTestMixinStruct.create(data.b);
    }

    public serialize(): RtestMixin2StructSerialized {
        return {
            b: {[this.b.getFullClassName()]: this.b.serialize()}
        };
    }

    // Polymorphic section below. If a new type to be registered, use RtestMixin2Struct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: RtestMixin2Struct| RtestMixin2StructSerialized): RtestMixin2}} = {
        // This basic registration will happen below [RtestMixin2Struct.FullClassName]: RtestMixin2Struct
    };

    public static register(className: string, ctor: {new (data?: RtestMixin2Struct| RtestMixin2StructSerialized): RtestMixin2}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: RtestMixin2StructSerialized}): RtestMixin2 {
        const polymorphicId = Object.keys(data)[0];
        const ctor = RtestMixin2Struct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for RtestMixin2Struct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(RtestMixin2Struct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in RtestMixin2Struct._knownPolymorphic;
    }
}

RtestMixin2Struct.register(RtestMixin2Struct.FullClassName, RtestMixin2Struct);

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
Introspector.register('izumi.test.domain01.RtestMixin2', {
        full: 'izumi.test.domain01.RtestMixin2',
        short: 'RtestMixin2',
        package: 'izumi.test.domain01',
        type: IntrospectorTypes.Mixin,
        ctor: () => new RtestMixin2Struct(),
        fields: [
            {
                name: 'b',
                accessName: 'b',
                type: {intro: IntrospectorTypes.Mixin, full: 'izumi.test.domain01.RTestMixin'} as IIntrospectorUserType
            }
        ],
        implementations: RtestMixin2Struct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(RtestMixin2Struct.FullClassName, {
        full: RtestMixin2Struct.FullClassName,
        short: RtestMixin2Struct.ClassName,
        package: RtestMixin2Struct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new RtestMixin2Struct(),
        fields: [
            {
                name: 'b',
                accessName: 'b',
                type: {intro: IntrospectorTypes.Mixin, full: 'izumi.test.domain01.RTestMixin'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorDataObject
);