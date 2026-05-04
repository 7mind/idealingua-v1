// Auto-generated, any modifications may be overwritten in the future.
import {
    IntNode,
    IntNodeStruct,
    IntNodeStructSerialized
} from './IntNode';
import {
    IfNode,
    IfNodeStruct,
    IfNodeStructSerialized
} from './IfNode';
import {
    LamNode,
    LamNodeStruct,
    LamNodeStructSerialized
} from './LamNode';
import {
    BoolNode,
    BoolNodeStruct,
    BoolNodeStructSerialized
} from './BoolNode';
import {
    SymNode,
    SymNodeStruct,
    SymNodeStructSerialized
} from './SymNode';
import {
    FloatNode,
    FloatNodeStruct,
    FloatNodeStructSerialized
} from './FloatNode';
import {
    AppNode,
    AppNodeStruct,
    AppNodeStructSerialized
} from './AppNode';

// AST Algebraic Data Type
export type AST = IntNode | FloatNode | BoolNode | SymNode | AppNode | LamNode | IfNode;
export type ASTSerialized = {[key: string]: IntNodeStructSerialized} | {[key: string]: FloatNodeStructSerialized} | {[key: string]: BoolNodeStructSerialized} | {[key: string]: SymNodeStructSerialized} | {[key: string]: AppNodeStructSerialized} | {[key: string]: LamNodeStructSerialized} | {[key: string]: IfNodeStructSerialized}

export class ASTHelpers {
    public static isInstanceOf(o: any): boolean {
        if (!o['getClassName'] || typeof o['getClassName'] !== 'function') {
            return false;
        }
        const fullClassName = o.getFullClassName();
        return IntNodeStruct.isRegisteredType(fullClassName) || FloatNodeStruct.isRegisteredType(fullClassName) || BoolNodeStruct.isRegisteredType(fullClassName) || SymNodeStruct.isRegisteredType(fullClassName) || AppNodeStruct.isRegisteredType(fullClassName) || LamNodeStruct.isRegisteredType(fullClassName) || IfNodeStruct.isRegisteredType(fullClassName);
    }

    public static serialize(adt: AST): {[key: string]: IntNodeStructSerialized | FloatNodeStructSerialized | BoolNodeStructSerialized | SymNodeStructSerialized | AppNodeStructSerialized | LamNodeStructSerialized | IfNodeStructSerialized} {
        let className = adt.getClassName();
        const fullClassName = adt.getFullClassName();
        let serialized: any = undefined;

        if (IntNodeStruct.isRegisteredType(fullClassName)) {
            className = 'IntNode'; serialized = {[fullClassName]: adt.serialize()};
        } else 
        if (FloatNodeStruct.isRegisteredType(fullClassName)) {
            className = 'FloatNode'; serialized = {[fullClassName]: adt.serialize()};
        } else 
        if (BoolNodeStruct.isRegisteredType(fullClassName)) {
            className = 'BoolNode'; serialized = {[fullClassName]: adt.serialize()};
        } else 
        if (SymNodeStruct.isRegisteredType(fullClassName)) {
            className = 'SymNode'; serialized = {[fullClassName]: adt.serialize()};
        } else 
        if (AppNodeStruct.isRegisteredType(fullClassName)) {
            className = 'AppNode'; serialized = {[fullClassName]: adt.serialize()};
        } else 
        if (LamNodeStruct.isRegisteredType(fullClassName)) {
            className = 'LamNode'; serialized = {[fullClassName]: adt.serialize()};
        } else 
        if (IfNodeStruct.isRegisteredType(fullClassName)) {
            className = 'IfNode'; serialized = {[fullClassName]: adt.serialize()};
        }
        if (className == 'FloatNode') {
            className = 'FloatRenamed'
        }
        return {
            [className]: serialized || adt.serialize()
        };
    }

    public static deserialize(data: {[key: string]: IntNodeStructSerialized | FloatNodeStructSerialized | BoolNodeStructSerialized | SymNodeStructSerialized | AppNodeStructSerialized | LamNodeStructSerialized | IfNodeStructSerialized}): AST {
        const id = Object.keys(data)[0];
        const content = (data as any)[id];
        switch (id) {
            case 'IntNode': return IntNodeStruct.create(content as any);
            case 'FloatRenamed': return FloatNodeStruct.create(content as any);
            case 'BoolNode': return BoolNodeStruct.create(content as any);
            case 'SymNode': return SymNodeStruct.create(content as any);
            case 'AppNode': return AppNodeStruct.create(content as any);
            case 'LamNode': return LamNodeStruct.create(content as any);
            case 'IfNode': return IfNodeStruct.create(content as any);
            default:
                throw new Error('Unknown type id ' + id + ' for AST');
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
Introspector.register('idltest.ast.AST', {
        full: 'idltest.ast.AST',
        short: 'AST',
        package: 'idltest.ast',
        type: IntrospectorTypes.Adt,
        options: [
            {
                name: 'IntNode',
                type: {intro: IntrospectorTypes.Mixin, full: 'idltest.ast.IntNode'} as IIntrospectorUserType
            },
            {
                name: 'FloatRenamed',
                type: {intro: IntrospectorTypes.Mixin, full: 'idltest.ast.FloatNode'} as IIntrospectorUserType
            },
            {
                name: 'BoolNode',
                type: {intro: IntrospectorTypes.Mixin, full: 'idltest.ast.BoolNode'} as IIntrospectorUserType
            },
            {
                name: 'SymNode',
                type: {intro: IntrospectorTypes.Mixin, full: 'idltest.ast.SymNode'} as IIntrospectorUserType
            },
            {
                name: 'AppNode',
                type: {intro: IntrospectorTypes.Mixin, full: 'idltest.ast.AppNode'} as IIntrospectorUserType
            },
            {
                name: 'LamNode',
                type: {intro: IntrospectorTypes.Mixin, full: 'idltest.ast.LamNode'} as IIntrospectorUserType
            },
            {
                name: 'IfNode',
                type: {intro: IntrospectorTypes.Mixin, full: 'idltest.ast.IfNode'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorAdtObject
);