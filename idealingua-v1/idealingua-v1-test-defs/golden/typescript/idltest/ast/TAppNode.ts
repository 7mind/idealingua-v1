// Auto-generated, any modifications may be overwritten in the future.
import {
    AppNode,
    AppNodeStruct,
    AppNodeStructSerialized
} from './AppNode';
import {
    AST,
    ASTSerialized,
    ASTHelpers
} from './AST';
import {
    TypeInfoStruct,
    TypeInfoStructSerialized
} from './TypeInfo';
import {
    Type,
    TypeSerialized
} from './Type';

// TAppNode Interface
export interface TAppNode extends AppNode {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): TAppNodeStructSerialized;

    tpe: Type;
    fun: AST | undefined;
    args: AST[];
}

export interface TAppNodeStructSerialized extends AppNodeStructSerialized {
    tpe: TypeSerialized;
    fun: {[key: string]: any} | undefined;
    args: {[key: string]: any}[];
}

export class TAppNodeStruct implements TAppNode {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.ast.TAppNode';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.ast.TAppNode.Struct';

    public getPackageName(): string { return TAppNodeStruct.PackageName; }
    public getClassName(): string { return TAppNodeStruct.ClassName; }
    public getFullClassName(): string { return TAppNodeStruct.FullClassName; }

    private _tpe: Type;
    private _fun: AST | undefined;
    private _args: AST[];

    public get tpe(): Type {
        return this._tpe;
    }

    public set tpe(value: Type) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field tpe is not optional');
        }
        this._tpe = value;
    }

    public get fun(): AST | undefined {
        return this._fun;
    }

    public set fun(value: AST | undefined) {
        if (typeof value === 'undefined' || value === null) {
            this._fun = undefined;
            return;
        }
        this._fun = value;
    }

    public get args(): AST[] {
        return this._args;
    }

    public set args(value: AST[]) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field args is not optional');
        }
        this._args = value;
    }

    constructor(data: TAppNodeStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            this.args = [];
            return;
        }

        this.tpe = new Type(data.tpe);
        this.fun = typeof data.fun !== 'undefined' ? ASTHelpers.deserialize(data.fun) : undefined;
        this.args = data.args.map(e => { return ASTHelpers.deserialize(e); });
    }

    public serialize(): TAppNodeStructSerialized {
        return {
            tpe: this.tpe.serialize(),
            fun: typeof this.fun !== 'undefined' ? ASTHelpers.serialize(this.fun) : undefined,
            args: this.args.map(e => { return ASTHelpers.serialize(e); })
        };
    }

    // Polymorphic section below. If a new type to be registered, use TAppNodeStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: TAppNodeStruct| TAppNodeStructSerialized): TAppNode}} = {
        // This basic registration will happen below [TAppNodeStruct.FullClassName]: TAppNodeStruct
    };

    public static register(className: string, ctor: {new (data?: TAppNodeStruct| TAppNodeStructSerialized): TAppNode}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: TAppNodeStructSerialized}): TAppNode {
        const polymorphicId = Object.keys(data)[0];
        const ctor = TAppNodeStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for TAppNodeStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(TAppNodeStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in TAppNodeStruct._knownPolymorphic;
    }
}

TAppNodeStruct.register(TAppNodeStruct.FullClassName, TAppNodeStruct);
AppNodeStruct.register(TAppNodeStruct.FullClassName, TAppNodeStruct);

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
Introspector.register('idltest.ast.TAppNode', {
        full: 'idltest.ast.TAppNode',
        short: 'TAppNode',
        package: 'idltest.ast',
        type: IntrospectorTypes.Mixin,
        ctor: () => new TAppNodeStruct(),
        fields: [

        ],
        implementations: TAppNodeStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(TAppNodeStruct.FullClassName, {
        full: TAppNodeStruct.FullClassName,
        short: TAppNodeStruct.ClassName,
        package: TAppNodeStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new TAppNodeStruct(),
        fields: [

        ]
    } as IIntrospectorDataObject
);