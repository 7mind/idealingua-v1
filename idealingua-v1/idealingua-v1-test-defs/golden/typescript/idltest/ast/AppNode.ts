// Auto-generated, any modifications may be overwritten in the future.
import {
    AST,
    ASTSerialized,
    ASTHelpers
} from './AST';

// AppNode Interface
export interface AppNode {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): AppNodeStructSerialized;

    fun: AST | undefined;
    args: AST[];
}

export interface AppNodeStructSerialized {
    fun: {[key: string]: any} | undefined;
    args: {[key: string]: any}[];
}

export class AppNodeStruct implements AppNode {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.ast.AppNode';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.ast.AppNode.Struct';

    public getPackageName(): string { return AppNodeStruct.PackageName; }
    public getClassName(): string { return AppNodeStruct.ClassName; }
    public getFullClassName(): string { return AppNodeStruct.FullClassName; }

    private _fun: AST | undefined;
    private _args: AST[];

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

    constructor(data: AppNodeStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            this.args = [];
            return;
        }

        this.fun = typeof data.fun !== 'undefined' ? ASTHelpers.deserialize(data.fun) : undefined;
        this.args = data.args.map(e => { return ASTHelpers.deserialize(e); });
    }

    public serialize(): AppNodeStructSerialized {
        return {
            fun: typeof this.fun !== 'undefined' ? ASTHelpers.serialize(this.fun) : undefined,
            args: this.args.map(e => { return ASTHelpers.serialize(e); })
        };
    }

    // Polymorphic section below. If a new type to be registered, use AppNodeStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: AppNodeStruct| AppNodeStructSerialized): AppNode}} = {
        // This basic registration will happen below [AppNodeStruct.FullClassName]: AppNodeStruct
    };

    public static register(className: string, ctor: {new (data?: AppNodeStruct| AppNodeStructSerialized): AppNode}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: AppNodeStructSerialized}): AppNode {
        const polymorphicId = Object.keys(data)[0];
        const ctor = AppNodeStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for AppNodeStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(AppNodeStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in AppNodeStruct._knownPolymorphic;
    }
}

AppNodeStruct.register(AppNodeStruct.FullClassName, AppNodeStruct);

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
Introspector.register('idltest.ast.AppNode', {
        full: 'idltest.ast.AppNode',
        short: 'AppNode',
        package: 'idltest.ast',
        type: IntrospectorTypes.Mixin,
        ctor: () => new AppNodeStruct(),
        fields: [
            {
                name: 'fun',
                accessName: 'fun',
                type: {intro: IntrospectorTypes.Opt, value: {intro: IntrospectorTypes.Adt, full: 'idltest.ast.AST'} as IIntrospectorUserType} as IIntrospectorGenericType
            },
            {
                name: 'args',
                accessName: 'args',
                type: {intro: IntrospectorTypes.List, value: {intro: IntrospectorTypes.Adt, full: 'idltest.ast.AST'} as IIntrospectorUserType} as IIntrospectorGenericType
            }
        ],
        implementations: AppNodeStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(AppNodeStruct.FullClassName, {
        full: AppNodeStruct.FullClassName,
        short: AppNodeStruct.ClassName,
        package: AppNodeStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new AppNodeStruct(),
        fields: [
            {
                name: 'fun',
                accessName: 'fun',
                type: {intro: IntrospectorTypes.Opt, value: {intro: IntrospectorTypes.Adt, full: 'idltest.ast.AST'} as IIntrospectorUserType} as IIntrospectorGenericType
            },
            {
                name: 'args',
                accessName: 'args',
                type: {intro: IntrospectorTypes.List, value: {intro: IntrospectorTypes.Adt, full: 'idltest.ast.AST'} as IIntrospectorUserType} as IIntrospectorGenericType
            }
        ]
    } as IIntrospectorDataObject
);