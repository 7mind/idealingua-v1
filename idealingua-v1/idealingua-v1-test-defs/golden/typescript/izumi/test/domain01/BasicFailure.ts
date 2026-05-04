// Auto-generated, any modifications may be overwritten in the future.
import {
    CommonFailure,
    CommonFailureStruct,
    CommonFailureStructSerialized
} from './CommonFailure';

// BasicFailure DTO
export class BasicFailure implements CommonFailure  {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain01';
    public static readonly ClassName = 'BasicFailure';
    public static readonly FullClassName = 'izumi.test.domain01.BasicFailure';

    public getPackageName(): string { return BasicFailure.PackageName; }
    public getClassName(): string { return BasicFailure.ClassName; }
    public getFullClassName(): string { return BasicFailure.FullClassName; }

    private _code: number;

    public get code(): number {
        return this._code;
    }

    public set code(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field code is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field code expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field code is expected to be an integer, got ' + value);
        }

        this._code = value;
    }

    constructor(data: BasicFailureSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.code = data.code;
    }

    public toCommonFailureSerialized(): CommonFailureStructSerialized {
        return {
            code: this.code
        };
    }

    public toCommonFailure(): CommonFailureStruct {
        return new CommonFailureStruct(this.toCommonFailureSerialized());
    }

    public loadCommonFailureSerialized(slice: CommonFailureStructSerialized) {
        this.code = slice.code;
    }

    public loadCommonFailure(slice: CommonFailureStruct) {
        this.loadCommonFailureSerialized(slice.serialize());
    }

    public serialize(): BasicFailureSerialized {
        return {
            code: this.code
        };
    }
}

export interface BasicFailureSerialized extends CommonFailureStructSerialized  {
    code: number;
}

CommonFailureStruct.register(BasicFailure.FullClassName, BasicFailure);

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorDataObject
} from '../../../irt';
Introspector.register(BasicFailure.FullClassName, {
        full: BasicFailure.FullClassName,
        short: BasicFailure.ClassName,
        package: BasicFailure.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new BasicFailure(),
        fields: [
            {
                name: 'code',
                accessName: 'code',
                type: {intro: IntrospectorTypes.I32}
            }
        ]
    } as IIntrospectorDataObject
);