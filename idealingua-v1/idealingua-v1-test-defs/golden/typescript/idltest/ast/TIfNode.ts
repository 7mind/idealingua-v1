// Auto-generated, any modifications may be overwritten in the future.
import {
    IfNode,
    IfNodeStruct,
    IfNodeStructSerialized
} from './IfNode';
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

// TIfNode Interface
export interface TIfNode extends IfNode {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): TIfNodeStructSerialized;

    tpe: Type;
    cond: AST | undefined;
    thenNode: AST | undefined;
    elseNode: AST | undefined;
}

export interface TIfNodeStructSerialized extends IfNodeStructSerialized {
    tpe: TypeSerialized;
    cond: {[key: string]: any} | undefined;
    thenNode: {[key: string]: any} | undefined;
    elseNode: {[key: string]: any} | undefined;
}

export class TIfNodeStruct implements TIfNode {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.ast.TIfNode';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.ast.TIfNode.Struct';

    public getPackageName(): string { return TIfNodeStruct.PackageName; }
    public getClassName(): string { return TIfNodeStruct.ClassName; }
    public getFullClassName(): string { return TIfNodeStruct.FullClassName; }

    private _tpe: Type;
    private _cond: AST | undefined;
    private _thenNode: AST | undefined;
    private _elseNode: AST | undefined;

    public get tpe(): Type {
        return this._tpe;
    }

    public set tpe(value: Type) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field tpe is not optional');
        }
        this._tpe = value;
    }

    public get cond(): AST | undefined {
        return this._cond;
    }

    public set cond(value: AST | undefined) {
        if (typeof value === 'undefined' || value === null) {
            this._cond = undefined;
            return;
        }
        this._cond = value;
    }

    public get thenNode(): AST | undefined {
        return this._thenNode;
    }

    public set thenNode(value: AST | undefined) {
        if (typeof value === 'undefined' || value === null) {
            this._thenNode = undefined;
            return;
        }
        this._thenNode = value;
    }

    public get elseNode(): AST | undefined {
        return this._elseNode;
    }

    public set elseNode(value: AST | undefined) {
        if (typeof value === 'undefined' || value === null) {
            this._elseNode = undefined;
            return;
        }
        this._elseNode = value;
    }

    constructor(data: TIfNodeStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.tpe = new Type(data.tpe);
        this.cond = typeof data.cond !== 'undefined' ? ASTHelpers.deserialize(data.cond) : undefined;
        this.thenNode = typeof data.thenNode !== 'undefined' ? ASTHelpers.deserialize(data.thenNode) : undefined;
        this.elseNode = typeof data.elseNode !== 'undefined' ? ASTHelpers.deserialize(data.elseNode) : undefined;
    }

    public serialize(): TIfNodeStructSerialized {
        return {
            tpe: this.tpe.serialize(),
            cond: typeof this.cond !== 'undefined' ? ASTHelpers.serialize(this.cond) : undefined,
            thenNode: typeof this.thenNode !== 'undefined' ? ASTHelpers.serialize(this.thenNode) : undefined,
            elseNode: typeof this.elseNode !== 'undefined' ? ASTHelpers.serialize(this.elseNode) : undefined
        };
    }

    // Polymorphic section below. If a new type to be registered, use TIfNodeStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: TIfNodeStruct| TIfNodeStructSerialized): TIfNode}} = {
        // This basic registration will happen below [TIfNodeStruct.FullClassName]: TIfNodeStruct
    };

    public static register(className: string, ctor: {new (data?: TIfNodeStruct| TIfNodeStructSerialized): TIfNode}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: TIfNodeStructSerialized}): TIfNode {
        const polymorphicId = Object.keys(data)[0];
        const ctor = TIfNodeStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for TIfNodeStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(TIfNodeStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in TIfNodeStruct._knownPolymorphic;
    }
}

TIfNodeStruct.register(TIfNodeStruct.FullClassName, TIfNodeStruct);
IfNodeStruct.register(TIfNodeStruct.FullClassName, TIfNodeStruct);

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
Introspector.register('idltest.ast.TIfNode', {
        full: 'idltest.ast.TIfNode',
        short: 'TIfNode',
        package: 'idltest.ast',
        type: IntrospectorTypes.Mixin,
        ctor: () => new TIfNodeStruct(),
        fields: [

        ],
        implementations: TIfNodeStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(TIfNodeStruct.FullClassName, {
        full: TIfNodeStruct.FullClassName,
        short: TIfNodeStruct.ClassName,
        package: TIfNodeStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new TIfNodeStruct(),
        fields: [

        ]
    } as IIntrospectorDataObject
);