// Auto-generated, any modifications may be overwritten in the future.
import {
    DepartmentEnum
} from './DepartmentEnum';

export class UserWithEnumId implements IUserWithEnumId {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.identifiers';
    public static readonly ClassName = 'UserWithEnumId';
    public static readonly FullClassName = 'idltest.identifiers.UserWithEnumId';

    public getPackageName(): string { return UserWithEnumId.PackageName; }
    public getClassName(): string { return UserWithEnumId.ClassName; }
    public getFullClassName(): string { return UserWithEnumId.FullClassName; }

    private _value: string;
    private _company: string;
    private _dept: DepartmentEnum;

    public get value(): string {
        return this._value;
    }

    public set value(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field value is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field value expects type string, got ' + value);
        }

        if (!value.match('^[0-9a-fA-f]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$')) {
            throw new Error('Field value expects guid format, got ' + value);
        }

        this._value = value;
    }

    public get company(): string {
        return this._company;
    }

    public set company(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field company is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field company expects type string, got ' + value);
        }

        if (!value.match('^[0-9a-fA-f]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$')) {
            throw new Error('Field company expects guid format, got ' + value);
        }

        this._company = value;
    }

    public get dept(): DepartmentEnum {
        return this._dept;
    }

    public set dept(value: DepartmentEnum) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field dept is not optional');
        }
        this._dept = value;
    }

    constructor(data: string | IUserWithEnumId = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        if (typeof data === 'string') {
            if (!data.startsWith('UserWithEnumId#')) {
                throw new Error('Identifier must start with UserWithEnumId, got ' + data);
            }
            const parts = data.substr(data.indexOf('#') + 1).split(':');
            this.company = decodeURIComponent(parts[0]);
            this.dept = DepartmentEnum[decodeURIComponent(parts[1]) as keyof typeof DepartmentEnum];
            this.value = decodeURIComponent(parts[2]);
        } else {
            this.value = data.value;
            this.company = data.company;
            this.dept = DepartmentEnum[data.dept as keyof typeof DepartmentEnum];
        }
    }

    public toString(): string {
        const suffix = encodeURIComponent(this.company) + ':' + encodeURIComponent(DepartmentEnum[this.dept]) + ':' + encodeURIComponent(this.value);
        return 'UserWithEnumId#' + suffix;
    }

    public serialize(): string {
        return this.toString();
    }
}

export interface IUserWithEnumId {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): string;

    value: string;
    company: string;
    dept: DepartmentEnum;
}

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorIdObject
} from '../../irt';
Introspector.register(UserWithEnumId.FullClassName, {
        full: UserWithEnumId.FullClassName,
        short: UserWithEnumId.ClassName,
        package: UserWithEnumId.PackageName,
        type: IntrospectorTypes.Id,
        ctor: () => new UserWithEnumId(),
        fields: [
            {
                name: 'value',
                accessName: 'value',
                type: {intro: IntrospectorTypes.Uid}
            },
            {
                name: 'company',
                accessName: 'company',
                type: {intro: IntrospectorTypes.Uid}
            },
            {
                name: 'dept',
                accessName: 'dept',
                type: {intro: IntrospectorTypes.Enum, full: 'idltest.identifiers.DepartmentEnum'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorIdObject
);