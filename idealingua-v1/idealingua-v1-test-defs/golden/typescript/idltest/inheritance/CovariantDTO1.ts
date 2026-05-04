// Auto-generated, any modifications may be overwritten in the future.
import {
    WithCovariance,
    WithCovarianceStruct,
    WithCovarianceStructSerialized
} from './WithCovariance';
import {
    Covariant,
    CovariantStruct,
    CovariantStructSerialized
} from './Covariant';

// CovariantDTO1 DTO
export class CovariantDTO1 implements WithCovariance  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.inheritance';
    public static readonly ClassName = 'CovariantDTO1';
    public static readonly FullClassName = 'idltest.inheritance.CovariantDTO1';

    public getPackageName(): string { return CovariantDTO1.PackageName; }
    public getClassName(): string { return CovariantDTO1.ClassName; }
    public getFullClassName(): string { return CovariantDTO1.FullClassName; }

    private _field: Covariant;

    public get field(): Covariant {
        return this._field;
    }

    public set field(value: Covariant) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field field is not optional');
        }
        this._field = value;
    }

    constructor(data: CovariantDTO1Serialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.field = CovariantStruct.create(data.field);
    }

    public toWithCovarianceSerialized(): WithCovarianceStructSerialized {
        return {
            field: {[this.field.getFullClassName()]: this.field.serialize()}
        };
    }

    public toWithCovariance(): WithCovarianceStruct {
        return new WithCovarianceStruct(this.toWithCovarianceSerialized());
    }

    public loadWithCovarianceSerialized(slice: WithCovarianceStructSerialized) {
        this.field = CovariantStruct.create(slice.field);
    }

    public loadWithCovariance(slice: WithCovarianceStruct) {
        this.loadWithCovarianceSerialized(slice.serialize());
    }

    public serialize(): CovariantDTO1Serialized {
        return {
            field: {[this.field.getFullClassName()]: this.field.serialize()}
        };
    }
}

export interface CovariantDTO1Serialized extends WithCovarianceStructSerialized  {
    field: {[key: string]: CovariantStructSerialized};
}

WithCovarianceStruct.register(CovariantDTO1.FullClassName, CovariantDTO1);

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorDataObject
} from '../../irt';
Introspector.register(CovariantDTO1.FullClassName, {
        full: CovariantDTO1.FullClassName,
        short: CovariantDTO1.ClassName,
        package: CovariantDTO1.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new CovariantDTO1(),
        fields: [
            {
                name: 'field',
                accessName: 'field',
                type: {intro: IntrospectorTypes.Mixin, full: 'idltest.inheritance.Covariant'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorDataObject
);