// Auto-generated, any modifications may be overwritten in the future.
import { Formatter } from '../../irt';

// NotiBase Interface
export interface NotiBase {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): NotiBaseStructSerialized;

    at: Date;
    userID: string;
    userName: string | undefined;
    message: string | undefined;
}

export interface NotiBaseStructSerialized {
    at: string;
    userID: string;
    userName: string | undefined;
    message: string | undefined;
}

export class NotiBaseStruct implements NotiBase {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.inheritance.NotiBase';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.inheritance.NotiBase.Struct';

    public getPackageName(): string { return NotiBaseStruct.PackageName; }
    public getClassName(): string { return NotiBaseStruct.ClassName; }
    public getFullClassName(): string { return NotiBaseStruct.FullClassName; }

    private _at: Date;
    private _userID: string;
    private _userName: string | undefined;
    private _message: string | undefined;

    public get at(): Date {
        return this._at;
    }

    public set at(value: Date) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field at is not optional');
        }

        if (!(value instanceof Date)) {
            throw new Error('Field at expects type Date, got ' + value);
        }
        this._at = value;
    }

    public get atAsString(): string {
        return Formatter.writeZoneDateTime(this._at);
    }

    public set atAsString(value: string) {
        if (typeof value !== 'string') {
            throw new Error('atAsString expects type string, got ' + value);
        }
        this._at = Formatter.readZoneDateTime(value);
    }

    public get userID(): string {
        return this._userID;
    }

    public set userID(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field userID is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field userID expects type string, got ' + value);
        }

        this._userID = value;
    }

    public get userName(): string | undefined {
        return this._userName;
    }

    public set userName(value: string | undefined) {
        if (typeof value === 'undefined' || value === null) {
            this._userName = undefined;
            return;
        }

        if (typeof value !== 'string') {
            throw new Error('Field userName expects type string, got ' + value);
        }

        this._userName = value;
    }

    public get message(): string | undefined {
        return this._message;
    }

    public set message(value: string | undefined) {
        if (typeof value === 'undefined' || value === null) {
            this._message = undefined;
            return;
        }

        if (typeof value !== 'string') {
            throw new Error('Field message expects type string, got ' + value);
        }

        this._message = value;
    }

    constructor(data: NotiBaseStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.atAsString = data.at;
        this.userID = data.userID;
        this.userName = typeof data.userName !== 'undefined' ? data.userName : undefined;
        this.message = typeof data.message !== 'undefined' ? data.message : undefined;
    }

    public serialize(): NotiBaseStructSerialized {
        return {
            at: this.atAsString,
            userID: this.userID,
            userName: typeof this.userName !== 'undefined' ? this.userName : undefined,
            message: typeof this.message !== 'undefined' ? this.message : undefined
        };
    }

    // Polymorphic section below. If a new type to be registered, use NotiBaseStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: NotiBaseStruct| NotiBaseStructSerialized): NotiBase}} = {
        // This basic registration will happen below [NotiBaseStruct.FullClassName]: NotiBaseStruct
    };

    public static register(className: string, ctor: {new (data?: NotiBaseStruct| NotiBaseStructSerialized): NotiBase}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: NotiBaseStructSerialized}): NotiBase {
        const polymorphicId = Object.keys(data)[0];
        const ctor = NotiBaseStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for NotiBaseStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(NotiBaseStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in NotiBaseStruct._knownPolymorphic;
    }
}

NotiBaseStruct.register(NotiBaseStruct.FullClassName, NotiBaseStruct);

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
Introspector.register('idltest.inheritance.NotiBase', {
        full: 'idltest.inheritance.NotiBase',
        short: 'NotiBase',
        package: 'idltest.inheritance',
        type: IntrospectorTypes.Mixin,
        ctor: () => new NotiBaseStruct(),
        fields: [
            {
                name: 'at',
                accessName: 'at',
                type: {intro: IntrospectorTypes.Tsz}
            },
            {
                name: 'userID',
                accessName: 'userID',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'userName',
                accessName: 'userName',
                type: {intro: IntrospectorTypes.Opt, value: {intro: IntrospectorTypes.Str}} as IIntrospectorGenericType
            },
            {
                name: 'message',
                accessName: 'message',
                type: {intro: IntrospectorTypes.Opt, value: {intro: IntrospectorTypes.Str}} as IIntrospectorGenericType
            }
        ],
        implementations: NotiBaseStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(NotiBaseStruct.FullClassName, {
        full: NotiBaseStruct.FullClassName,
        short: NotiBaseStruct.ClassName,
        package: NotiBaseStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new NotiBaseStruct(),
        fields: [
            {
                name: 'at',
                accessName: 'at',
                type: {intro: IntrospectorTypes.Tsz}
            },
            {
                name: 'userID',
                accessName: 'userID',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'userName',
                accessName: 'userName',
                type: {intro: IntrospectorTypes.Opt, value: {intro: IntrospectorTypes.Str}} as IIntrospectorGenericType
            },
            {
                name: 'message',
                accessName: 'message',
                type: {intro: IntrospectorTypes.Opt, value: {intro: IntrospectorTypes.Str}} as IIntrospectorGenericType
            }
        ]
    } as IIntrospectorDataObject
);