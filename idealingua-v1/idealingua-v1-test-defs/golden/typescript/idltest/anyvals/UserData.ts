// Auto-generated, any modifications may be overwritten in the future.
import {
    WithRecordId,
    WithRecordIdStruct,
    WithRecordIdStructSerialized
} from './WithRecordId';

// UserData Interface
export interface UserData {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): UserDataStructSerialized;

    id: WithRecordId;
}

export interface UserDataStructSerialized {
    id: {[key: string]: WithRecordIdStructSerialized};
}

export class UserDataStruct implements UserData {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.anyvals.UserData';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.anyvals.UserData.Struct';

    public getPackageName(): string { return UserDataStruct.PackageName; }
    public getClassName(): string { return UserDataStruct.ClassName; }
    public getFullClassName(): string { return UserDataStruct.FullClassName; }

    private _id: WithRecordId;

    public get id(): WithRecordId {
        return this._id;
    }

    public set id(value: WithRecordId) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field id is not optional');
        }
        this._id = value;
    }

    constructor(data: UserDataStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.id = WithRecordIdStruct.create(data.id);
    }

    public serialize(): UserDataStructSerialized {
        return {
            id: {[this.id.getFullClassName()]: this.id.serialize()}
        };
    }

    // Polymorphic section below. If a new type to be registered, use UserDataStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: UserDataStruct| UserDataStructSerialized): UserData}} = {
        // This basic registration will happen below [UserDataStruct.FullClassName]: UserDataStruct
    };

    public static register(className: string, ctor: {new (data?: UserDataStruct| UserDataStructSerialized): UserData}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: UserDataStructSerialized}): UserData {
        const polymorphicId = Object.keys(data)[0];
        const ctor = UserDataStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for UserDataStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(UserDataStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in UserDataStruct._knownPolymorphic;
    }
}

UserDataStruct.register(UserDataStruct.FullClassName, UserDataStruct);

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
Introspector.register('idltest.anyvals.UserData', {
        full: 'idltest.anyvals.UserData',
        short: 'UserData',
        package: 'idltest.anyvals',
        type: IntrospectorTypes.Mixin,
        ctor: () => new UserDataStruct(),
        fields: [
            {
                name: 'id',
                accessName: 'id',
                type: {intro: IntrospectorTypes.Mixin, full: 'idltest.anyvals.WithRecordId'} as IIntrospectorUserType
            }
        ],
        implementations: UserDataStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(UserDataStruct.FullClassName, {
        full: UserDataStruct.FullClassName,
        short: UserDataStruct.ClassName,
        package: UserDataStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new UserDataStruct(),
        fields: [
            {
                name: 'id',
                accessName: 'id',
                type: {intro: IntrospectorTypes.Mixin, full: 'idltest.anyvals.WithRecordId'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorDataObject
);