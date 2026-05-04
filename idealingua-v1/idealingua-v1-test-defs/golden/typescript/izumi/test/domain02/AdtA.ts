// Auto-generated, any modifications may be overwritten in the future.
import {
    AdtA2,
    AdtA2Serialized
} from './AdtA2';
import {
    AdtA1,
    AdtA1Serialized
} from './AdtA1';

// AdtA Algebraic Data Type
export type AdtA = AdtA1 | AdtA2;
export type AdtASerialized = AdtA1Serialized | AdtA2Serialized

export class AdtAHelpers {
    public static isInstanceOf(o: any): boolean {
        if (!o['getClassName'] || typeof o['getClassName'] !== 'function') {
            return false;
        }

        return o instanceof AdtA1 || o instanceof AdtA2;
    }

    public static serialize(adt: AdtA): {[key: string]: AdtA1Serialized | AdtA2Serialized} {
        let className = adt.getClassName();

        return {
            [className]: adt.serialize()
        };
    }

    public static deserialize(data: {[key: string]: AdtA1Serialized | AdtA2Serialized}): AdtA {
        const id = Object.keys(data)[0];
        const content = (data as any)[id];
        switch (id) {
            case 'AdtA1': return new AdtA1(content as any);
            case 'AdtA2': return new AdtA2(content as any);
            default:
                throw new Error('Unknown type id ' + id + ' for AdtA');
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
} from '../../../irt';
Introspector.register('izumi.test.domain02.AdtA', {
        full: 'izumi.test.domain02.AdtA',
        short: 'AdtA',
        package: 'izumi.test.domain02',
        type: IntrospectorTypes.Adt,
        options: [
            {
                name: 'AdtA1',
                type: {intro: IntrospectorTypes.Data, full: 'izumi.test.domain02.AdtA1'} as IIntrospectorUserType
            },
            {
                name: 'AdtA2',
                type: {intro: IntrospectorTypes.Data, full: 'izumi.test.domain02.AdtA2'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorAdtObject
);