// Auto-generated, any modifications may be overwritten in the future.
import {
    BranchB,
    BranchBSerialized
} from './BranchB';
import {
    BranchA,
    BranchASerialized
} from './BranchA';

// ADTType Algebraic Data Type
export type ADTType = BranchA | BranchB;
export type ADTTypeSerialized = BranchASerialized | BranchBSerialized

export class ADTTypeHelpers {
    public static isInstanceOf(o: any): boolean {
        if (!o['getClassName'] || typeof o['getClassName'] !== 'function') {
            return false;
        }

        return o instanceof BranchA || o instanceof BranchB;
    }

    public static serialize(adt: ADTType): {[key: string]: BranchASerialized | BranchBSerialized} {
        let className = adt.getClassName();

        return {
            [className]: adt.serialize()
        };
    }

    public static deserialize(data: {[key: string]: BranchASerialized | BranchBSerialized}): ADTType {
        const id = Object.keys(data)[0];
        const content = (data as any)[id];
        switch (id) {
            case 'BranchA': return new BranchA(content as any);
            case 'BranchB': return new BranchB(content as any);
            default:
                throw new Error('Unknown type id ' + id + ' for ADTType');
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
Introspector.register('idltest.events.ADTType', {
        full: 'idltest.events.ADTType',
        short: 'ADTType',
        package: 'idltest.events',
        type: IntrospectorTypes.Adt,
        options: [
            {
                name: 'BranchA',
                type: {intro: IntrospectorTypes.Data, full: 'idltest.events.BranchA'} as IIntrospectorUserType
            },
            {
                name: 'BranchB',
                type: {intro: IntrospectorTypes.Data, full: 'idltest.events.BranchB'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorAdtObject
);