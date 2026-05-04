// Auto-generated, any modifications may be overwritten in the future.
import {
    AnyValTest,
    AnyValTestStruct,
    AnyValTestStructSerialized
} from './AnyValTest';

// AnyValTest2 Interface
export interface AnyValTest2 {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): AnyValTest2StructSerialized;

    field: AnyValTest;
}

export interface AnyValTest2StructSerialized {
    field: {[key: string]: AnyValTestStructSerialized};
}

export class AnyValTest2Struct implements AnyValTest2 {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain01.AnyValTest2';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'izumi.test.domain01.AnyValTest2.Struct';

    public getPackageName(): string { return AnyValTest2Struct.PackageName; }
    public getClassName(): string { return AnyValTest2Struct.ClassName; }
    public getFullClassName(): string { return AnyValTest2Struct.FullClassName; }

    private _field: AnyValTest;

    public get field(): AnyValTest {
        return this._field;
    }

    public set field(value: AnyValTest) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field field is not optional');
        }
        this._field = value;
    }

    constructor(data: AnyValTest2StructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.field = AnyValTestStruct.create(data.field);
    }

    public serialize(): AnyValTest2StructSerialized {
        return {
            field: {[this.field.getFullClassName()]: this.field.serialize()}
        };
    }

    // Polymorphic section below. If a new type to be registered, use AnyValTest2Struct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: AnyValTest2Struct| AnyValTest2StructSerialized): AnyValTest2}} = {
        // This basic registration will happen below [AnyValTest2Struct.FullClassName]: AnyValTest2Struct
    };

    public static register(className: string, ctor: {new (data?: AnyValTest2Struct| AnyValTest2StructSerialized): AnyValTest2}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: AnyValTest2StructSerialized}): AnyValTest2 {
        const polymorphicId = Object.keys(data)[0];
        const ctor = AnyValTest2Struct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for AnyValTest2Struct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(AnyValTest2Struct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in AnyValTest2Struct._knownPolymorphic;
    }
}

AnyValTest2Struct.register(AnyValTest2Struct.FullClassName, AnyValTest2Struct);

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
Introspector.register('izumi.test.domain01.AnyValTest2', {
        full: 'izumi.test.domain01.AnyValTest2',
        short: 'AnyValTest2',
        package: 'izumi.test.domain01',
        type: IntrospectorTypes.Mixin,
        ctor: () => new AnyValTest2Struct(),
        fields: [
            {
                name: 'field',
                accessName: 'field',
                type: {intro: IntrospectorTypes.Mixin, full: 'izumi.test.domain01.AnyValTest'} as IIntrospectorUserType
            }
        ],
        implementations: AnyValTest2Struct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(AnyValTest2Struct.FullClassName, {
        full: AnyValTest2Struct.FullClassName,
        short: AnyValTest2Struct.ClassName,
        package: AnyValTest2Struct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new AnyValTest2Struct(),
        fields: [
            {
                name: 'field',
                accessName: 'field',
                type: {intro: IntrospectorTypes.Mixin, full: 'izumi.test.domain01.AnyValTest'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorDataObject
);