// Auto-generated, any modifications may be overwritten in the future.

// Test00Data1AnyVal DTO
export class Test00Data1AnyVal  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.anyvals';
    public static readonly ClassName = 'Test00Data1AnyVal';
    public static readonly FullClassName = 'idltest.anyvals.Test00Data1AnyVal';

    public getPackageName(): string { return Test00Data1AnyVal.PackageName; }
    public getClassName(): string { return Test00Data1AnyVal.ClassName; }
    public getFullClassName(): string { return Test00Data1AnyVal.FullClassName; }

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

    constructor(data: Test00Data1AnyValSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.value = data.value;
    }

    public serialize(): Test00Data1AnyValSerialized {
        return {
            value: this.value
        };
    }
}

export interface Test00Data1AnyValSerialized  {
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
Introspector.register(Test00Data1AnyVal.FullClassName, {
        full: Test00Data1AnyVal.FullClassName,
        short: Test00Data1AnyVal.ClassName,
        package: Test00Data1AnyVal.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new Test00Data1AnyVal(),
        fields: [
            {
                name: 'value',
                accessName: 'value',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);