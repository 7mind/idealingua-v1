// Auto-generated, any modifications may be overwritten in the future.

// CommonFailure Interface
export interface CommonFailure {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): CommonFailureStructSerialized;

    code: number;
}

export interface CommonFailureStructSerialized {
    code: number;
}

export class CommonFailureStruct implements CommonFailure {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain01.CommonFailure';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'izumi.test.domain01.CommonFailure.Struct';

    public getPackageName(): string { return CommonFailureStruct.PackageName; }
    public getClassName(): string { return CommonFailureStruct.ClassName; }
    public getFullClassName(): string { return CommonFailureStruct.FullClassName; }

    private _code: number;

    public get code(): number {
        return this._code;
    }

    public set code(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field code is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field code expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field code is expected to be an integer, got ' + value);
        }

        this._code = value;
    }

    constructor(data: CommonFailureStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.code = data.code;
    }

    public serialize(): CommonFailureStructSerialized {
        return {
            code: this.code
        };
    }

    // Polymorphic section below. If a new type to be registered, use CommonFailureStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: CommonFailureStruct| CommonFailureStructSerialized): CommonFailure}} = {
        // This basic registration will happen below [CommonFailureStruct.FullClassName]: CommonFailureStruct
    };

    public static register(className: string, ctor: {new (data?: CommonFailureStruct| CommonFailureStructSerialized): CommonFailure}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: CommonFailureStructSerialized}): CommonFailure {
        const polymorphicId = Object.keys(data)[0];
        const ctor = CommonFailureStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for CommonFailureStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(CommonFailureStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in CommonFailureStruct._knownPolymorphic;
    }
}

CommonFailureStruct.register(CommonFailureStruct.FullClassName, CommonFailureStruct);

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorMixinObject,
    IIntrospectorDataObject
} from '../../../irt';
Introspector.register('izumi.test.domain01.CommonFailure', {
        full: 'izumi.test.domain01.CommonFailure',
        short: 'CommonFailure',
        package: 'izumi.test.domain01',
        type: IntrospectorTypes.Mixin,
        ctor: () => new CommonFailureStruct(),
        fields: [
            {
                name: 'code',
                accessName: 'code',
                type: {intro: IntrospectorTypes.I32}
            }
        ],
        implementations: CommonFailureStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(CommonFailureStruct.FullClassName, {
        full: CommonFailureStruct.FullClassName,
        short: CommonFailureStruct.ClassName,
        package: CommonFailureStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new CommonFailureStruct(),
        fields: [
            {
                name: 'code',
                accessName: 'code',
                type: {intro: IntrospectorTypes.I32}
            }
        ]
    } as IIntrospectorDataObject
);