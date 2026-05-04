// Auto-generated, any modifications may be overwritten in the future.
import {
    TestMixin,
    TestMixinStruct,
    TestMixinStructSerialized
} from './TestMixin';
import {
    TestOneliners,
    TestOnelinersSerialized
} from './TestOneliners';
import {
    TestDto,
    TestDtoSerialized
} from './TestDto';

// Ast Algebraic Data Type
export type Ast = TestMixin | TestDto | TestOneliners;
export type AstSerialized = {[key: string]: TestMixinStructSerialized} | TestDtoSerialized | TestOnelinersSerialized

export class AstHelpers {
    public static isInstanceOf(o: any): boolean {
        if (!o['getClassName'] || typeof o['getClassName'] !== 'function') {
            return false;
        }
        const fullClassName = o.getFullClassName();
        return TestMixinStruct.isRegisteredType(fullClassName) || o instanceof TestDto || o instanceof TestOneliners;
    }

    public static serialize(adt: Ast): {[key: string]: TestMixinStructSerialized | TestDtoSerialized | TestOnelinersSerialized} {
        let className = adt.getClassName();
        const fullClassName = adt.getFullClassName();
        let serialized: any = undefined;

        if (TestMixinStruct.isRegisteredType(fullClassName)) {
            className = 'TestMixin'; serialized = {[fullClassName]: adt.serialize()};
        }

        return {
            [className]: serialized || adt.serialize()
        };
    }

    public static deserialize(data: {[key: string]: TestMixinStructSerialized | TestDtoSerialized | TestOnelinersSerialized}): Ast {
        const id = Object.keys(data)[0];
        const content = (data as any)[id];
        switch (id) {
            case 'TestMixin': return TestMixinStruct.create(content as any);
            case 'TestDto': return new TestDto(content as any);
            case 'TestOneliners': return new TestOneliners(content as any);
            default:
                throw new Error('Unknown type id ' + id + ' for Ast');
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
Introspector.register('idltest.syntax.Ast', {
        full: 'idltest.syntax.Ast',
        short: 'Ast',
        package: 'idltest.syntax',
        type: IntrospectorTypes.Adt,
        options: [
            {
                name: 'TestMixin',
                type: {intro: IntrospectorTypes.Mixin, full: 'idltest.syntax.TestMixin'} as IIntrospectorUserType
            },
            {
                name: 'TestDto',
                type: {intro: IntrospectorTypes.Data, full: 'idltest.syntax.TestDto'} as IIntrospectorUserType
            },
            {
                name: 'TestOneliners',
                type: {intro: IntrospectorTypes.Data, full: 'idltest.syntax.TestOneliners'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorAdtObject
);