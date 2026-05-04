// Auto-generated, any modifications may be overwritten in the future.
import { Formatter } from '../../irt';
import {
    NotiBaseStruct,
    NotiBaseStructSerialized
} from './NotiBase';
import {
    NotiWithFile,
    NotiWithFileStruct,
    NotiWithFileStructSerialized
} from './NotiWithFile';

// NotiWithFileRevision Interface
export interface NotiWithFileRevision extends NotiWithFile {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): NotiWithFileRevisionStructSerialized;

    at: Date;
    userID: string;
    userName: string | undefined;
    message: string | undefined;
    fileID: number;
    fileName: string;
    fileRevision: number;
}

export interface NotiWithFileRevisionStructSerialized extends NotiWithFileStructSerialized {
    at: string;
    userID: string;
    userName: string | undefined;
    message: string | undefined;
    fileID: number;
    fileName: string;
    fileRevision: number;
}

export class NotiWithFileRevisionStruct implements NotiWithFileRevision {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.inheritance.NotiWithFileRevision';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.inheritance.NotiWithFileRevision.Struct';

    public getPackageName(): string { return NotiWithFileRevisionStruct.PackageName; }
    public getClassName(): string { return NotiWithFileRevisionStruct.ClassName; }
    public getFullClassName(): string { return NotiWithFileRevisionStruct.FullClassName; }

    private _at: Date;
    private _userID: string;
    private _userName: string | undefined;
    private _message: string | undefined;
    private _fileID: number;
    private _fileName: string;
    private _fileRevision: number;

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

    public get fileRevision(): number {
        return this._fileRevision;
    }

    public set fileRevision(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field fileRevision is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field fileRevision expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field fileRevision is expected to be an integer, got ' + value);
        }

        this._fileRevision = value;
    }

    constructor(data: NotiWithFileRevisionStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.atAsString = data.at;
        this.userID = data.userID;
        this.userName = typeof data.userName !== 'undefined' ? data.userName : undefined;
        this.message = typeof data.message !== 'undefined' ? data.message : undefined;
        this.fileID = data.fileID;
        this.fileName = data.fileName;
        this.fileRevision = data.fileRevision;
    }

    public serialize(): NotiWithFileRevisionStructSerialized {
        return {
            at: this.atAsString,
            userID: this.userID,
            userName: typeof this.userName !== 'undefined' ? this.userName : undefined,
            message: typeof this.message !== 'undefined' ? this.message : undefined,
            fileID: this.fileID,
            fileName: this.fileName,
            fileRevision: this.fileRevision
        };
    }

    // Polymorphic section below. If a new type to be registered, use NotiWithFileRevisionStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: NotiWithFileRevisionStruct| NotiWithFileRevisionStructSerialized): NotiWithFileRevision}} = {
        // This basic registration will happen below [NotiWithFileRevisionStruct.FullClassName]: NotiWithFileRevisionStruct
    };

    public static register(className: string, ctor: {new (data?: NotiWithFileRevisionStruct| NotiWithFileRevisionStructSerialized): NotiWithFileRevision}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: NotiWithFileRevisionStructSerialized}): NotiWithFileRevision {
        const polymorphicId = Object.keys(data)[0];
        const ctor = NotiWithFileRevisionStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for NotiWithFileRevisionStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(NotiWithFileRevisionStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in NotiWithFileRevisionStruct._knownPolymorphic;
    }
}

NotiWithFileRevisionStruct.register(NotiWithFileRevisionStruct.FullClassName, NotiWithFileRevisionStruct);
NotiWithFileStruct.register(NotiWithFileRevisionStruct.FullClassName, NotiWithFileRevisionStruct);
NotiBaseStruct.register(NotiWithFileRevisionStruct.FullClassName, NotiWithFileRevisionStruct);

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
Introspector.register('idltest.inheritance.NotiWithFileRevision', {
        full: 'idltest.inheritance.NotiWithFileRevision',
        short: 'NotiWithFileRevision',
        package: 'idltest.inheritance',
        type: IntrospectorTypes.Mixin,
        ctor: () => new NotiWithFileRevisionStruct(),
        fields: [
            {
                name: 'fileRevision',
                accessName: 'fileRevision',
                type: {intro: IntrospectorTypes.I64}
            }
        ],
        implementations: NotiWithFileRevisionStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(NotiWithFileRevisionStruct.FullClassName, {
        full: NotiWithFileRevisionStruct.FullClassName,
        short: NotiWithFileRevisionStruct.ClassName,
        package: NotiWithFileRevisionStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new NotiWithFileRevisionStruct(),
        fields: [
            {
                name: 'fileRevision',
                accessName: 'fileRevision',
                type: {intro: IntrospectorTypes.I64}
            }
        ]
    } as IIntrospectorDataObject
);