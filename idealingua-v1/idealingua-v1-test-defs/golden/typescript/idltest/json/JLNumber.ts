// Auto-generated, any modifications may be overwritten in the future.

// JLNumber DTO
export class JLNumber  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.json';
    public static readonly ClassName = 'JLNumber';
    public static readonly FullClassName = 'idltest.json.JLNumber';

    public getPackageName(): string { return JLNumber.PackageName; }
    public getClassName(): string { return JLNumber.ClassName; }
    public getFullClassName(): string { return JLNumber.FullClassName; }

    private _value: number;

    public get value(): number {
        // Precision: 64
        return this._value;
    }

    public set value(value: number) {
        // Precision: 64
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field value is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field value expects type number, got ' + value);
        }

        this._value = value;
    }

    constructor(data: JLNumberSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.value = data.value;
    }

    public serialize(): JLNumberSerialized {
        return {
            value: this.value
        };
    }
}

export interface JLNumberSerialized  {
    value: number;
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
Introspector.register(JLNumber.FullClassName, {
        full: JLNumber.FullClassName,
        short: JLNumber.ClassName,
        package: JLNumber.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new JLNumber(),
        fields: [
            {
                name: 'value',
                accessName: 'value',
                type: {intro: IntrospectorTypes.F64}
            }
        ]
    } as IIntrospectorDataObject
);