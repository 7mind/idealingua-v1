// Auto-generated, any modifications may be overwritten in the future.
import {
    IntNode,
    IntNodeStruct,
    IntNodeStructSerialized
} from './IntNode';
import {
    TypeInfoStruct,
    TypeInfoStructSerialized
} from './TypeInfo';
import {
    Type,
    TypeSerialized
} from './Type';

// TIntNode Interface
export interface TIntNode extends IntNode {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): TIntNodeStructSerialized;

    tpe: Type;
    lit: number;
}

export interface TIntNodeStructSerialized extends IntNodeStructSerialized {
    tpe: TypeSerialized;
    lit: number;
}

export class TIntNodeStruct implements TIntNode {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.ast.TIntNode';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.ast.TIntNode.Struct';

    public getPackageName(): string { return TIntNodeStruct.PackageName; }
    public getClassName(): string { return TIntNodeStruct.ClassName; }
    public getFullClassName(): string { return TIntNodeStruct.FullClassName; }

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
        return this._lit;
    }

    public set lit(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field lit is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field lit expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field lit is expected to be an integer, got ' + value);
        }

        this._lit = value;
    }

    constructor(data: TIntNodeStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.tpe = new Type(data.tpe);
        this.lit = data.lit;
    }

    public serialize(): TIntNodeStructSerialized {
        return {
            tpe: this.tpe.serialize(),
            lit: this.lit
        };
    }

    // Polymorphic section below. If a new type to be registered, use TIntNodeStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: TIntNodeStruct| TIntNodeStructSerialized): TIntNode}} = {
        // This basic registration will happen below [TIntNodeStruct.FullClassName]: TIntNodeStruct
    };

    public static register(className: string, ctor: {new (data?: TIntNodeStruct| TIntNodeStructSerialized): TIntNode}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: TIntNodeStructSerialized}): TIntNode {
        const polymorphicId = Object.keys(data)[0];
        const ctor = TIntNodeStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for TIntNodeStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(TIntNodeStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in TIntNodeStruct._knownPolymorphic;
    }
}

TIntNodeStruct.register(TIntNodeStruct.FullClassName, TIntNodeStruct);
IntNodeStruct.register(TIntNodeStruct.FullClassName, TIntNodeStruct);

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
Introspector.register('idltest.ast.TIntNode', {
        full: 'idltest.ast.TIntNode',
        short: 'TIntNode',
        package: 'idltest.ast',
        type: IntrospectorTypes.Mixin,
        ctor: () => new TIntNodeStruct(),
        fields: [

        ],
        implementations: TIntNodeStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(TIntNodeStruct.FullClassName, {
        full: TIntNodeStruct.FullClassName,
        short: TIntNodeStruct.ClassName,
        package: TIntNodeStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new TIntNodeStruct(),
        fields: [

        ]
    } as IIntrospectorDataObject
);