// Auto-generated, any modifications may be overwritten in the future.
import {
    Covariant,
    CovariantStruct,
    CovariantStructSerialized
} from './Covariant';

// CovariantA Interface
export interface CovariantA extends Covariant {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): CovariantAStructSerialized;
}
export interface CovariantAStructSerialized extends CovariantStructSerialized {
}

export class CovariantAStruct implements CovariantA {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.inheritance.CovariantA';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.inheritance.CovariantA.Struct';

    public getPackageName(): string { return CovariantAStruct.PackageName; }
    public getClassName(): string { return CovariantAStruct.ClassName; }
    public getFullClassName(): string { return CovariantAStruct.FullClassName; }

    constructor(data: CovariantAStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

    }

    public serialize(): CovariantAStructSerialized {
        return {
        };
    }

    // Polymorphic section below. If a new type to be registered, use CovariantAStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: CovariantAStruct| CovariantAStructSerialized): CovariantA}} = {
        // This basic registration will happen below [CovariantAStruct.FullClassName]: CovariantAStruct
    };

    public static register(className: string, ctor: {new (data?: CovariantAStruct| CovariantAStructSerialized): CovariantA}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: CovariantAStructSerialized}): CovariantA {
        const polymorphicId = Object.keys(data)[0];
        const ctor = CovariantAStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for CovariantAStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(CovariantAStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in CovariantAStruct._knownPolymorphic;
    }
}

CovariantAStruct.register(CovariantAStruct.FullClassName, CovariantAStruct);
CovariantStruct.register(CovariantAStruct.FullClassName, CovariantAStruct);

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
Introspector.register('idltest.inheritance.CovariantA', {
        full: 'idltest.inheritance.CovariantA',
        short: 'CovariantA',
        package: 'idltest.inheritance',
        type: IntrospectorTypes.Mixin,
        ctor: () => new CovariantAStruct(),
        fields: [

        ],
        implementations: CovariantAStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(CovariantAStruct.FullClassName, {
        full: CovariantAStruct.FullClassName,
        short: CovariantAStruct.ClassName,
        package: CovariantAStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new CovariantAStruct(),
        fields: [

        ]
    } as IIntrospectorDataObject
);