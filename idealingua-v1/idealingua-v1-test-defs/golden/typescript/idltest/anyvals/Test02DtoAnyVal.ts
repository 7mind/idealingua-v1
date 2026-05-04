// Auto-generated, any modifications may be overwritten in the future.

// Test02DtoAnyVal DTO
export class Test02DtoAnyVal  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.anyvals';
    public static readonly ClassName = 'Test02DtoAnyVal';
    public static readonly FullClassName = 'idltest.anyvals.Test02DtoAnyVal';

    public getPackageName(): string { return Test02DtoAnyVal.PackageName; }
    public getClassName(): string { return Test02DtoAnyVal.ClassName; }
    public getFullClassName(): string { return Test02DtoAnyVal.FullClassName; }

    private _value: string;

    public get value(): string {
        return this._value;
    }

    public set value(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field value is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field value expects type string, got ' + value);
        }

        this._value = value;
    }

    constructor(data: Test02DtoAnyValSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.value = data.value;
    }

    public serialize(): Test02DtoAnyValSerialized {
        return {
            value: this.value
        };
    }
}

export interface Test02DtoAnyValSerialized  {
    value: string;
}

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorDataObject
} from '../../irt';
Introspector.register(Test02DtoAnyVal.FullClassName, {
        full: Test02DtoAnyVal.FullClassName,
        short: Test02DtoAnyVal.ClassName,
        package: Test02DtoAnyVal.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new Test02DtoAnyVal(),
        fields: [
            {
                name: 'value',
                accessName: 'value',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);