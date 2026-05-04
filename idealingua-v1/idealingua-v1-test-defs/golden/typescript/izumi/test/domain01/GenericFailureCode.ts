// Auto-generated, any modifications may be overwritten in the future.

// GenericFailureCode Enumeration
export enum GenericFailureCode {
    EntityNotFound = 'EntityNotFound',
    EntityAlreadyExists = 'EntityAlreadyExists',
    ExpirationFailure = 'ExpirationFailure',
    ConditionNotMet = 'ConditionNotMet',
    AccessDenied = 'AccessDenied',
    AssertionFailed = 'AssertionFailed',
    UnexpectedException = 'UnexpectedException',
    CodecFailed = 'CodecFailed',
    Unknown = 'Unknown'
}

export class GenericFailureCodeHelpers {
    public static readonly all = [
        GenericFailureCode.EntityNotFound,
        GenericFailureCode.EntityAlreadyExists,
        GenericFailureCode.ExpirationFailure,
        GenericFailureCode.ConditionNotMet,
        GenericFailureCode.AccessDenied,
        GenericFailureCode.AssertionFailed,
        GenericFailureCode.UnexpectedException,
        GenericFailureCode.CodecFailed,
        GenericFailureCode.Unknown
    ];

    public static isValid(value: string): boolean {
        return GenericFailureCodeHelpers.all.indexOf(value as GenericFailureCode) >= 0;
    }
}

// Introspector registration
import { Introspector, IntrospectorTypes, IIntrospectorEnumObject } from '../../../irt';
Introspector.register('izumi.test.domain01.GenericFailureCode', {
        full: 'izumi.test.domain01.GenericFailureCode',
        short: 'GenericFailureCode',
        package: 'izumi.test.domain01',
        type: IntrospectorTypes.Enum,
        options: GenericFailureCodeHelpers.all
    } as IIntrospectorEnumObject
);