// Auto-generated, any modifications may be overwritten in the future.
import {
    WithCovarianceStruct,
    WithCovarianceStructSerialized
} from './WithCovariance';
import {
    CovariantA,
    CovariantAStruct,
    CovariantAStructSerialized
} from './CovariantA';
import {
    InheritedCovariant,
    InheritedCovariantStruct,
    InheritedCovariantStructSerialized
} from './InheritedCovariant';
import {
    Covariant,
    CovariantStruct,
    CovariantStructSerialized
} from './Covariant';

// CovariantDTO2 DTO
export class CovariantDTO2 implements InheritedCovariant  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.inheritance';
    public static readonly ClassName = 'CovariantDTO2';
    public static readonly FullClassName = 'idltest.inheritance.CovariantDTO2';

    public getPackageName(): string { return CovariantDTO2.PackageName; }
    public getClassName(): string { return CovariantDTO2.ClassName; }
    public getFullClassName(): string { return CovariantDTO2.FullClassName; }

    private _field: CovariantA;

    public get field(): CovariantA {
        return this._field;
    }

    public set field(value: CovariantA) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field field is not optional');
        }
        this._field = value;
    }

    constructor(data: CovariantDTO2Serialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.field = CovariantAStruct.create(data.field);
    }

    public toInheritedCovariantSerialized(): InheritedCovariantStructSerialized {
        return {
            field: {[this.field.getFullClassName()]: this.field.serialize()}
        };
    }

    public toInheritedCovariant(): InheritedCovariantStruct {
        return new InheritedCovariantStruct(this.toInheritedCovariantSerialized());
    }

    public toWithCovarianceSerialized(): WithCovarianceStructSerialized {
        return {
            field: {[this.field.getFullClassName()]: this.field.serialize()}
        };
    }

    public toWithCovariance(): WithCovarianceStruct {
        return new WithCovarianceStruct(this.toWithCovarianceSerialized());
    }

    public loadInheritedCovariantSerialized(slice: InheritedCovariantStructSerialized) {
        this.field = CovariantAStruct.create(slice.field);
    }

    public loadInheritedCovariant(slice: InheritedCovariantStruct) {
        this.loadInheritedCovariantSerialized(slice.serialize());
    }

    public loadWithCovarianceSerialized(slice: WithCovarianceStructSerialized) {
        this.field = CovariantStruct.create(slice.field);
    }

    public loadWithCovariance(slice: WithCovarianceStruct) {
        this.loadWithCovarianceSerialized(slice.serialize());
    }

    public serialize(): CovariantDTO2Serialized {
        return {
            field: {[this.field.getFullClassName()]: this.field.serialize()}
        };
    }
}

export interface CovariantDTO2Serialized extends InheritedCovariantStructSerialized  {
    field: {[key: string]: CovariantAStructSerialized};
}

InheritedCovariantStruct.register(CovariantDTO2.FullClassName, CovariantDTO2);
WithCovarianceStruct.register(CovariantDTO2.FullClassName, CovariantDTO2);

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorDataObject
} from '../../irt';
Introspector.register(CovariantDTO2.FullClassName, {
        full: CovariantDTO2.FullClassName,
        short: CovariantDTO2.ClassName,
        package: CovariantDTO2.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new CovariantDTO2(),
        fields: [
            {
                name: 'field',
                accessName: 'field',
                type: {intro: IntrospectorTypes.Mixin, full: 'idltest.inheritance.CovariantA'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorDataObject
);