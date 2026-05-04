// Auto-generated, any modifications may be overwritten in the future.
import {
    AST,
    ASTSerialized,
    ASTHelpers
} from './AST';

// LamNode Interface
export interface LamNode {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): LamNodeStructSerialized;

    paramNames: string[];
    body: AST | undefined;
}

export interface LamNodeStructSerialized {
    paramNames: string[];
    body: {[key: string]: any} | undefined;
}

export class LamNodeStruct implements LamNode {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.ast.LamNode';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.ast.LamNode.Struct';

    public getPackageName(): string { return LamNodeStruct.PackageName; }
    public getClassName(): string { return LamNodeStruct.ClassName; }
    public getFullClassName(): string { return LamNodeStruct.FullClassName; }

    private _paramNames: string[];
    private _body: AST | undefined;

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

    constructor(data: LamNodeStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            this.paramNames = [];
            return;
        }

        this.paramNames = data.paramNames.slice();
        this.body = typeof data.body !== 'undefined' ? ASTHelpers.deserialize(data.body) : undefined;
    }

    public serialize(): LamNodeStructSerialized {
        return {
            paramNames: this.paramNames.slice(),
            body: typeof this.body !== 'undefined' ? ASTHelpers.serialize(this.body) : undefined
        };
    }

    // Polymorphic section below. If a new type to be registered, use LamNodeStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: LamNodeStruct| LamNodeStructSerialized): LamNode}} = {
        // This basic registration will happen below [LamNodeStruct.FullClassName]: LamNodeStruct
    };

    public static register(className: string, ctor: {new (data?: LamNodeStruct| LamNodeStructSerialized): LamNode}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: LamNodeStructSerialized}): LamNode {
        const polymorphicId = Object.keys(data)[0];
        const ctor = LamNodeStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for LamNodeStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(LamNodeStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in LamNodeStruct._knownPolymorphic;
    }
}

LamNodeStruct.register(LamNodeStruct.FullClassName, LamNodeStruct);

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
Introspector.register('idltest.ast.LamNode', {
        full: 'idltest.ast.LamNode',
        short: 'LamNode',
        package: 'idltest.ast',
        type: IntrospectorTypes.Mixin,
        ctor: () => new LamNodeStruct(),
        fields: [
            {
                name: 'paramNames',
                accessName: 'paramNames',
                type: {intro: IntrospectorTypes.List, value: {intro: IntrospectorTypes.Str}} as IIntrospectorGenericType
            },
            {
                name: 'body',
                accessName: 'body',
                type: {intro: IntrospectorTypes.Opt, value: {intro: IntrospectorTypes.Adt, full: 'idltest.ast.AST'} as IIntrospectorUserType} as IIntrospectorGenericType
            }
        ],
        implementations: LamNodeStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(LamNodeStruct.FullClassName, {
        full: LamNodeStruct.FullClassName,
        short: LamNodeStruct.ClassName,
        package: LamNodeStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new LamNodeStruct(),
        fields: [
            {
                name: 'paramNames',
                accessName: 'paramNames',
                type: {intro: IntrospectorTypes.List, value: {intro: IntrospectorTypes.Str}} as IIntrospectorGenericType
            },
            {
                name: 'body',
                accessName: 'body',
                type: {intro: IntrospectorTypes.Opt, value: {intro: IntrospectorTypes.Adt, full: 'idltest.ast.AST'} as IIntrospectorUserType} as IIntrospectorGenericType
            }
        ]
    } as IIntrospectorDataObject
);