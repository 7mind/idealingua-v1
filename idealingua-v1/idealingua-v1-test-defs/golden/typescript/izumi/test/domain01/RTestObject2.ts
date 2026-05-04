// Auto-generated, any modifications may be overwritten in the future.
import {
    RtestMixin2,
    RtestMixin2Struct,
    RtestMixin2StructSerialized
} from './RtestMixin2';
import {
    RTestMixin,
    RTestMixinStruct,
    RTestMixinStructSerialized
} from './RTestMixin';

// RTestObject2 DTO
export class RTestObject2 implements RtestMixin2  {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain01';
    public static readonly ClassName = 'RTestObject2';
    public static readonly FullClassName = 'izumi.test.domain01.RTestObject2';

    public getPackageName(): string { return RTestObject2.PackageName; }
    public getClassName(): string { return RTestObject2.ClassName; }
    public getFullClassName(): string { return RTestObject2.FullClassName; }

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

    constructor(data: RTestObject2Serialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.b = RTestMixinStruct.create(data.b);
    }

    public toRtestMixin2Serialized(): RtestMixin2StructSerialized {
        return {
            b: {[this.b.getFullClassName()]: this.b.serialize()}
        };
    }

    public toRtestMixin2(): RtestMixin2Struct {
        return new RtestMixin2Struct(this.toRtestMixin2Serialized());
    }

    public loadRtestMixin2Serialized(slice: RtestMixin2StructSerialized) {
        this.b = RTestMixinStruct.create(slice.b);
    }

    public loadRtestMixin2(slice: RtestMixin2Struct) {
        this.loadRtestMixin2Serialized(slice.serialize());
    }

    public serialize(): RTestObject2Serialized {
        return {
            b: {[this.b.getFullClassName()]: this.b.serialize()}
        };
    }
}

export interface RTestObject2Serialized extends RtestMixin2StructSerialized  {
    b: {[key: string]: RTestMixinStructSerialized};
}

RtestMixin2Struct.register(RTestObject2.FullClassName, RTestObject2);

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorDataObject
} from '../../../irt';
Introspector.register(RTestObject2.FullClassName, {
        full: RTestObject2.FullClassName,
        short: RTestObject2.ClassName,
        package: RTestObject2.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new RTestObject2(),
        fields: [
            {
                name: 'b',
                accessName: 'b',
                type: {intro: IntrospectorTypes.Mixin, full: 'izumi.test.domain01.RTestMixin'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorDataObject
);