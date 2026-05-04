// Auto-generated, any modifications may be overwritten in the future.

// SomeEnum Enumeration
export enum SomeEnum {
    VALUE = 'VALUE'
}

export class SomeEnumHelpers {
    public static readonly all = [
        SomeEnum.VALUE
    ];

    public static isValid(value: string): boolean {
        return SomeEnumHelpers.all.indexOf(value as SomeEnum) >= 0;
    }
}

// Introspector registration
import { Introspector, IntrospectorTypes, IIntrospectorEnumObject } from '../../../../irt';
Introspector.register('izumi.test.clashing.another.SomeEnum', {
        full: 'izumi.test.clashing.another.SomeEnum',
        short: 'SomeEnum',
        package: 'izumi.test.clashing.another',
        type: IntrospectorTypes.Enum,
        options: SomeEnumHelpers.all
    } as IIntrospectorEnumObject
);