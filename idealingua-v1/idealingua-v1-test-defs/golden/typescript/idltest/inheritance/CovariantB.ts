// Auto-generated, any modifications may be overwritten in the future.
import {
    Covariant,
    CovariantStruct,
    CovariantStructSerialized
} from './Covariant';

// CovariantB Interface
export interface CovariantB extends Covariant {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): CovariantBStructSerialized;
}
export interface CovariantBStructSerialized extends CovariantStructSerialized {
}

export class CovariantBStruct implements CovariantB {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.inheritance.CovariantB';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.inheritance.CovariantB.Struct';

    public getPackageName(): string { return CovariantBStruct.PackageName; }
    public getClassName(): string { return CovariantBStruct.ClassName; }
    public getFullClassName(): string { return CovariantBStruct.FullClassName; }

    constructor(data: CovariantBStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

    }

    public serialize(): CovariantBStructSerialized {
        return {
        };
    }

    // Polymorphic section below. If a new type to be registered, use CovariantBStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: CovariantBStruct| CovariantBStructSerialized): CovariantB}} = {
        // This basic registration will happen below [CovariantBStruct.FullClassName]: CovariantBStruct
    };

    public static register(className: string, ctor: {new (data?: CovariantBStruct| CovariantBStructSerialized): CovariantB}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: CovariantBStructSerialized}): CovariantB {
        const polymorphicId = Object.keys(data)[0];
        const ctor = CovariantBStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for CovariantBStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(CovariantBStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in CovariantBStruct._knownPolymorphic;
    }
}

CovariantBStruct.register(CovariantBStruct.FullClassName, CovariantBStruct);
CovariantStruct.register(CovariantBStruct.FullClassName, CovariantBStruct);

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
Introspector.register('idltest.inheritance.CovariantB', {
        full: 'idltest.inheritance.CovariantB',
        short: 'CovariantB',
        package: 'idltest.inheritance',
        type: IntrospectorTypes.Mixin,
        ctor: () => new CovariantBStruct(),
        fields: [

        ],
        implementations: CovariantBStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(CovariantBStruct.FullClassName, {
        full: CovariantBStruct.FullClassName,
        short: CovariantBStruct.ClassName,
        package: CovariantBStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new CovariantBStruct(),
        fields: [

        ]
    } as IIntrospectorDataObject
);