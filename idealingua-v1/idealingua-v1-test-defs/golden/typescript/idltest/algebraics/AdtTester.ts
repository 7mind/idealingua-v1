// Auto-generated, any modifications may be overwritten in the future.
import {
    ComplexAdt,
    ComplexAdtSerialized
} from './ComplexAdt';
import {
    ComplexAdt2,
    ComplexAdt2Serialized
} from './ComplexAdt2';

// AdtTester Algebraic Data Type
export type AdtTester = ComplexAdt | ComplexAdt2;
export type AdtTesterSerialized = ComplexAdtSerialized | ComplexAdt2Serialized

export class AdtTesterHelpers {
    public static isInstanceOf(o: any): boolean {
        if (!o['getClassName'] || typeof o['getClassName'] !== 'function') {
            return false;
        }

        return o instanceof ComplexAdt || o instanceof ComplexAdt2;
    }

    public static serialize(adt: AdtTester): {[key: string]: ComplexAdtSerialized | ComplexAdt2Serialized} {
        let className = adt.getClassName();

        return {
            [className]: adt.serialize()
        };
    }

    public static deserialize(data: {[key: string]: ComplexAdtSerialized | ComplexAdt2Serialized}): AdtTester {
        const id = Object.keys(data)[0];
        const content = (data as any)[id];
        switch (id) {
            case 'ComplexAdt': return new ComplexAdt(content as any);
            case 'ComplexAdt2': return new ComplexAdt2(content as any);
            default:
                throw new Error('Unknown type id ' + id + ' for AdtTester');
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
Introspector.register('idltest.algebraics.AdtTester', {
        full: 'idltest.algebraics.AdtTester',
        short: 'AdtTester',
        package: 'idltest.algebraics',
        type: IntrospectorTypes.Adt,
        options: [
            {
                name: 'ComplexAdt',
                type: {intro: IntrospectorTypes.Data, full: 'idltest.algebraics.ComplexAdt'} as IIntrospectorUserType
            },
            {
                name: 'ComplexAdt2',
                type: {intro: IntrospectorTypes.Data, full: 'idltest.algebraics.ComplexAdt2'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorAdtObject
);