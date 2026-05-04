// Auto-generated, any modifications may be overwritten in the future.
import {
    PublicData,
    PublicDataSerialized
} from './PublicData';
import {
    EventData,
    EventDataSerialized
} from './EventData';

// AliasedAdt Algebraic Data Type
export type AliasedAdt = EventData | PublicData;
export type AliasedAdtSerialized = EventDataSerialized | PublicDataSerialized

export class AliasedAdtHelpers {
    public static isInstanceOf(o: any): boolean {
        if (!o['getClassName'] || typeof o['getClassName'] !== 'function') {
            return false;
        }

        return o instanceof EventData || o instanceof PublicData;
    }

    public static serialize(adt: AliasedAdt): {[key: string]: EventDataSerialized | PublicDataSerialized} {
        let className = adt.getClassName();

        if (className == 'EventData') {
            className = 'event'
        }
        if (className == 'PublicData') {
            className = 'public'
        }
        return {
            [className]: adt.serialize()
        };
    }

    public static deserialize(data: {[key: string]: EventDataSerialized | PublicDataSerialized}): AliasedAdt {
        const id = Object.keys(data)[0];
        const content = (data as any)[id];
        switch (id) {
            case 'event': return new EventData(content as any);
            case 'public': return new PublicData(content as any);
            default:
                throw new Error('Unknown type id ' + id + ' for AliasedAdt');
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
Introspector.register('idltest.ast.AliasedAdt', {
        full: 'idltest.ast.AliasedAdt',
        short: 'AliasedAdt',
        package: 'idltest.ast',
        type: IntrospectorTypes.Adt,
        options: [
            {
                name: 'event',
                type: {intro: IntrospectorTypes.Data, full: 'idltest.ast.EventData'} as IIntrospectorUserType
            },
            {
                name: 'public',
                type: {intro: IntrospectorTypes.Data, full: 'idltest.ast.PublicData'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorAdtObject
);