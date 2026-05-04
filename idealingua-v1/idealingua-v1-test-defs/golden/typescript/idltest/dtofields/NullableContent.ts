// Auto-generated, any modifications may be overwritten in the future.

// NullableContent Interface
export interface NullableContent {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): NullableContentStructSerialized;

    a: number;
}

export interface NullableContentStructSerialized {
    a: number;
}

export class NullableContentStruct implements NullableContent {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.dtofields.NullableContent';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.dtofields.NullableContent.Struct';

    public getPackageName(): string { return NullableContentStruct.PackageName; }
    public getClassName(): string { return NullableContentStruct.ClassName; }
    public getFullClassName(): string { return NullableContentStruct.FullClassName; }

    private _a: number;

    public get a(): number {
        return this._a;
    }

    public set a(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field a is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field a expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field a is expected to be an integer, got ' + value);
        }

        this._a = value;
    }

    constructor(data: NullableContentStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.a = data.a;
    }

    public serialize(): NullableContentStructSerialized {
        return {
            a: this.a
        };
    }

    // Polymorphic section below. If a new type to be registered, use NullableContentStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: NullableContentStruct| NullableContentStructSerialized): NullableContent}} = {
        // This basic registration will happen below [NullableContentStruct.FullClassName]: NullableContentStruct
    };

    public static register(className: string, ctor: {new (data?: NullableContentStruct| NullableContentStructSerialized): NullableContent}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: NullableContentStructSerialized}): NullableContent {
        const polymorphicId = Object.keys(data)[0];
        const ctor = NullableContentStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for NullableContentStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(NullableContentStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in NullableContentStruct._knownPolymorphic;
    }
}

NullableContentStruct.register(NullableContentStruct.FullClassName, NullableContentStruct);

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
Introspector.register('idltest.dtofields.NullableContent', {
        full: 'idltest.dtofields.NullableContent',
        short: 'NullableContent',
        package: 'idltest.dtofields',
        type: IntrospectorTypes.Mixin,
        ctor: () => new NullableContentStruct(),
        fields: [
            {
                name: 'a',
                accessName: 'a',
                type: {intro: IntrospectorTypes.I32}
            }
        ],
        implementations: NullableContentStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(NullableContentStruct.FullClassName, {
        full: NullableContentStruct.FullClassName,
        short: NullableContentStruct.ClassName,
        package: NullableContentStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new NullableContentStruct(),
        fields: [
            {
                name: 'a',
                accessName: 'a',
                type: {intro: IntrospectorTypes.I32}
            }
        ]
    } as IIntrospectorDataObject
);