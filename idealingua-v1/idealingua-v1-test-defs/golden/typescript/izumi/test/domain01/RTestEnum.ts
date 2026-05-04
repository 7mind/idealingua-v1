// Auto-generated, any modifications may be overwritten in the future.

// RTestEnum Enumeration
export enum RTestEnum {
    A = 'A'
}

export class RTestEnumHelpers {
    public static readonly all = [
        RTestEnum.A
    ];

    public static isValid(value: string): boolean {
        return RTestEnumHelpers.all.indexOf(value as RTestEnum) >= 0;
    }
}

// Introspector registration
import { Introspector, IntrospectorTypes, IIntrospectorEnumObject } from '../../../irt';
Introspector.register('izumi.test.domain01.RTestEnum', {
        full: 'izumi.test.domain01.RTestEnum',
        short: 'RTestEnum',
        package: 'izumi.test.domain01',
        type: IntrospectorTypes.Enum,
        options: RTestEnumHelpers.all
    } as IIntrospectorEnumObject
);