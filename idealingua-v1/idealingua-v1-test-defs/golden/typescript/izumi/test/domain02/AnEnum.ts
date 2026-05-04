// Auto-generated, any modifications may be overwritten in the future.

// AnEnum Enumeration
export enum AnEnum {
    VALUE1 = 'VALUE1',
    VALUE2 = 'VALUE2'
}

export class AnEnumHelpers {
    public static readonly all = [
        AnEnum.VALUE1,
        AnEnum.VALUE2
    ];

    public static isValid(value: string): boolean {
        return AnEnumHelpers.all.indexOf(value as AnEnum) >= 0;
    }
}

// Introspector registration
import { Introspector, IntrospectorTypes, IIntrospectorEnumObject } from '../../../irt';
Introspector.register('izumi.test.domain02.AnEnum', {
        full: 'izumi.test.domain02.AnEnum',
        short: 'AnEnum',
        package: 'izumi.test.domain02',
        type: IntrospectorTypes.Enum,
        options: AnEnumHelpers.all
    } as IIntrospectorEnumObject
);