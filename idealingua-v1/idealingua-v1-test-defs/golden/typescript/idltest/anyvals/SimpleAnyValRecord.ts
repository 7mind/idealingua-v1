// Auto-generated, any modifications may be overwritten in the future.

// SimpleAnyValRecord DTO
export class SimpleAnyValRecord  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.anyvals';
    public static readonly ClassName = 'SimpleAnyValRecord';
    public static readonly FullClassName = 'idltest.anyvals.SimpleAnyValRecord';

    public getPackageName(): string { return SimpleAnyValRecord.PackageName; }
    public getClassName(): string { return SimpleAnyValRecord.ClassName; }
    public getFullClassName(): string { return SimpleAnyValRecord.FullClassName; }

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

    constructor(data: SimpleAnyValRecordSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.value = data.value;
    }

    public serialize(): SimpleAnyValRecordSerialized {
        return {
            value: this.value
        };
    }
}

export interface SimpleAnyValRecordSerialized  {
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
Introspector.register(SimpleAnyValRecord.FullClassName, {
        full: SimpleAnyValRecord.FullClassName,
        short: SimpleAnyValRecord.ClassName,
        package: SimpleAnyValRecord.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new SimpleAnyValRecord(),
        fields: [
            {
                name: 'value',
                accessName: 'value',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);