// Auto-generated, any modifications may be overwritten in the future.
import {
    UserWithEnumId
} from './UserWithEnumId';
import {
    BucketID
} from './BucketID';

export class ComplexID implements IComplexID {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.identifiers';
    public static readonly ClassName = 'ComplexID';
    public static readonly FullClassName = 'idltest.identifiers.ComplexID';

    public getPackageName(): string { return ComplexID.PackageName; }
    public getClassName(): string { return ComplexID.ClassName; }
    public getFullClassName(): string { return ComplexID.FullClassName; }

    private _bucket: BucketID;
    private _user: UserWithEnumId;
    private _i32: number;
    private _uid: string;
    private _str: string;

    public get bucket(): BucketID {
        return this._bucket;
    }

    public set bucket(value: BucketID) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field bucket is not optional');
        }
        this._bucket = value;
    }

    public get user(): UserWithEnumId {
        return this._user;
    }

    public set user(value: UserWithEnumId) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field user is not optional');
        }
        this._user = value;
    }

    public get i32(): number {
        return this._i32;
    }

    public set i32(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field i32 is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field i32 expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field i32 is expected to be an integer, got ' + value);
        }

        this._i32 = value;
    }

    public get uid(): string {
        return this._uid;
    }

    public set uid(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field uid is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field uid expects type string, got ' + value);
        }

        if (!value.match('^[0-9a-fA-f]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$')) {
            throw new Error('Field uid expects guid format, got ' + value);
        }

        this._uid = value;
    }

    public get str(): string {
        return this._str;
    }

    public set str(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field str is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field str expects type string, got ' + value);
        }

        this._str = value;
    }

    constructor(data: string | IComplexID = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        if (typeof data === 'string') {
            if (!data.startsWith('ComplexID#')) {
                throw new Error('Identifier must start with ComplexID, got ' + data);
            }
            const parts = data.substr(data.indexOf('#') + 1).split(':');
            this.bucket = new BucketID(decodeURIComponent(parts[0]));
            this.i32 = parseInt(decodeURIComponent(parts[1]), 10);
            this.str = decodeURIComponent(parts[2]);
            this.uid = decodeURIComponent(parts[3]);
            this.user = new UserWithEnumId(decodeURIComponent(parts[4]));
        } else {
            this.bucket = new BucketID(data.bucket);
            this.user = new UserWithEnumId(data.user);
            this.i32 = data.i32;
            this.uid = data.uid;
            this.str = data.str;
        }
    }

    public toString(): string {
        const suffix = encodeURIComponent(this.bucket.toString()) + ':' + encodeURIComponent(this.i32.toString()) + ':' + encodeURIComponent(this.str) + ':' + encodeURIComponent(this.uid) + ':' + encodeURIComponent(this.user.toString());
        return 'ComplexID#' + suffix;
    }

    public serialize(): string {
        return this.toString();
    }
}

export interface IComplexID {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): string;

    bucket: BucketID;
    user: UserWithEnumId;
    i32: number;
    uid: string;
    str: string;
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
Introspector.register(ComplexID.FullClassName, {
        full: ComplexID.FullClassName,
        short: ComplexID.ClassName,
        package: ComplexID.PackageName,
        type: IntrospectorTypes.Id,
        ctor: () => new ComplexID(),
        fields: [
            {
                name: 'bucket',
                accessName: 'bucket',
                type: {intro: IntrospectorTypes.Id, full: 'idltest.identifiers.BucketID'} as IIntrospectorUserType
            },
            {
                name: 'user',
                accessName: 'user',
                type: {intro: IntrospectorTypes.Id, full: 'idltest.identifiers.UserWithEnumId'} as IIntrospectorUserType
            },
            {
                name: 'i32',
                accessName: 'i32',
                type: {intro: IntrospectorTypes.I32}
            },
            {
                name: 'uid',
                accessName: 'uid',
                type: {intro: IntrospectorTypes.Uid}
            },
            {
                name: 'str',
                accessName: 'str',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorIdObject
);