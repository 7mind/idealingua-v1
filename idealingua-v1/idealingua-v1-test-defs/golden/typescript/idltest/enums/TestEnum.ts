// Auto-generated, any modifications may be overwritten in the future.

// TestEnum Enumeration
export enum TestEnum {
    Element1 = 'Element1',
    Element2 = 'Element2',
    Element3 = 'Element3',
    Element4 = 'Element4'
}

export class TestEnumHelpers {
    public static readonly all = [
        TestEnum.Element1,
        TestEnum.Element2,
        TestEnum.Element3,
        TestEnum.Element4
    ];

    public static isValid(value: string): boolean {
        return TestEnumHelpers.all.indexOf(value as TestEnum) >= 0;
    }
}

// Introspector registration
import { Introspector, IntrospectorTypes, IIntrospectorEnumObject } from '../../irt';
Introspector.register('idltest.enums.TestEnum', {
        full: 'idltest.enums.TestEnum',
        short: 'TestEnum',
        package: 'idltest.enums',
        type: IntrospectorTypes.Enum,
        options: TestEnumHelpers.all
    } as IIntrospectorEnumObject
);