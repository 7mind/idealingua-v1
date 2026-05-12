// Auto-generated, any modifications may be overwritten in the future.
import {
    PersonalAttributesStruct,
    PersonalAttributesStructSerialized
} from './PersonalAttributes';
import {
    SecurityAttributesStruct,
    SecurityAttributesStructSerialized
} from './SecurityAttributes';

// User2 DTO
export class User2  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.substraction';
    public static readonly ClassName = 'User2';
    public static readonly FullClassName = 'idltest.substraction.User2';

    public getPackageName(): string { return User2.PackageName; }
    public getClassName(): string { return User2.ClassName; }
    public getFullClassName(): string { return User2.FullClassName; }

    private _ssn: string;
    private _password: string;
    private _name: string;

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

    public get name(): string {
        return this._name;
    }

    public set name(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field name is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field name expects type string, got ' + value);
        }

        this._name = value;
    }

    constructor(data: User2Serialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.ssn = data.ssn;
        this.password = data.password;
        this.name = data.name;
    }

    public serialize(): User2Serialized {
        return {
            ssn: this.ssn,
            password: this.password,
            name: this.name
        };
    }
}

export interface User2Serialized  {
    ssn: string;
    password: string;
    name: string;
}

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorDataObject
} from '../../irt';
Introspector.register(User2.FullClassName, {
        full: User2.FullClassName,
        short: User2.ClassName,
        package: User2.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new User2(),
        fields: [
            {
                name: 'name',
                accessName: 'name',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'password',
                accessName: 'password',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'ssn',
                accessName: 'ssn',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);