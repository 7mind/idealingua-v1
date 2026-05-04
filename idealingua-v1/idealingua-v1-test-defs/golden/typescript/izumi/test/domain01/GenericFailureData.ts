// Auto-generated, any modifications may be overwritten in the future.

// GenericFailureData Interface
export interface GenericFailureData {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): GenericFailureDataStructSerialized;

    message: string;
    diagnostics: string | undefined;
    reserved: {[key: string]: string};
}

export interface GenericFailureDataStructSerialized {
    message: string;
    diagnostics: string | undefined;
    reserved: {[key: string]: string};
}

export class GenericFailureDataStruct implements GenericFailureData {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain01.GenericFailureData';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'izumi.test.domain01.GenericFailureData.Struct';

    public getPackageName(): string { return GenericFailureDataStruct.PackageName; }
    public getClassName(): string { return GenericFailureDataStruct.ClassName; }
    public getFullClassName(): string { return GenericFailureDataStruct.FullClassName; }

    private _message: string;
    private _diagnostics: string | undefined;
    private _reserved: {[key: string]: string};

    public get message(): string {
        return this._message;
    }

    public set message(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field message is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field message expects type string, got ' + value);
        }

        this._message = value;
    }

    public get diagnostics(): string | undefined {
        return this._diagnostics;
    }

    public set diagnostics(value: string | undefined) {
        if (typeof value === 'undefined' || value === null) {
            this._diagnostics = undefined;
            return;
        }

        if (typeof value !== 'string') {
            throw new Error('Field diagnostics expects type string, got ' + value);
        }

        this._diagnostics = value;
    }

    public get reserved(): {[key: string]: string} {
        return this._reserved;
    }

    public set reserved(value: {[key: string]: string}) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field reserved is not optional');
        }
        this._reserved = value;
    }

    constructor(data: GenericFailureDataStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            this.reserved = {};
            return;
        }

        this.message = data.message;
        this.diagnostics = typeof data.diagnostics !== 'undefined' ? data.diagnostics : undefined;
        this.reserved = Object.keys(data.reserved).reduce<any>((previous, current) => {previous[current] = data.reserved[current as any]; return previous; }, {});
    }

    public serialize(): GenericFailureDataStructSerialized {
        return {
            message: this.message,
            diagnostics: typeof this.diagnostics !== 'undefined' ? this.diagnostics : undefined,
            reserved: Object.keys(this.reserved).reduce<any>((previous, current) => {previous[current] = this.reserved[current as any]; return previous; }, {})
        };
    }

    // Polymorphic section below. If a new type to be registered, use GenericFailureDataStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: GenericFailureDataStruct| GenericFailureDataStructSerialized): GenericFailureData}} = {
        // This basic registration will happen below [GenericFailureDataStruct.FullClassName]: GenericFailureDataStruct
    };

    public static register(className: string, ctor: {new (data?: GenericFailureDataStruct| GenericFailureDataStructSerialized): GenericFailureData}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: GenericFailureDataStructSerialized}): GenericFailureData {
        const polymorphicId = Object.keys(data)[0];
        const ctor = GenericFailureDataStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for GenericFailureDataStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(GenericFailureDataStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in GenericFailureDataStruct._knownPolymorphic;
    }
}

GenericFailureDataStruct.register(GenericFailureDataStruct.FullClassName, GenericFailureDataStruct);

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
Introspector.register('izumi.test.domain01.GenericFailureData', {
        full: 'izumi.test.domain01.GenericFailureData',
        short: 'GenericFailureData',
        package: 'izumi.test.domain01',
        type: IntrospectorTypes.Mixin,
        ctor: () => new GenericFailureDataStruct(),
        fields: [
            {
                name: 'message',
                accessName: 'message',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'diagnostics',
                accessName: 'diagnostics',
                type: {intro: IntrospectorTypes.Opt, value: {intro: IntrospectorTypes.Str}} as IIntrospectorGenericType
            },
            {
                name: 'reserved',
                accessName: 'reserved',
                type: {intro: IntrospectorTypes.Map, key: {intro: IntrospectorTypes.Str}, value: {intro: IntrospectorTypes.Str}} as IIntrospectorMapType
            }
        ],
        implementations: GenericFailureDataStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(GenericFailureDataStruct.FullClassName, {
        full: GenericFailureDataStruct.FullClassName,
        short: GenericFailureDataStruct.ClassName,
        package: GenericFailureDataStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new GenericFailureDataStruct(),
        fields: [
            {
                name: 'message',
                accessName: 'message',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'diagnostics',
                accessName: 'diagnostics',
                type: {intro: IntrospectorTypes.Opt, value: {intro: IntrospectorTypes.Str}} as IIntrospectorGenericType
            },
            {
                name: 'reserved',
                accessName: 'reserved',
                type: {intro: IntrospectorTypes.Map, key: {intro: IntrospectorTypes.Str}, value: {intro: IntrospectorTypes.Str}} as IIntrospectorMapType
            }
        ]
    } as IIntrospectorDataObject
);