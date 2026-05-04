// Auto-generated, any modifications may be overwritten in the future.

// TestServiceEnum Enumeration
export enum TestServiceEnum {
    Value1 = 'Value1',
    Value2 = 'Value2'
}

export class TestServiceEnumHelpers {
    public static readonly all = [
        TestServiceEnum.Value1,
        TestServiceEnum.Value2
    ];

    public static isValid(value: string): boolean {
        return TestServiceEnumHelpers.all.indexOf(value as TestServiceEnum) >= 0;
    }
}

// Introspector registration
import { Introspector, IntrospectorTypes, IIntrospectorEnumObject } from '../../irt';
Introspector.register('idltest.services.TestServiceEnum', {
        full: 'idltest.services.TestServiceEnum',
        short: 'TestServiceEnum',
        package: 'idltest.services',
        type: IntrospectorTypes.Enum,
        options: TestServiceEnumHelpers.all
    } as IIntrospectorEnumObject
);