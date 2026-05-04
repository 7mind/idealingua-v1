// Auto-generated, any modifications may be overwritten in the future.

// GoAliasEnumTest Enumeration
export enum GoAliasEnumTest {
    Val1 = 'Val1',
    Val2 = 'Val2'
}

export class GoAliasEnumTestHelpers {
    public static readonly all = [
        GoAliasEnumTest.Val1,
        GoAliasEnumTest.Val2
    ];

    public static isValid(value: string): boolean {
        return GoAliasEnumTestHelpers.all.indexOf(value as GoAliasEnumTest) >= 0;
    }
}

// Introspector registration
import { Introspector, IntrospectorTypes, IIntrospectorEnumObject } from '../../../irt';
Introspector.register('izumi.test.domain01.GoAliasEnumTest', {
        full: 'izumi.test.domain01.GoAliasEnumTest',
        short: 'GoAliasEnumTest',
        package: 'izumi.test.domain01',
        type: IntrospectorTypes.Enum,
        options: GoAliasEnumTestHelpers.all
    } as IIntrospectorEnumObject
);