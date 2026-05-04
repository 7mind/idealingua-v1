// Auto-generated, any modifications may be overwritten in the future.

// JLBool DTO
export class JLBool  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.json';
    public static readonly ClassName = 'JLBool';
    public static readonly FullClassName = 'idltest.json.JLBool';

    public getPackageName(): string { return JLBool.PackageName; }
    public getClassName(): string { return JLBool.ClassName; }
    public getFullClassName(): string { return JLBool.FullClassName; }

    private _value: boolean;

    public get value(): boolean {
        return this._value;
    }

    public set value(value: boolean) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field value is not optional');
        }

        if (typeof value !== 'boolean') {
            throw new Error('Field value expects boolean type, got ' + value);
        }

        this._value = value;
    }

    constructor(data: JLBoolSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.value = data.value;
    }

    public serialize(): JLBoolSerialized {
        return {
            value: this.value
        };
    }
}

export interface JLBoolSerialized  {
    value: boolean;
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
Introspector.register(JLBool.FullClassName, {
        full: JLBool.FullClassName,
        short: JLBool.ClassName,
        package: JLBool.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new JLBool(),
        fields: [
            {
                name: 'value',
                accessName: 'value',
                type: {intro: IntrospectorTypes.Bool}
            }
        ]
    } as IIntrospectorDataObject
);