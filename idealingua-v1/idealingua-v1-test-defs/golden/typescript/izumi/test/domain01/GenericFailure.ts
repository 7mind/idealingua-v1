// Auto-generated, any modifications may be overwritten in the future.
import {
    GenericFailureCode
} from './GenericFailureCode';
import {
    GenericFailureDataStruct,
    GenericFailureDataStructSerialized
} from './GenericFailureData';

// GenericFailure DTO
export class GenericFailure  {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain01';
    public static readonly ClassName = 'GenericFailure';
    public static readonly FullClassName = 'izumi.test.domain01.GenericFailure';

    public getPackageName(): string { return GenericFailure.PackageName; }
    public getClassName(): string { return GenericFailure.ClassName; }
    public getFullClassName(): string { return GenericFailure.FullClassName; }

    private _message: string;
    private _diagnostics: string | undefined;
    private _reserved: {[key: string]: string};
    private _code: GenericFailureCode;

    public get message(): string {
        return this._message;
    }

    public set message(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field message is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field message expects type string, got ' + value);
        }

        this._message = value;
    }

    public get diagnostics(): string | undefined {
        return this._diagnostics;
    }

    public set diagnostics(value: string | undefined) {
        if (typeof value === 'undefined' || value === null) {
            this._diagnostics = undefined;
            return;
        }

        if (typeof value !== 'string') {
            throw new Error('Field diagnostics expects type string, got ' + value);
        }

        this._diagnostics = value;
    }

    public get reserved(): {[key: string]: string} {
        return this._reserved;
    }

    public set reserved(value: {[key: string]: string}) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field reserved is not optional');
        }
        this._reserved = value;
    }

    public get code(): GenericFailureCode {
        return this._code;
    }

    public set code(value: GenericFailureCode) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field code is not optional');
        }
        this._code = value;
    }

    constructor(data: GenericFailureSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            this.reserved = {};
            return;
        }

        this.message = data.message;
        this.diagnostics = typeof data.diagnostics !== 'undefined' ? data.diagnostics : undefined;
        this.reserved = Object.keys(data.reserved).reduce<any>((previous, current) => {previous[current] = data.reserved[current as any]; return previous; }, {});
        this.code = GenericFailureCode[data.code as keyof typeof GenericFailureCode];
    }

    public serialize(): GenericFailureSerialized {
        return {
            message: this.message,
            diagnostics: typeof this.diagnostics !== 'undefined' ? this.diagnostics : undefined,
            reserved: Object.keys(this.reserved).reduce<any>((previous, current) => {previous[current] = this.reserved[current as any]; return previous; }, {}),
            code: GenericFailureCode[this.code]
        };
    }
}

export interface GenericFailureSerialized  {
    message: string;
    diagnostics: string | undefined;
    reserved: {[key: string]: string};
    code: string;
}

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorDataObject
} from '../../../irt';
Introspector.register(GenericFailure.FullClassName, {
        full: GenericFailure.FullClassName,
        short: GenericFailure.ClassName,
        package: GenericFailure.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new GenericFailure(),
        fields: [
            {
                name: 'code',
                accessName: 'code',
                type: {intro: IntrospectorTypes.Enum, full: 'izumi.test.domain01.GenericFailureCode'} as IIntrospectorUserType
            },
            {
                name: 'message',
                accessName: 'message',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'diagnostics',
                accessName: 'diagnostics',
                type: {intro: IntrospectorTypes.Opt, value: {intro: IntrospectorTypes.Str}} as IIntrospectorGenericType
            },
            {
                name: 'reserved',
                accessName: 'reserved',
                type: {intro: IntrospectorTypes.Map, key: {intro: IntrospectorTypes.Str}, value: {intro: IntrospectorTypes.Str}} as IIntrospectorMapType
            }
        ]
    } as IIntrospectorDataObject
);