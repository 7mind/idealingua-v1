// Auto-generated, any modifications may be overwritten in the future.
import {
    Type,
    TypeSerialized
} from './Type';

// TypeInfo Interface
export interface TypeInfo {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): TypeInfoStructSerialized;

    tpe: Type;
}

export interface TypeInfoStructSerialized {
    tpe: TypeSerialized;
}

export class TypeInfoStruct implements TypeInfo {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.ast.TypeInfo';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.ast.TypeInfo.Struct';

    public getPackageName(): string { return TypeInfoStruct.PackageName; }
    public getClassName(): string { return TypeInfoStruct.ClassName; }
    public getFullClassName(): string { return TypeInfoStruct.FullClassName; }

    private _tpe: Type;

    public get tpe(): Type {
        return this._tpe;
    }

    public set tpe(value: Type) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field tpe is not optional');
        }
        this._tpe = value;
    }

    constructor(data: TypeInfoStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.tpe = new Type(data.tpe);
    }

    public serialize(): TypeInfoStructSerialized {
        return {
            tpe: this.tpe.serialize()
        };
    }

    // Polymorphic section below. If a new type to be registered, use TypeInfoStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: TypeInfoStruct| TypeInfoStructSerialized): TypeInfo}} = {
        // This basic registration will happen below [TypeInfoStruct.FullClassName]: TypeInfoStruct
    };

    public static register(className: string, ctor: {new (data?: TypeInfoStruct| TypeInfoStructSerialized): TypeInfo}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: TypeInfoStructSerialized}): TypeInfo {
        const polymorphicId = Object.keys(data)[0];
        const ctor = TypeInfoStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for TypeInfoStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(TypeInfoStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in TypeInfoStruct._knownPolymorphic;
    }
}

TypeInfoStruct.register(TypeInfoStruct.FullClassName, TypeInfoStruct);

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
Introspector.register('idltest.ast.TypeInfo', {
        full: 'idltest.ast.TypeInfo',
        short: 'TypeInfo',
        package: 'idltest.ast',
        type: IntrospectorTypes.Mixin,
        ctor: () => new TypeInfoStruct(),
        fields: [
            {
                name: 'tpe',
                accessName: 'tpe',
                type: {intro: IntrospectorTypes.Data, full: 'idltest.ast.Type'} as IIntrospectorUserType
            }
        ],
        implementations: TypeInfoStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(TypeInfoStruct.FullClassName, {
        full: TypeInfoStruct.FullClassName,
        short: TypeInfoStruct.ClassName,
        package: TypeInfoStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new TypeInfoStruct(),
        fields: [
            {
                name: 'tpe',
                accessName: 'tpe',
                type: {intro: IntrospectorTypes.Data, full: 'idltest.ast.Type'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorDataObject
);