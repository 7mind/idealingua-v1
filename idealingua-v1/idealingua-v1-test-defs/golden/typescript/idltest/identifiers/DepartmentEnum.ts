// Auto-generated, any modifications may be overwritten in the future.

// DepartmentEnum Enumeration
export enum DepartmentEnum {
    Engineering = 'Engineering',
    Sales = 'Sales'
}

export class DepartmentEnumHelpers {
    public static readonly all = [
        DepartmentEnum.Engineering,
        DepartmentEnum.Sales
    ];

    public static isValid(value: string): boolean {
        return DepartmentEnumHelpers.all.indexOf(value as DepartmentEnum) >= 0;
    }
}

// Introspector registration
import { Introspector, IntrospectorTypes, IIntrospectorEnumObject } from '../../irt';
Introspector.register('idltest.identifiers.DepartmentEnum', {
        full: 'idltest.identifiers.DepartmentEnum',
        short: 'DepartmentEnum',
        package: 'idltest.identifiers',
        type: IntrospectorTypes.Enum,
        options: DepartmentEnumHelpers.all
    } as IIntrospectorEnumObject
);