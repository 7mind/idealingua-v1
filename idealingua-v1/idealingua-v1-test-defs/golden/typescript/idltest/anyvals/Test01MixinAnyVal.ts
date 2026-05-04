// Auto-generated, any modifications may be overwritten in the future.

// Test01MixinAnyVal Interface
export interface Test01MixinAnyVal {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): Test01MixinAnyValStructSerialized;

    value: string;
}

export interface Test01MixinAnyValStructSerialized {
    value: string;
}

export class Test01MixinAnyValStruct implements Test01MixinAnyVal {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.anyvals.Test01MixinAnyVal';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.anyvals.Test01MixinAnyVal.Struct';

    public getPackageName(): string { return Test01MixinAnyValStruct.PackageName; }
    public getClassName(): string { return Test01MixinAnyValStruct.ClassName; }
    public getFullClassName(): string { return Test01MixinAnyValStruct.FullClassName; }

    private _value: string;

    public get value(): string {
        return this._value;
    }

    public set value(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field value is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field value expects type string, got ' + value);
        }

        this._value = value;
    }

    constructor(data: Test01MixinAnyValStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.value = data.value;
    }

    public serialize(): Test01MixinAnyValStructSerialized {
        return {
            value: this.value
        };
    }

    // Polymorphic section below. If a new type to be registered, use Test01MixinAnyValStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: Test01MixinAnyValStruct| Test01MixinAnyValStructSerialized): Test01MixinAnyVal}} = {
        // This basic registration will happen below [Test01MixinAnyValStruct.FullClassName]: Test01MixinAnyValStruct
    };

    public static register(className: string, ctor: {new (data?: Test01MixinAnyValStruct| Test01MixinAnyValStructSerialized): Test01MixinAnyVal}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: Test01MixinAnyValStructSerialized}): Test01MixinAnyVal {
        const polymorphicId = Object.keys(data)[0];
        const ctor = Test01MixinAnyValStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for Test01MixinAnyValStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(Test01MixinAnyValStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in Test01MixinAnyValStruct._knownPolymorphic;
    }
}

Test01MixinAnyValStruct.register(Test01MixinAnyValStruct.FullClassName, Test01MixinAnyValStruct);

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
Introspector.register('idltest.anyvals.Test01MixinAnyVal', {
        full: 'idltest.anyvals.Test01MixinAnyVal',
        short: 'Test01MixinAnyVal',
        package: 'idltest.anyvals',
        type: IntrospectorTypes.Mixin,
        ctor: () => new Test01MixinAnyValStruct(),
        fields: [
            {
                name: 'value',
                accessName: 'value',
                type: {intro: IntrospectorTypes.Str}
            }
        ],
        implementations: Test01MixinAnyValStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(Test01MixinAnyValStruct.FullClassName, {
        full: Test01MixinAnyValStruct.FullClassName,
        short: Test01MixinAnyValStruct.ClassName,
        package: Test01MixinAnyValStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new Test01MixinAnyValStruct(),
        fields: [
            {
                name: 'value',
                accessName: 'value',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);