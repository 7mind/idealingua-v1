// Auto-generated, any modifications may be overwritten in the future.
import {
    TSymNode,
    TSymNodeStruct,
    TSymNodeStructSerialized
} from './TSymNode';
import {
    TFloatNode,
    TFloatNodeStruct,
    TFloatNodeStructSerialized
} from './TFloatNode';
import {
    TAppNode,
    TAppNodeStruct,
    TAppNodeStructSerialized
} from './TAppNode';
import {
    TBoolNode,
    TBoolNodeStruct,
    TBoolNodeStructSerialized
} from './TBoolNode';
import {
    TIntNode,
    TIntNodeStruct,
    TIntNodeStructSerialized
} from './TIntNode';
import {
    TLamNode,
    TLamNodeStruct,
    TLamNodeStructSerialized
} from './TLamNode';
import {
    TIfNode,
    TIfNodeStruct,
    TIfNodeStructSerialized
} from './TIfNode';

// TypedAst Algebraic Data Type
export type TypedAst = TIntNode | TFloatNode | TBoolNode | TSymNode | TAppNode | TLamNode | TIfNode;
export type TypedAstSerialized = {[key: string]: TIntNodeStructSerialized} | {[key: string]: TFloatNodeStructSerialized} | {[key: string]: TBoolNodeStructSerialized} | {[key: string]: TSymNodeStructSerialized} | {[key: string]: TAppNodeStructSerialized} | {[key: string]: TLamNodeStructSerialized} | {[key: string]: TIfNodeStructSerialized}

export class TypedAstHelpers {
    public static isInstanceOf(o: any): boolean {
        if (!o['getClassName'] || typeof o['getClassName'] !== 'function') {
            return false;
        }
        const fullClassName = o.getFullClassName();
        return TIntNodeStruct.isRegisteredType(fullClassName) || TFloatNodeStruct.isRegisteredType(fullClassName) || TBoolNodeStruct.isRegisteredType(fullClassName) || TSymNodeStruct.isRegisteredType(fullClassName) || TAppNodeStruct.isRegisteredType(fullClassName) || TLamNodeStruct.isRegisteredType(fullClassName) || TIfNodeStruct.isRegisteredType(fullClassName);
    }

    public static serialize(adt: TypedAst): {[key: string]: TIntNodeStructSerialized | TFloatNodeStructSerialized | TBoolNodeStructSerialized | TSymNodeStructSerialized | TAppNodeStructSerialized | TLamNodeStructSerialized | TIfNodeStructSerialized} {
        let className = adt.getClassName();
        const fullClassName = adt.getFullClassName();
        let serialized: any = undefined;

        if (TIntNodeStruct.isRegisteredType(fullClassName)) {
            className = 'TIntNode'; serialized = {[fullClassName]: adt.serialize()};
        } else 
        if (TFloatNodeStruct.isRegisteredType(fullClassName)) {
            className = 'TFloatNode'; serialized = {[fullClassName]: adt.serialize()};
        } else 
        if (TBoolNodeStruct.isRegisteredType(fullClassName)) {
            className = 'TBoolNode'; serialized = {[fullClassName]: adt.serialize()};
        } else 
        if (TSymNodeStruct.isRegisteredType(fullClassName)) {
            className = 'TSymNode'; serialized = {[fullClassName]: adt.serialize()};
        } else 
        if (TAppNodeStruct.isRegisteredType(fullClassName)) {
            className = 'TAppNode'; serialized = {[fullClassName]: adt.serialize()};
        } else 
        if (TLamNodeStruct.isRegisteredType(fullClassName)) {
            className = 'TLamNode'; serialized = {[fullClassName]: adt.serialize()};
        } else 
        if (TIfNodeStruct.isRegisteredType(fullClassName)) {
            className = 'TIfNode'; serialized = {[fullClassName]: adt.serialize()};
        }

        return {
            [className]: serialized || adt.serialize()
        };
    }

    public static deserialize(data: {[key: string]: TIntNodeStructSerialized | TFloatNodeStructSerialized | TBoolNodeStructSerialized | TSymNodeStructSerialized | TAppNodeStructSerialized | TLamNodeStructSerialized | TIfNodeStructSerialized}): TypedAst {
        const id = Object.keys(data)[0];
        const content = (data as any)[id];
        switch (id) {
            case 'TIntNode': return TIntNodeStruct.create(content as any);
            case 'TFloatNode': return TFloatNodeStruct.create(content as any);
            case 'TBoolNode': return TBoolNodeStruct.create(content as any);
            case 'TSymNode': return TSymNodeStruct.create(content as any);
            case 'TAppNode': return TAppNodeStruct.create(content as any);
            case 'TLamNode': return TLamNodeStruct.create(content as any);
            case 'TIfNode': return TIfNodeStruct.create(content as any);
            default:
                throw new Error('Unknown type id ' + id + ' for TypedAst');
        }
    }
}

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorAdtObject
} from '../../irt';
Introspector.register('idltest.ast.TypedAst', {
        full: 'idltest.ast.TypedAst',
        short: 'TypedAst',
        package: 'idltest.ast',
        type: IntrospectorTypes.Adt,
        options: [
            {
                name: 'TIntNode',
                type: {intro: IntrospectorTypes.Mixin, full: 'idltest.ast.TIntNode'} as IIntrospectorUserType
            },
            {
                name: 'TFloatNode',
                type: {intro: IntrospectorTypes.Mixin, full: 'idltest.ast.TFloatNode'} as IIntrospectorUserType
            },
            {
                name: 'TBoolNode',
                type: {intro: IntrospectorTypes.Mixin, full: 'idltest.ast.TBoolNode'} as IIntrospectorUserType
            },
            {
                name: 'TSymNode',
                type: {intro: IntrospectorTypes.Mixin, full: 'idltest.ast.TSymNode'} as IIntrospectorUserType
            },
            {
                name: 'TAppNode',
                type: {intro: IntrospectorTypes.Mixin, full: 'idltest.ast.TAppNode'} as IIntrospectorUserType
            },
            {
                name: 'TLamNode',
                type: {intro: IntrospectorTypes.Mixin, full: 'idltest.ast.TLamNode'} as IIntrospectorUserType
            },
            {
                name: 'TIfNode',
                type: {intro: IntrospectorTypes.Mixin, full: 'idltest.ast.TIfNode'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorAdtObject
);