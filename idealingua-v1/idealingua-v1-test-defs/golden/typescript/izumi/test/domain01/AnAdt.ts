// Auto-generated, any modifications may be overwritten in the future.
import {
    AllTypes,
    AllTypesStruct,
    AllTypesStructSerialized
} from './AllTypes';
import {
    TestObject,
    TestObjectSerialized
} from './TestObject';
import {
    AnyValTest,
    AnyValTestStruct,
    AnyValTestStructSerialized
} from './AnyValTest';

// AnAdt Algebraic Data Type
export type AnAdt = AllTypes | TestObject | AnyValTest;
export type AnAdtSerialized = {[key: string]: AllTypesStructSerialized} | TestObjectSerialized | {[key: string]: AnyValTestStructSerialized}

export class AnAdtHelpers {
    public static isInstanceOf(o: any): boolean {
        if (!o['getClassName'] || typeof o['getClassName'] !== 'function') {
            return false;
        }
        const fullClassName = o.getFullClassName();
        return AllTypesStruct.isRegisteredType(fullClassName) || o instanceof TestObject || AnyValTestStruct.isRegisteredType(fullClassName);
    }

    public static serialize(adt: AnAdt): {[key: string]: AllTypesStructSerialized | TestObjectSerialized | AnyValTestStructSerialized} {
        let className = adt.getClassName();
        const fullClassName = adt.getFullClassName();
        let serialized: any = undefined;

        if (AllTypesStruct.isRegisteredType(fullClassName)) {
            className = 'AllTypes'; serialized = {[fullClassName]: adt.serialize()};
        } else 
        if (AnyValTestStruct.isRegisteredType(fullClassName)) {
            className = 'AnyValTest'; serialized = {[fullClassName]: adt.serialize()};
        }
        if (className == 'AnyValTest') {
            className = 'AnotherMember'
        }
        return {
            [className]: serialized || adt.serialize()
        };
    }

    public static deserialize(data: {[key: string]: AllTypesStructSerialized | TestObjectSerialized | AnyValTestStructSerialized}): AnAdt {
        const id = Object.keys(data)[0];
        const content = (data as any)[id];
        switch (id) {
            case 'AllTypes': return AllTypesStruct.create(content as any);
            case 'TestObject': return new TestObject(content as any);
            case 'AnotherMember': return AnyValTestStruct.create(content as any);
            default:
                throw new Error('Unknown type id ' + id + ' for AnAdt');
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
Introspector.register('izumi.test.domain01.AnAdt', {
        full: 'izumi.test.domain01.AnAdt',
        short: 'AnAdt',
        package: 'izumi.test.domain01',
        type: IntrospectorTypes.Adt,
        options: [
            {
                name: 'AllTypes',
                type: {intro: IntrospectorTypes.Mixin, full: 'izumi.test.domain01.AllTypes'} as IIntrospectorUserType
            },
            {
                name: 'TestObject',
                type: {intro: IntrospectorTypes.Data, full: 'izumi.test.domain01.TestObject'} as IIntrospectorUserType
            },
            {
                name: 'AnotherMember',
                type: {intro: IntrospectorTypes.Mixin, full: 'izumi.test.domain01.AnyValTest'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorAdtObject
);