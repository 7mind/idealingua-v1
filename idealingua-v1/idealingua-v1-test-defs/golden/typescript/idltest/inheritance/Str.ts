// Auto-generated, any modifications may be overwritten in the future.
import {
    Empty,
    EmptyStruct,
    EmptyStructSerialized
} from './Empty';

// Str DTO
export class Str implements Empty  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.inheritance';
    public static readonly ClassName = 'Str';
    public static readonly FullClassName = 'idltest.inheritance.Str';

    public getPackageName(): string { return Str.PackageName; }
    public getClassName(): string { return Str.ClassName; }
    public getFullClassName(): string { return Str.FullClassName; }

    private _str: string;

    public get str(): string {
        return this._str;
    }

    public set str(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field str is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field str expects type string, got ' + value);
        }

        this._str = value;
    }

    constructor(data: StrSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.str = data.str;
    }

    public toEmptySerialized(): EmptyStructSerialized {
        return {
        };
    }

    public toEmpty(): EmptyStruct {
        return new EmptyStruct(this.toEmptySerialized());
    }

    public loadEmptySerialized(slice: EmptyStructSerialized) {
    }

    public loadEmpty(slice: EmptyStruct) {
        this.loadEmptySerialized(slice.serialize());
    }

    public serialize(): StrSerialized {
        return {
            str: this.str
        };
    }
}

export interface StrSerialized extends EmptyStructSerialized  {
    str: string;
}

EmptyStruct.register(Str.FullClassName, Str);

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorDataObject
} from '../../irt';
Introspector.register(Str.FullClassName, {
        full: Str.FullClassName,
        short: Str.ClassName,
        package: Str.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new Str(),
        fields: [
            {
                name: 'str',
                accessName: 'str',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);