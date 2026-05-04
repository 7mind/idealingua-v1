// Auto-generated, any modifications may be overwritten in the future.
import {
    Success,
    SuccessSerialized
} from './Success';
import {
    Failure,
    FailureSerialized
} from './Failure';

// Alternative Algebraic Data Type
export type Alternative = Success | Failure;
export type AlternativeSerialized = SuccessSerialized | FailureSerialized

export class AlternativeHelpers {
    public static isInstanceOf(o: any): boolean {
        if (!o['getClassName'] || typeof o['getClassName'] !== 'function') {
            return false;
        }

        return o instanceof Success || o instanceof Failure;
    }

    public static serialize(adt: Alternative): {[key: string]: SuccessSerialized | FailureSerialized} {
        let className = adt.getClassName();

        if (className == 'Success') {
            className = 'TestSuccess'
        }
        return {
            [className]: adt.serialize()
        };
    }

    public static deserialize(data: {[key: string]: SuccessSerialized | FailureSerialized}): Alternative {
        const id = Object.keys(data)[0];
        const content = (data as any)[id];
        switch (id) {
            case 'TestSuccess': return new Success(content as any);
            case 'Failure': return new Failure(content as any);
            default:
                throw new Error('Unknown type id ' + id + ' for Alternative');
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
Introspector.register('idltest.algebraics.Alternative', {
        full: 'idltest.algebraics.Alternative',
        short: 'Alternative',
        package: 'idltest.algebraics',
        type: IntrospectorTypes.Adt,
        options: [
            {
                name: 'TestSuccess',
                type: {intro: IntrospectorTypes.Data, full: 'idltest.algebraics.Success'} as IIntrospectorUserType
            },
            {
                name: 'Failure',
                type: {intro: IntrospectorTypes.Data, full: 'idltest.algebraics.Failure'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorAdtObject
);