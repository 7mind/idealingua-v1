// Auto-generated, any modifications may be overwritten in the future.
import {
    TypeInfoStruct,
    TypeInfoStructSerialized
} from './TypeInfo';
import {
    SymNode,
    SymNodeStruct,
    SymNodeStructSerialized
} from './SymNode';
import {
    Type,
    TypeSerialized
} from './Type';

// TSymNode Interface
export interface TSymNode extends SymNode {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): TSymNodeStructSerialized;

    tpe: Type;
    lit: string;
}

export interface TSymNodeStructSerialized extends SymNodeStructSerialized {
    tpe: TypeSerialized;
    lit: string;
}

export class TSymNodeStruct implements TSymNode {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.ast.TSymNode';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.ast.TSymNode.Struct';

    public getPackageName(): string { return TSymNodeStruct.PackageName; }
    public getClassName(): string { return TSymNodeStruct.ClassName; }
    public getFullClassName(): string { return TSymNodeStruct.FullClassName; }

    private _tpe: Type;
    private _lit: string;

    public get tpe(): Type {
        return this._tpe;
    }

    public set tpe(value: Type) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field tpe is not optional');
        }
        this._tpe = value;
    }

    public get lit(): string {
        return this._lit;
    }

    public set lit(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field lit is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field lit expects type string, got ' + value);
        }

        this._lit = value;
    }

    constructor(data: TSymNodeStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.tpe = new Type(data.tpe);
        this.lit = data.lit;
    }

    public serialize(): TSymNodeStructSerialized {
        return {
            tpe: this.tpe.serialize(),
            lit: this.lit
        };
    }

    // Polymorphic section below. If a new type to be registered, use TSymNodeStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: TSymNodeStruct| TSymNodeStructSerialized): TSymNode}} = {
        // This basic registration will happen below [TSymNodeStruct.FullClassName]: TSymNodeStruct
    };

    public static register(className: string, ctor: {new (data?: TSymNodeStruct| TSymNodeStructSerialized): TSymNode}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: TSymNodeStructSerialized}): TSymNode {
        const polymorphicId = Object.keys(data)[0];
        const ctor = TSymNodeStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for TSymNodeStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(TSymNodeStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in TSymNodeStruct._knownPolymorphic;
    }
}

TSymNodeStruct.register(TSymNodeStruct.FullClassName, TSymNodeStruct);
SymNodeStruct.register(TSymNodeStruct.FullClassName, TSymNodeStruct);

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
Introspector.register('idltest.ast.TSymNode', {
        full: 'idltest.ast.TSymNode',
        short: 'TSymNode',
        package: 'idltest.ast',
        type: IntrospectorTypes.Mixin,
        ctor: () => new TSymNodeStruct(),
        fields: [

        ],
        implementations: TSymNodeStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(TSymNodeStruct.FullClassName, {
        full: TSymNodeStruct.FullClassName,
        short: TSymNodeStruct.ClassName,
        package: TSymNodeStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new TSymNodeStruct(),
        fields: [

        ]
    } as IIntrospectorDataObject
);