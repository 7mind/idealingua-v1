// Auto-generated, any modifications may be overwritten in the future.
import {
    BoolNode,
    BoolNodeStruct,
    BoolNodeStructSerialized
} from './BoolNode';
import {
    TypeInfoStruct,
    TypeInfoStructSerialized
} from './TypeInfo';
import {
    Type,
    TypeSerialized
} from './Type';

// TBoolNode Interface
export interface TBoolNode extends BoolNode {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): TBoolNodeStructSerialized;

    tpe: Type;
    lit: boolean;
}

export interface TBoolNodeStructSerialized extends BoolNodeStructSerialized {
    tpe: TypeSerialized;
    lit: boolean;
}

export class TBoolNodeStruct implements TBoolNode {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.ast.TBoolNode';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.ast.TBoolNode.Struct';

    public getPackageName(): string { return TBoolNodeStruct.PackageName; }
    public getClassName(): string { return TBoolNodeStruct.ClassName; }
    public getFullClassName(): string { return TBoolNodeStruct.FullClassName; }

    private _tpe: Type;
    private _lit: boolean;

    public get tpe(): Type {
        return this._tpe;
    }

    public set tpe(value: Type) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field tpe is not optional');
        }
        this._tpe = value;
    }

    public get lit(): boolean {
        return this._lit;
    }

    public set lit(value: boolean) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field lit is not optional');
        }

        if (typeof value !== 'boolean') {
            throw new Error('Field lit expects boolean type, got ' + value);
        }

        this._lit = value;
    }

    constructor(data: TBoolNodeStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.tpe = new Type(data.tpe);
        this.lit = data.lit;
    }

    public serialize(): TBoolNodeStructSerialized {
        return {
            tpe: this.tpe.serialize(),
            lit: this.lit
        };
    }

    // Polymorphic section below. If a new type to be registered, use TBoolNodeStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: TBoolNodeStruct| TBoolNodeStructSerialized): TBoolNode}} = {
        // This basic registration will happen below [TBoolNodeStruct.FullClassName]: TBoolNodeStruct
    };

    public static register(className: string, ctor: {new (data?: TBoolNodeStruct| TBoolNodeStructSerialized): TBoolNode}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: TBoolNodeStructSerialized}): TBoolNode {
        const polymorphicId = Object.keys(data)[0];
        const ctor = TBoolNodeStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for TBoolNodeStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(TBoolNodeStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in TBoolNodeStruct._knownPolymorphic;
    }
}

TBoolNodeStruct.register(TBoolNodeStruct.FullClassName, TBoolNodeStruct);
BoolNodeStruct.register(TBoolNodeStruct.FullClassName, TBoolNodeStruct);

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
Introspector.register('idltest.ast.TBoolNode', {
        full: 'idltest.ast.TBoolNode',
        short: 'TBoolNode',
        package: 'idltest.ast',
        type: IntrospectorTypes.Mixin,
        ctor: () => new TBoolNodeStruct(),
        fields: [

        ],
        implementations: TBoolNodeStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(TBoolNodeStruct.FullClassName, {
        full: TBoolNodeStruct.FullClassName,
        short: TBoolNodeStruct.ClassName,
        package: TBoolNodeStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new TBoolNodeStruct(),
        fields: [

        ]
    } as IIntrospectorDataObject
);