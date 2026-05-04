// Auto-generated, any modifications may be overwritten in the future.
import {
    JLBool,
    JLBoolSerialized
} from './JLBool';
import {
    JLArray,
    JLArraySerialized
} from './JLArray';
import {
    JLString,
    JLStringSerialized
} from './JLString';
import {
    JLNumber,
    JLNumberSerialized
} from './JLNumber';
import {
    JLObject,
    JLObjectSerialized
} from './JLObject';
import {
    JLNull,
    JLNullSerialized
} from './JLNull';

// JSONLike Algebraic Data Type
export type JSONLike = JLObject | JLArray | JLString | JLNumber | JLBool | JLNull;
export type JSONLikeSerialized = JLObjectSerialized | JLArraySerialized | JLStringSerialized | JLNumberSerialized | JLBoolSerialized | JLNullSerialized

export class JSONLikeHelpers {
    public static isInstanceOf(o: any): boolean {
        if (!o['getClassName'] || typeof o['getClassName'] !== 'function') {
            return false;
        }

        return o instanceof JLObject || o instanceof JLArray || o instanceof JLString || o instanceof JLNumber || o instanceof JLBool || o instanceof JLNull;
    }

    public static serialize(adt: JSONLike): {[key: string]: JLObjectSerialized | JLArraySerialized | JLStringSerialized | JLNumberSerialized | JLBoolSerialized | JLNullSerialized} {
        let className = adt.getClassName();

        return {
            [className]: adt.serialize()
        };
    }

    public static deserialize(data: {[key: string]: JLObjectSerialized | JLArraySerialized | JLStringSerialized | JLNumberSerialized | JLBoolSerialized | JLNullSerialized}): JSONLike {
        const id = Object.keys(data)[0];
        const content = (data as any)[id];
        switch (id) {
            case 'JLObject': return new JLObject(content as any);
            case 'JLArray': return new JLArray(content as any);
            case 'JLString': return new JLString(content as any);
            case 'JLNumber': return new JLNumber(content as any);
            case 'JLBool': return new JLBool(content as any);
            case 'JLNull': return new JLNull(content as any);
            default:
                throw new Error('Unknown type id ' + id + ' for JSONLike');
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
Introspector.register('idltest.json.JSONLike', {
        full: 'idltest.json.JSONLike',
        short: 'JSONLike',
        package: 'idltest.json',
        type: IntrospectorTypes.Adt,
        options: [
            {
                name: 'JLObject',
                type: {intro: IntrospectorTypes.Data, full: 'idltest.json.JLObject'} as IIntrospectorUserType
            },
            {
                name: 'JLArray',
                type: {intro: IntrospectorTypes.Data, full: 'idltest.json.JLArray'} as IIntrospectorUserType
            },
            {
                name: 'JLString',
                type: {intro: IntrospectorTypes.Data, full: 'idltest.json.JLString'} as IIntrospectorUserType
            },
            {
                name: 'JLNumber',
                type: {intro: IntrospectorTypes.Data, full: 'idltest.json.JLNumber'} as IIntrospectorUserType
            },
            {
                name: 'JLBool',
                type: {intro: IntrospectorTypes.Data, full: 'idltest.json.JLBool'} as IIntrospectorUserType
            },
            {
                name: 'JLNull',
                type: {intro: IntrospectorTypes.Data, full: 'idltest.json.JLNull'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorAdtObject
);