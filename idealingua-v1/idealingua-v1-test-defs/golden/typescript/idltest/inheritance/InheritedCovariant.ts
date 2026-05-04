// Auto-generated, any modifications may be overwritten in the future.
import {
    WithCovariance,
    WithCovarianceStruct,
    WithCovarianceStructSerialized
} from './WithCovariance';
import {
    CovariantA,
    CovariantAStruct,
    CovariantAStructSerialized
} from './CovariantA';
import {
    Covariant,
    CovariantStruct,
    CovariantStructSerialized
} from './Covariant';

// InheritedCovariant Interface
export interface InheritedCovariant extends WithCovariance {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): InheritedCovariantStructSerialized;

    field: CovariantA;
}

export interface InheritedCovariantStructSerialized extends WithCovarianceStructSerialized {
    field: {[key: string]: CovariantAStructSerialized};
}

export class InheritedCovariantStruct implements InheritedCovariant {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.inheritance.InheritedCovariant';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.inheritance.InheritedCovariant.Struct';

    public getPackageName(): string { return InheritedCovariantStruct.PackageName; }
    public getClassName(): string { return InheritedCovariantStruct.ClassName; }
    public getFullClassName(): string { return InheritedCovariantStruct.FullClassName; }

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

    constructor(data: InheritedCovariantStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.field = CovariantAStruct.create(data.field);
    }

    public serialize(): InheritedCovariantStructSerialized {
        return {
            field: {[this.field.getFullClassName()]: this.field.serialize()}
        };
    }

    // Polymorphic section below. If a new type to be registered, use InheritedCovariantStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: InheritedCovariantStruct| InheritedCovariantStructSerialized): InheritedCovariant}} = {
        // This basic registration will happen below [InheritedCovariantStruct.FullClassName]: InheritedCovariantStruct
    };

    public static register(className: string, ctor: {new (data?: InheritedCovariantStruct| InheritedCovariantStructSerialized): InheritedCovariant}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: InheritedCovariantStructSerialized}): InheritedCovariant {
        const polymorphicId = Object.keys(data)[0];
        const ctor = InheritedCovariantStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for InheritedCovariantStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(InheritedCovariantStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in InheritedCovariantStruct._knownPolymorphic;
    }
}

InheritedCovariantStruct.register(InheritedCovariantStruct.FullClassName, InheritedCovariantStruct);
WithCovarianceStruct.register(InheritedCovariantStruct.FullClassName, InheritedCovariantStruct);

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorMixinObject,
    IIntrospectorDataObject
} from '../../irt';
Introspector.register('idltest.inheritance.InheritedCovariant', {
        full: 'idltest.inheritance.InheritedCovariant',
        short: 'InheritedCovariant',
        package: 'idltest.inheritance',
        type: IntrospectorTypes.Mixin,
        ctor: () => new InheritedCovariantStruct(),
        fields: [
            {
                name: 'field',
                accessName: 'field',
                type: {intro: IntrospectorTypes.Mixin, full: 'idltest.inheritance.CovariantA'} as IIntrospectorUserType
            }
        ],
        implementations: InheritedCovariantStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(InheritedCovariantStruct.FullClassName, {
        full: InheritedCovariantStruct.FullClassName,
        short: InheritedCovariantStruct.ClassName,
        package: InheritedCovariantStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new InheritedCovariantStruct(),
        fields: [
            {
                name: 'field',
                accessName: 'field',
                type: {intro: IntrospectorTypes.Mixin, full: 'idltest.inheritance.CovariantA'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorDataObject
);