// Auto-generated, any modifications may be overwritten in the future.

// Failure DTO
export class Failure  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.algebraics';
    public static readonly ClassName = 'Failure';
    public static readonly FullClassName = 'idltest.algebraics.Failure';

    public getPackageName(): string { return Failure.PackageName; }
    public getClassName(): string { return Failure.ClassName; }
    public getFullClassName(): string { return Failure.FullClassName; }

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

        if (value < -128) {
            throw new Error('Field code is expected to be not less than -128, got ' + value);
        }

        if (value > 127) {
            throw new Error('Field code is expected to be not greater than 127, got ' + value);
        }

        this._code = value;
    }

    constructor(data: FailureSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.code = data.code;
    }

    public serialize(): FailureSerialized {
        return {
            code: this.code
        };
    }
}

export interface FailureSerialized  {
    code: number;
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
Introspector.register(Failure.FullClassName, {
        full: Failure.FullClassName,
        short: Failure.ClassName,
        package: Failure.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new Failure(),
        fields: [
            {
                name: 'code',
                accessName: 'code',
                type: {intro: IntrospectorTypes.I08}
            }
        ]
    } as IIntrospectorDataObject
);