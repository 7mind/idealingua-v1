// Auto-generated, any modifications may be overwritten in the future.
import {
    AST,
    ASTSerialized,
    ASTHelpers
} from './AST';

// IfNode Interface
export interface IfNode {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): IfNodeStructSerialized;

    cond: AST | undefined;
    thenNode: AST | undefined;
    elseNode: AST | undefined;
}

export interface IfNodeStructSerialized {
    cond: {[key: string]: any} | undefined;
    thenNode: {[key: string]: any} | undefined;
    elseNode: {[key: string]: any} | undefined;
}

export class IfNodeStruct implements IfNode {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.ast.IfNode';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.ast.IfNode.Struct';

    public getPackageName(): string { return IfNodeStruct.PackageName; }
    public getClassName(): string { return IfNodeStruct.ClassName; }
    public getFullClassName(): string { return IfNodeStruct.FullClassName; }

    private _cond: AST | undefined;
    private _thenNode: AST | undefined;
    private _elseNode: AST | undefined;

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

    constructor(data: IfNodeStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.cond = typeof data.cond !== 'undefined' ? ASTHelpers.deserialize(data.cond) : undefined;
        this.thenNode = typeof data.thenNode !== 'undefined' ? ASTHelpers.deserialize(data.thenNode) : undefined;
        this.elseNode = typeof data.elseNode !== 'undefined' ? ASTHelpers.deserialize(data.elseNode) : undefined;
    }

    public serialize(): IfNodeStructSerialized {
        return {
            cond: typeof this.cond !== 'undefined' ? ASTHelpers.serialize(this.cond) : undefined,
            thenNode: typeof this.thenNode !== 'undefined' ? ASTHelpers.serialize(this.thenNode) : undefined,
            elseNode: typeof this.elseNode !== 'undefined' ? ASTHelpers.serialize(this.elseNode) : undefined
        };
    }

    // Polymorphic section below. If a new type to be registered, use IfNodeStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: IfNodeStruct| IfNodeStructSerialized): IfNode}} = {
        // This basic registration will happen below [IfNodeStruct.FullClassName]: IfNodeStruct
    };

    public static register(className: string, ctor: {new (data?: IfNodeStruct| IfNodeStructSerialized): IfNode}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: IfNodeStructSerialized}): IfNode {
        const polymorphicId = Object.keys(data)[0];
        const ctor = IfNodeStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for IfNodeStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(IfNodeStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in IfNodeStruct._knownPolymorphic;
    }
}

IfNodeStruct.register(IfNodeStruct.FullClassName, IfNodeStruct);

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
Introspector.register('idltest.ast.IfNode', {
        full: 'idltest.ast.IfNode',
        short: 'IfNode',
        package: 'idltest.ast',
        type: IntrospectorTypes.Mixin,
        ctor: () => new IfNodeStruct(),
        fields: [
            {
                name: 'cond',
                accessName: 'cond',
                type: {intro: IntrospectorTypes.Opt, value: {intro: IntrospectorTypes.Adt, full: 'idltest.ast.AST'} as IIntrospectorUserType} as IIntrospectorGenericType
            },
            {
                name: 'thenNode',
                accessName: 'thenNode',
                type: {intro: IntrospectorTypes.Opt, value: {intro: IntrospectorTypes.Adt, full: 'idltest.ast.AST'} as IIntrospectorUserType} as IIntrospectorGenericType
            },
            {
                name: 'elseNode',
                accessName: 'elseNode',
                type: {intro: IntrospectorTypes.Opt, value: {intro: IntrospectorTypes.Adt, full: 'idltest.ast.AST'} as IIntrospectorUserType} as IIntrospectorGenericType
            }
        ],
        implementations: IfNodeStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(IfNodeStruct.FullClassName, {
        full: IfNodeStruct.FullClassName,
        short: IfNodeStruct.ClassName,
        package: IfNodeStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new IfNodeStruct(),
        fields: [
            {
                name: 'cond',
                accessName: 'cond',
                type: {intro: IntrospectorTypes.Opt, value: {intro: IntrospectorTypes.Adt, full: 'idltest.ast.AST'} as IIntrospectorUserType} as IIntrospectorGenericType
            },
            {
                name: 'thenNode',
                accessName: 'thenNode',
                type: {intro: IntrospectorTypes.Opt, value: {intro: IntrospectorTypes.Adt, full: 'idltest.ast.AST'} as IIntrospectorUserType} as IIntrospectorGenericType
            },
            {
                name: 'elseNode',
                accessName: 'elseNode',
                type: {intro: IntrospectorTypes.Opt, value: {intro: IntrospectorTypes.Adt, full: 'idltest.ast.AST'} as IIntrospectorUserType} as IIntrospectorGenericType
            }
        ]
    } as IIntrospectorDataObject
);