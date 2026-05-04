// Auto-generated, any modifications may be overwritten in the future.

// AnyValTest Interface
export interface AnyValTest {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): AnyValTestStructSerialized;

    boolField: boolean;
}

export interface AnyValTestStructSerialized {
    boolField: boolean;
}

export class AnyValTestStruct implements AnyValTest {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain01.AnyValTest';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'izumi.test.domain01.AnyValTest.Struct';

    public getPackageName(): string { return AnyValTestStruct.PackageName; }
    public getClassName(): string { return AnyValTestStruct.ClassName; }
    public getFullClassName(): string { return AnyValTestStruct.FullClassName; }

    private _boolField: boolean;

    public get boolField(): boolean {
        return this._boolField;
    }

    public set boolField(value: boolean) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field boolField is not optional');
        }

        if (typeof value !== 'boolean') {
            throw new Error('Field boolField expects boolean type, got ' + value);
        }

        this._boolField = value;
    }

    constructor(data: AnyValTestStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.boolField = data.boolField;
    }

    public serialize(): AnyValTestStructSerialized {
        return {
            boolField: this.boolField
        };
    }

    // Polymorphic section below. If a new type to be registered, use AnyValTestStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: AnyValTestStruct| AnyValTestStructSerialized): AnyValTest}} = {
        // This basic registration will happen below [AnyValTestStruct.FullClassName]: AnyValTestStruct
    };

    public static register(className: string, ctor: {new (data?: AnyValTestStruct| AnyValTestStructSerialized): AnyValTest}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: AnyValTestStructSerialized}): AnyValTest {
        const polymorphicId = Object.keys(data)[0];
        const ctor = AnyValTestStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for AnyValTestStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(AnyValTestStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in AnyValTestStruct._knownPolymorphic;
    }
}

AnyValTestStruct.register(AnyValTestStruct.FullClassName, AnyValTestStruct);

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
Introspector.register('izumi.test.domain01.AnyValTest', {
        full: 'izumi.test.domain01.AnyValTest',
        short: 'AnyValTest',
        package: 'izumi.test.domain01',
        type: IntrospectorTypes.Mixin,
        ctor: () => new AnyValTestStruct(),
        fields: [
            {
                name: 'boolField',
                accessName: 'boolField',
                type: {intro: IntrospectorTypes.Bool}
            }
        ],
        implementations: AnyValTestStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(AnyValTestStruct.FullClassName, {
        full: AnyValTestStruct.FullClassName,
        short: AnyValTestStruct.ClassName,
        package: AnyValTestStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new AnyValTestStruct(),
        fields: [
            {
                name: 'boolField',
                accessName: 'boolField',
                type: {intro: IntrospectorTypes.Bool}
            }
        ]
    } as IIntrospectorDataObject
);