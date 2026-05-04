// Auto-generated, any modifications may be overwritten in the future.

// LengthInBytes Interface
export interface LengthInBytes {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): LengthInBytesStructSerialized;

    bytes: number;
}

export interface LengthInBytesStructSerialized {
    bytes: number;
}

export class LengthInBytesStruct implements LengthInBytes {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.phase.LengthInBytes';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.phase.LengthInBytes.Struct';

    public getPackageName(): string { return LengthInBytesStruct.PackageName; }
    public getClassName(): string { return LengthInBytesStruct.ClassName; }
    public getFullClassName(): string { return LengthInBytesStruct.FullClassName; }

    private _bytes: number;

    public get bytes(): number {
        return this._bytes;
    }

    public set bytes(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field bytes is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field bytes expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field bytes is expected to be an integer, got ' + value);
        }

        this._bytes = value;
    }

    constructor(data: LengthInBytesStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.bytes = data.bytes;
    }

    public serialize(): LengthInBytesStructSerialized {
        return {
            bytes: this.bytes
        };
    }

    // Polymorphic section below. If a new type to be registered, use LengthInBytesStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: LengthInBytesStruct| LengthInBytesStructSerialized): LengthInBytes}} = {
        // This basic registration will happen below [LengthInBytesStruct.FullClassName]: LengthInBytesStruct
    };

    public static register(className: string, ctor: {new (data?: LengthInBytesStruct| LengthInBytesStructSerialized): LengthInBytes}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: LengthInBytesStructSerialized}): LengthInBytes {
        const polymorphicId = Object.keys(data)[0];
        const ctor = LengthInBytesStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for LengthInBytesStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(LengthInBytesStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in LengthInBytesStruct._knownPolymorphic;
    }
}

LengthInBytesStruct.register(LengthInBytesStruct.FullClassName, LengthInBytesStruct);

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
Introspector.register('idltest.phase.LengthInBytes', {
        full: 'idltest.phase.LengthInBytes',
        short: 'LengthInBytes',
        package: 'idltest.phase',
        type: IntrospectorTypes.Mixin,
        ctor: () => new LengthInBytesStruct(),
        fields: [
            {
                name: 'bytes',
                accessName: 'bytes',
                type: {intro: IntrospectorTypes.I64}
            }
        ],
        implementations: LengthInBytesStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(LengthInBytesStruct.FullClassName, {
        full: LengthInBytesStruct.FullClassName,
        short: LengthInBytesStruct.ClassName,
        package: LengthInBytesStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new LengthInBytesStruct(),
        fields: [
            {
                name: 'bytes',
                accessName: 'bytes',
                type: {intro: IntrospectorTypes.I64}
            }
        ]
    } as IIntrospectorDataObject
);