// Auto-generated, any modifications may be overwritten in the future.
import {
    Success,
    SuccessSerialized
} from './Success';
import {
    AFace,
    AFaceStruct,
    AFaceStructSerialized
} from './AFace';

// AdtWithInterface Algebraic Data Type
export type AdtWithInterface = AFace | Success;
export type AdtWithInterfaceSerialized = {[key: string]: AFaceStructSerialized} | SuccessSerialized

export class AdtWithInterfaceHelpers {
    public static isInstanceOf(o: any): boolean {
        if (!o['getClassName'] || typeof o['getClassName'] !== 'function') {
            return false;
        }
        const fullClassName = o.getFullClassName();
        return AFaceStruct.isRegisteredType(fullClassName) || o instanceof Success;
    }

    public static serialize(adt: AdtWithInterface): {[key: string]: AFaceStructSerialized | SuccessSerialized} {
        let className = adt.getClassName();
        const fullClassName = adt.getFullClassName();
        let serialized: any = undefined;

        if (AFaceStruct.isRegisteredType(fullClassName)) {
            className = 'AFace'; serialized = {[fullClassName]: adt.serialize()};
        }

        return {
            [className]: serialized || adt.serialize()
        };
    }

    public static deserialize(data: {[key: string]: AFaceStructSerialized | SuccessSerialized}): AdtWithInterface {
        const id = Object.keys(data)[0];
        const content = (data as any)[id];
        switch (id) {
            case 'AFace': return AFaceStruct.create(content as any);
            case 'Success': return new Success(content as any);
            default:
                throw new Error('Unknown type id ' + id + ' for AdtWithInterface');
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
Introspector.register('idltest.algebraics.AdtWithInterface', {
        full: 'idltest.algebraics.AdtWithInterface',
        short: 'AdtWithInterface',
        package: 'idltest.algebraics',
        type: IntrospectorTypes.Adt,
        options: [
            {
                name: 'AFace',
                type: {intro: IntrospectorTypes.Mixin, full: 'idltest.algebraics.AFace'} as IIntrospectorUserType
            },
            {
                name: 'Success',
                type: {intro: IntrospectorTypes.Data, full: 'idltest.algebraics.Success'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorAdtObject
);