// Auto-generated, any modifications may be overwritten in the future.
import { Formatter } from '../../irt';
import {
    NotiBase,
    NotiBaseStruct,
    NotiBaseStructSerialized
} from './NotiBase';

// NotiWithFile Interface
export interface NotiWithFile extends NotiBase {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): NotiWithFileStructSerialized;

    at: Date;
    userID: string;
    userName: string | undefined;
    message: string | undefined;
    fileID: number;
    fileName: string;
}

export interface NotiWithFileStructSerialized extends NotiBaseStructSerialized {
    at: string;
    userID: string;
    userName: string | undefined;
    message: string | undefined;
    fileID: number;
    fileName: string;
}

export class NotiWithFileStruct implements NotiWithFile {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.inheritance.NotiWithFile';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.inheritance.NotiWithFile.Struct';

    public getPackageName(): string { return NotiWithFileStruct.PackageName; }
    public getClassName(): string { return NotiWithFileStruct.ClassName; }
    public getFullClassName(): string { return NotiWithFileStruct.FullClassName; }

    private _at: Date;
    private _userID: string;
    private _userName: string | undefined;
    private _message: string | undefined;
    private _fileID: number;
    private _fileName: string;

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

    public get fileID(): number {
        return this._fileID;
    }

    public set fileID(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field fileID is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field fileID expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field fileID is expected to be an integer, got ' + value);
        }

        this._fileID = value;
    }

    public get fileName(): string {
        return this._fileName;
    }

    public set fileName(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field fileName is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field fileName expects type string, got ' + value);
        }

        this._fileName = value;
    }

    constructor(data: NotiWithFileStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.atAsString = data.at;
        this.userID = data.userID;
        this.userName = typeof data.userName !== 'undefined' ? data.userName : undefined;
        this.message = typeof data.message !== 'undefined' ? data.message : undefined;
        this.fileID = data.fileID;
        this.fileName = data.fileName;
    }

    public serialize(): NotiWithFileStructSerialized {
        return {
            at: this.atAsString,
            userID: this.userID,
            userName: typeof this.userName !== 'undefined' ? this.userName : undefined,
            message: typeof this.message !== 'undefined' ? this.message : undefined,
            fileID: this.fileID,
            fileName: this.fileName
        };
    }

    // Polymorphic section below. If a new type to be registered, use NotiWithFileStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: NotiWithFileStruct| NotiWithFileStructSerialized): NotiWithFile}} = {
        // This basic registration will happen below [NotiWithFileStruct.FullClassName]: NotiWithFileStruct
    };

    public static register(className: string, ctor: {new (data?: NotiWithFileStruct| NotiWithFileStructSerialized): NotiWithFile}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: NotiWithFileStructSerialized}): NotiWithFile {
        const polymorphicId = Object.keys(data)[0];
        const ctor = NotiWithFileStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for NotiWithFileStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(NotiWithFileStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in NotiWithFileStruct._knownPolymorphic;
    }
}

NotiWithFileStruct.register(NotiWithFileStruct.FullClassName, NotiWithFileStruct);
NotiBaseStruct.register(NotiWithFileStruct.FullClassName, NotiWithFileStruct);

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
Introspector.register('idltest.inheritance.NotiWithFile', {
        full: 'idltest.inheritance.NotiWithFile',
        short: 'NotiWithFile',
        package: 'idltest.inheritance',
        type: IntrospectorTypes.Mixin,
        ctor: () => new NotiWithFileStruct(),
        fields: [
            {
                name: 'fileID',
                accessName: 'fileID',
                type: {intro: IntrospectorTypes.I64}
            },
            {
                name: 'fileName',
                accessName: 'fileName',
                type: {intro: IntrospectorTypes.Str}
            }
        ],
        implementations: NotiWithFileStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(NotiWithFileStruct.FullClassName, {
        full: NotiWithFileStruct.FullClassName,
        short: NotiWithFileStruct.ClassName,
        package: NotiWithFileStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new NotiWithFileStruct(),
        fields: [
            {
                name: 'fileID',
                accessName: 'fileID',
                type: {intro: IntrospectorTypes.I64}
            },
            {
                name: 'fileName',
                accessName: 'fileName',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);