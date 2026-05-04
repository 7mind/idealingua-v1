// Auto-generated, any modifications may be overwritten in the future.

// EnumType Enumeration
export enum EnumType {
    EnumA = 'EnumA',
    EnumB = 'EnumB'
}

export class EnumTypeHelpers {
    public static readonly all = [
        EnumType.EnumA,
        EnumType.EnumB
    ];

    public static isValid(value: string): boolean {
        return EnumTypeHelpers.all.indexOf(value as EnumType) >= 0;
    }
}

// Introspector registration
import { Introspector, IntrospectorTypes, IIntrospectorEnumObject } from '../../irt';
Introspector.register('idltest.events.EnumType', {
        full: 'idltest.events.EnumType',
        short: 'EnumType',
        package: 'idltest.events',
        type: IntrospectorTypes.Enum,
        options: EnumTypeHelpers.all
    } as IIntrospectorEnumObject
);