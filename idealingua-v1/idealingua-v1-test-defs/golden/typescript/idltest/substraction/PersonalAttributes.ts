// Auto-generated, any modifications may be overwritten in the future.

// PersonalAttributes Interface
export interface PersonalAttributes {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): PersonalAttributesStructSerialized;

    ssn: string;
}

export interface PersonalAttributesStructSerialized {
    ssn: string;
}

export class PersonalAttributesStruct implements PersonalAttributes {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.substraction.PersonalAttributes';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.substraction.PersonalAttributes.Struct';

    public getPackageName(): string { return PersonalAttributesStruct.PackageName; }
    public getClassName(): string { return PersonalAttributesStruct.ClassName; }
    public getFullClassName(): string { return PersonalAttributesStruct.FullClassName; }

    private _ssn: string;

    public get ssn(): string {
        return this._ssn;
    }

    public set ssn(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field ssn is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field ssn expects type string, got ' + value);
        }

        this._ssn = value;
    }

    constructor(data: PersonalAttributesStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.ssn = data.ssn;
    }

    public serialize(): PersonalAttributesStructSerialized {
        return {
            ssn: this.ssn
        };
    }

    // Polymorphic section below. If a new type to be registered, use PersonalAttributesStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: PersonalAttributesStruct| PersonalAttributesStructSerialized): PersonalAttributes}} = {
        // This basic registration will happen below [PersonalAttributesStruct.FullClassName]: PersonalAttributesStruct
    };

    public static register(className: string, ctor: {new (data?: PersonalAttributesStruct| PersonalAttributesStructSerialized): PersonalAttributes}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: PersonalAttributesStructSerialized}): PersonalAttributes {
        const polymorphicId = Object.keys(data)[0];
        const ctor = PersonalAttributesStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for PersonalAttributesStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(PersonalAttributesStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in PersonalAttributesStruct._knownPolymorphic;
    }
}

PersonalAttributesStruct.register(PersonalAttributesStruct.FullClassName, PersonalAttributesStruct);

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
Introspector.register('idltest.substraction.PersonalAttributes', {
        full: 'idltest.substraction.PersonalAttributes',
        short: 'PersonalAttributes',
        package: 'idltest.substraction',
        type: IntrospectorTypes.Mixin,
        ctor: () => new PersonalAttributesStruct(),
        fields: [
            {
                name: 'ssn',
                accessName: 'ssn',
                type: {intro: IntrospectorTypes.Str}
            }
        ],
        implementations: PersonalAttributesStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(PersonalAttributesStruct.FullClassName, {
        full: PersonalAttributesStruct.FullClassName,
        short: PersonalAttributesStruct.ClassName,
        package: PersonalAttributesStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new PersonalAttributesStruct(),
        fields: [
            {
                name: 'ssn',
                accessName: 'ssn',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);