// Auto-generated, any modifications may be overwritten in the future.

// JLString DTO
export class JLString  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.json';
    public static readonly ClassName = 'JLString';
    public static readonly FullClassName = 'idltest.json.JLString';

    public getPackageName(): string { return JLString.PackageName; }
    public getClassName(): string { return JLString.ClassName; }
    public getFullClassName(): string { return JLString.FullClassName; }

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

    constructor(data: JLStringSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.value = data.value;
    }

    public serialize(): JLStringSerialized {
        return {
            value: this.value
        };
    }
}

export interface JLStringSerialized  {
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
Introspector.register(JLString.FullClassName, {
        full: JLString.FullClassName,
        short: JLString.ClassName,
        package: JLString.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new JLString(),
        fields: [
            {
                name: 'value',
                accessName: 'value',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);