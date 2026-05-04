// Auto-generated, any modifications may be overwritten in the future.

// Environment Enumeration
export enum Environment {
    Dev = 'Dev',
    Prod = 'Prod'
}

export class EnvironmentHelpers {
    public static readonly all = [
        Environment.Dev,
        Environment.Prod
    ];

    public static isValid(value: string): boolean {
        return EnvironmentHelpers.all.indexOf(value as Environment) >= 0;
    }
}

// Introspector registration
import { Introspector, IntrospectorTypes, IIntrospectorEnumObject } from '../../irt';
Introspector.register('idltest.services.Environment', {
        full: 'idltest.services.Environment',
        short: 'Environment',
        package: 'idltest.services',
        type: IntrospectorTypes.Enum,
        options: EnvironmentHelpers.all
    } as IIntrospectorEnumObject
);