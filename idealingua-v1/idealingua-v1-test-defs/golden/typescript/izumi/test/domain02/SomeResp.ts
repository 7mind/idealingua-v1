// Auto-generated, any modifications may be overwritten in the future.
import {
    BasicFailure,
    BasicFailureSerialized
} from '../domain01';
import {
    DTO1,
    DTO1Serialized
} from './DTO1';

// SomeResp Algebraic Data Type
export type SomeResp = BasicFailure | DTO1;
export type SomeRespSerialized = BasicFailureSerialized | DTO1Serialized

export class SomeRespHelpers {
    public static isInstanceOf(o: any): boolean {
        if (!o['getClassName'] || typeof o['getClassName'] !== 'function') {
            return false;
        }

        return o instanceof BasicFailure || o instanceof DTO1;
    }

    public static serialize(adt: SomeResp): {[key: string]: BasicFailureSerialized | DTO1Serialized} {
        let className = adt.getClassName();

        return {
            [className]: adt.serialize()
        };
    }

    public static deserialize(data: {[key: string]: BasicFailureSerialized | DTO1Serialized}): SomeResp {
        const id = Object.keys(data)[0];
        const content = (data as any)[id];
        switch (id) {
            case 'ImportedBasicFailure': return new BasicFailure(content as any);
            case 'DTO1': return new DTO1(content as any);
            default:
                throw new Error('Unknown type id ' + id + ' for SomeResp');
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
Introspector.register('izumi.test.domain02.SomeResp', {
        full: 'izumi.test.domain02.SomeResp',
        short: 'SomeResp',
        package: 'izumi.test.domain02',
        type: IntrospectorTypes.Adt,
        options: [
            {
                name: 'ImportedBasicFailure',
                type: {intro: IntrospectorTypes.Data, full: 'izumi.test.domain01.BasicFailure'} as IIntrospectorUserType
            },
            {
                name: 'DTO1',
                type: {intro: IntrospectorTypes.Data, full: 'izumi.test.domain02.DTO1'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorAdtObject
);