// Auto-generated, any modifications may be overwritten in the future.

// ShortSyntaxEnum Enumeration
export enum ShortSyntaxEnum {
    Element11 = 'Element11',
    Element22 = 'Element22'
}

export class ShortSyntaxEnumHelpers {
    public static readonly all = [
        ShortSyntaxEnum.Element11,
        ShortSyntaxEnum.Element22
    ];

    public static isValid(value: string): boolean {
        return ShortSyntaxEnumHelpers.all.indexOf(value as ShortSyntaxEnum) >= 0;
    }
}

// Introspector registration
import { Introspector, IntrospectorTypes, IIntrospectorEnumObject } from '../../irt';
Introspector.register('idltest.enums.ShortSyntaxEnum', {
        full: 'idltest.enums.ShortSyntaxEnum',
        short: 'ShortSyntaxEnum',
        package: 'idltest.enums',
        type: IntrospectorTypes.Enum,
        options: ShortSyntaxEnumHelpers.all
    } as IIntrospectorEnumObject
);