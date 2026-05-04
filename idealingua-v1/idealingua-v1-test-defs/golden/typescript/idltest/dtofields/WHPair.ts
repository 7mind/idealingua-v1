// Auto-generated, any modifications may be overwritten in the future.

// WHPair Interface
export interface WHPair {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): WHPairStructSerialized;

    w: number;
    h: number;
}

export interface WHPairStructSerialized {
    w: number;
    h: number;
}

export class WHPairStruct implements WHPair {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.dtofields.WHPair';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.dtofields.WHPair.Struct';

    public getPackageName(): string { return WHPairStruct.PackageName; }
    public getClassName(): string { return WHPairStruct.ClassName; }
    public getFullClassName(): string { return WHPairStruct.FullClassName; }

    private _w: number;
    private _h: number;

    public get w(): number {
        return this._w;
    }

    public set w(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field w is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field w expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field w is expected to be an integer, got ' + value);
        }

        this._w = value;
    }

    public get h(): number {
        return this._h;
    }

    public set h(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field h is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field h expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field h is expected to be an integer, got ' + value);
        }

        this._h = value;
    }

    constructor(data: WHPairStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.w = data.w;
        this.h = data.h;
    }

    public serialize(): WHPairStructSerialized {
        return {
            w: this.w,
            h: this.h
        };
    }

    // Polymorphic section below. If a new type to be registered, use WHPairStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: WHPairStruct| WHPairStructSerialized): WHPair}} = {
        // This basic registration will happen below [WHPairStruct.FullClassName]: WHPairStruct
    };

    public static register(className: string, ctor: {new (data?: WHPairStruct| WHPairStructSerialized): WHPair}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: WHPairStructSerialized}): WHPair {
        const polymorphicId = Object.keys(data)[0];
        const ctor = WHPairStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for WHPairStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(WHPairStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in WHPairStruct._knownPolymorphic;
    }
}

WHPairStruct.register(WHPairStruct.FullClassName, WHPairStruct);

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
Introspector.register('idltest.dtofields.WHPair', {
        full: 'idltest.dtofields.WHPair',
        short: 'WHPair',
        package: 'idltest.dtofields',
        type: IntrospectorTypes.Mixin,
        ctor: () => new WHPairStruct(),
        fields: [
            {
                name: 'w',
                accessName: 'w',
                type: {intro: IntrospectorTypes.I32}
            },
            {
                name: 'h',
                accessName: 'h',
                type: {intro: IntrospectorTypes.I32}
            }
        ],
        implementations: WHPairStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(WHPairStruct.FullClassName, {
        full: WHPairStruct.FullClassName,
        short: WHPairStruct.ClassName,
        package: WHPairStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new WHPairStruct(),
        fields: [
            {
                name: 'w',
                accessName: 'w',
                type: {intro: IntrospectorTypes.I32}
            },
            {
                name: 'h',
                accessName: 'h',
                type: {intro: IntrospectorTypes.I32}
            }
        ]
    } as IIntrospectorDataObject
);