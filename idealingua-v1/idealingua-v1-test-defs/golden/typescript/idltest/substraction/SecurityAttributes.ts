// Auto-generated, any modifications may be overwritten in the future.
import {
    PersonalAttributes,
    PersonalAttributesStruct,
    PersonalAttributesStructSerialized
} from './PersonalAttributes';

// SecurityAttributes Interface
export interface SecurityAttributes extends PersonalAttributes {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): SecurityAttributesStructSerialized;

    ssn: string;
    password: string;
}

export interface SecurityAttributesStructSerialized extends PersonalAttributesStructSerialized {
    ssn: string;
    password: string;
}

export class SecurityAttributesStruct implements SecurityAttributes {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.substraction.SecurityAttributes';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.substraction.SecurityAttributes.Struct';

    public getPackageName(): string { return SecurityAttributesStruct.PackageName; }
    public getClassName(): string { return SecurityAttributesStruct.ClassName; }
    public getFullClassName(): string { return SecurityAttributesStruct.FullClassName; }

    private _ssn: string;
    private _password: string;

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

    public get password(): string {
        return this._password;
    }

    public set password(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field password is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field password expects type string, got ' + value);
        }

        this._password = value;
    }

    constructor(data: SecurityAttributesStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.ssn = data.ssn;
        this.password = data.password;
    }

    public serialize(): SecurityAttributesStructSerialized {
        return {
            ssn: this.ssn,
            password: this.password
        };
    }

    // Polymorphic section below. If a new type to be registered, use SecurityAttributesStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: SecurityAttributesStruct| SecurityAttributesStructSerialized): SecurityAttributes}} = {
        // This basic registration will happen below [SecurityAttributesStruct.FullClassName]: SecurityAttributesStruct
    };

    public static register(className: string, ctor: {new (data?: SecurityAttributesStruct| SecurityAttributesStructSerialized): SecurityAttributes}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: SecurityAttributesStructSerialized}): SecurityAttributes {
        const polymorphicId = Object.keys(data)[0];
        const ctor = SecurityAttributesStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for SecurityAttributesStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(SecurityAttributesStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in SecurityAttributesStruct._knownPolymorphic;
    }
}

SecurityAttributesStruct.register(SecurityAttributesStruct.FullClassName, SecurityAttributesStruct);
PersonalAttributesStruct.register(SecurityAttributesStruct.FullClassName, SecurityAttributesStruct);

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
Introspector.register('idltest.substraction.SecurityAttributes', {
        full: 'idltest.substraction.SecurityAttributes',
        short: 'SecurityAttributes',
        package: 'idltest.substraction',
        type: IntrospectorTypes.Mixin,
        ctor: () => new SecurityAttributesStruct(),
        fields: [
            {
                name: 'password',
                accessName: 'password',
                type: {intro: IntrospectorTypes.Str}
            }
        ],
        implementations: SecurityAttributesStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(SecurityAttributesStruct.FullClassName, {
        full: SecurityAttributesStruct.FullClassName,
        short: SecurityAttributesStruct.ClassName,
        package: SecurityAttributesStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new SecurityAttributesStruct(),
        fields: [
            {
                name: 'password',
                accessName: 'password',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);