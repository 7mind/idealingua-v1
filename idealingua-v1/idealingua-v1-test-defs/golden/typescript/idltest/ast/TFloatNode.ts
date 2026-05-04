// Auto-generated, any modifications may be overwritten in the future.
import {
    FloatNode,
    FloatNodeStruct,
    FloatNodeStructSerialized
} from './FloatNode';
import {
    TypeInfoStruct,
    TypeInfoStructSerialized
} from './TypeInfo';
import {
    Type,
    TypeSerialized
} from './Type';

// TFloatNode Interface
export interface TFloatNode extends FloatNode {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): TFloatNodeStructSerialized;

    tpe: Type;
    lit: number;
}

export interface TFloatNodeStructSerialized extends FloatNodeStructSerialized {
    tpe: TypeSerialized;
    lit: number;
}

export class TFloatNodeStruct implements TFloatNode {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.ast.TFloatNode';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.ast.TFloatNode.Struct';

    public getPackageName(): string { return TFloatNodeStruct.PackageName; }
    public getClassName(): string { return TFloatNodeStruct.ClassName; }
    public getFullClassName(): string { return TFloatNodeStruct.FullClassName; }

    private _tpe: Type;
    private _lit: number;

    public get tpe(): Type {
        return this._tpe;
    }

    public set tpe(value: Type) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field tpe is not optional');
        }
        this._tpe = value;
    }

    public get lit(): number {
        // Precision: 32
        return this._lit;
    }

    public set lit(value: number) {
        // Precision: 32
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field lit is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field lit expects type number, got ' + value);
        }

        this._lit = value;
    }

    constructor(data: TFloatNodeStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.tpe = new Type(data.tpe);
        this.lit = data.lit;
    }

    public serialize(): TFloatNodeStructSerialized {
        return {
            tpe: this.tpe.serialize(),
            lit: this.lit
        };
    }

    // Polymorphic section below. If a new type to be registered, use TFloatNodeStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: TFloatNodeStruct| TFloatNodeStructSerialized): TFloatNode}} = {
        // This basic registration will happen below [TFloatNodeStruct.FullClassName]: TFloatNodeStruct
    };

    public static register(className: string, ctor: {new (data?: TFloatNodeStruct| TFloatNodeStructSerialized): TFloatNode}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: TFloatNodeStructSerialized}): TFloatNode {
        const polymorphicId = Object.keys(data)[0];
        const ctor = TFloatNodeStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for TFloatNodeStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(TFloatNodeStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in TFloatNodeStruct._knownPolymorphic;
    }
}

TFloatNodeStruct.register(TFloatNodeStruct.FullClassName, TFloatNodeStruct);
FloatNodeStruct.register(TFloatNodeStruct.FullClassName, TFloatNodeStruct);

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
Introspector.register('idltest.ast.TFloatNode', {
        full: 'idltest.ast.TFloatNode',
        short: 'TFloatNode',
        package: 'idltest.ast',
        type: IntrospectorTypes.Mixin,
        ctor: () => new TFloatNodeStruct(),
        fields: [

        ],
        implementations: TFloatNodeStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(TFloatNodeStruct.FullClassName, {
        full: TFloatNodeStruct.FullClassName,
        short: TFloatNodeStruct.ClassName,
        package: TFloatNodeStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new TFloatNodeStruct(),
        fields: [

        ]
    } as IIntrospectorDataObject
);