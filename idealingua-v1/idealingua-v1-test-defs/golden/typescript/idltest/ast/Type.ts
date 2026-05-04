// Auto-generated, any modifications may be overwritten in the future.

// Type DTO
export class Type  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.ast';
    public static readonly ClassName = 'Type';
    public static readonly FullClassName = 'idltest.ast.Type';

    public getPackageName(): string { return Type.PackageName; }
    public getClassName(): string { return Type.ClassName; }
    public getFullClassName(): string { return Type.FullClassName; }

    private _label: string;

    public get label(): string {
        return this._label;
    }

    public set label(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field label is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field label expects type string, got ' + value);
        }

        this._label = value;
    }

    constructor(data: TypeSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.label = data.label;
    }

    public serialize(): TypeSerialized {
        return {
            label: this.label
        };
    }
}

export interface TypeSerialized  {
    label: string;
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
Introspector.register(Type.FullClassName, {
        full: Type.FullClassName,
        short: Type.ClassName,
        package: Type.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new Type(),
        fields: [
            {
                name: 'label',
                accessName: 'label',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);