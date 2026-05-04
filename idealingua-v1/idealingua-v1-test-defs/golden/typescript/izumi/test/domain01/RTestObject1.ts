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

// RTestObject1 DTO
export class RTestObject1 implements RtestMixin2  {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain01';
    public static readonly ClassName = 'RTestObject1';
    public static readonly FullClassName = 'izumi.test.domain01.RTestObject1';

    public getPackageName(): string { return RTestObject1.PackageName; }
    public getClassName(): string { return RTestObject1.ClassName; }
    public getFullClassName(): string { return RTestObject1.FullClassName; }

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

    constructor(data: RTestObject1Serialized = undefined) {
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

    public serialize(): RTestObject1Serialized {
        return {
            b: {[this.b.getFullClassName()]: this.b.serialize()}
        };
    }
}

export interface RTestObject1Serialized extends RtestMixin2StructSerialized  {
    b: {[key: string]: RTestMixinStructSerialized};
}

RtestMixin2Struct.register(RTestObject1.FullClassName, RTestObject1);

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorDataObject
} from '../../../irt';
Introspector.register(RTestObject1.FullClassName, {
        full: RTestObject1.FullClassName,
        short: RTestObject1.ClassName,
        package: RTestObject1.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new RTestObject1(),
        fields: [
            {
                name: 'b',
                accessName: 'b',
                type: {intro: IntrospectorTypes.Mixin, full: 'izumi.test.domain01.RTestMixin'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorDataObject
);