// Auto-generated, any modifications may be overwritten in the future.
import {
    AST,
    ASTSerialized,
    ASTHelpers
} from './AST';
import {
    LamNode,
    LamNodeStruct,
    LamNodeStructSerialized
} from './LamNode';
import {
    TypeInfoStruct,
    TypeInfoStructSerialized
} from './TypeInfo';
import {
    Type,
    TypeSerialized
} from './Type';

// TLamNode Interface
export interface TLamNode extends LamNode {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): TLamNodeStructSerialized;

    tpe: Type;
    paramNames: string[];
    body: AST | undefined;
}

export interface TLamNodeStructSerialized extends LamNodeStructSerialized {
    tpe: TypeSerialized;
    paramNames: string[];
    body: {[key: string]: any} | undefined;
}

export class TLamNodeStruct implements TLamNode {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.ast.TLamNode';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.ast.TLamNode.Struct';

    public getPackageName(): string { return TLamNodeStruct.PackageName; }
    public getClassName(): string { return TLamNodeStruct.ClassName; }
    public getFullClassName(): string { return TLamNodeStruct.FullClassName; }

    private _tpe: Type;
    private _paramNames: string[];
    private _body: AST | undefined;

    public get tpe(): Type {
        return this._tpe;
    }

    public set tpe(value: Type) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field tpe is not optional');
        }
        this._tpe = value;
    }

    public get paramNames(): string[] {
        return this._paramNames;
    }

    public set paramNames(value: string[]) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field paramNames is not optional');
        }
        this._paramNames = value;
    }

    public get body(): AST | undefined {
        return this._body;
    }

    public set body(value: AST | undefined) {
        if (typeof value === 'undefined' || value === null) {
            this._body = undefined;
            return;
        }
        this._body = value;
    }

    constructor(data: TLamNodeStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            this.paramNames = [];
            return;
        }

        this.tpe = new Type(data.tpe);
        this.paramNames = data.paramNames.slice();
        this.body = typeof data.body !== 'undefined' ? ASTHelpers.deserialize(data.body) : undefined;
    }

    public serialize(): TLamNodeStructSerialized {
        return {
            tpe: this.tpe.serialize(),
            paramNames: this.paramNames.slice(),
            body: typeof this.body !== 'undefined' ? ASTHelpers.serialize(this.body) : undefined
        };
    }

    // Polymorphic section below. If a new type to be registered, use TLamNodeStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: TLamNodeStruct| TLamNodeStructSerialized): TLamNode}} = {
        // This basic registration will happen below [TLamNodeStruct.FullClassName]: TLamNodeStruct
    };

    public static register(className: string, ctor: {new (data?: TLamNodeStruct| TLamNodeStructSerialized): TLamNode}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: TLamNodeStructSerialized}): TLamNode {
        const polymorphicId = Object.keys(data)[0];
        const ctor = TLamNodeStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for TLamNodeStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(TLamNodeStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in TLamNodeStruct._knownPolymorphic;
    }
}

TLamNodeStruct.register(TLamNodeStruct.FullClassName, TLamNodeStruct);
LamNodeStruct.register(TLamNodeStruct.FullClassName, TLamNodeStruct);

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
Introspector.register('idltest.ast.TLamNode', {
        full: 'idltest.ast.TLamNode',
        short: 'TLamNode',
        package: 'idltest.ast',
        type: IntrospectorTypes.Mixin,
        ctor: () => new TLamNodeStruct(),
        fields: [

        ],
        implementations: TLamNodeStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(TLamNodeStruct.FullClassName, {
        full: TLamNodeStruct.FullClassName,
        short: TLamNodeStruct.ClassName,
        package: TLamNodeStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new TLamNodeStruct(),
        fields: [

        ]
    } as IIntrospectorDataObject
);