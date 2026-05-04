// Auto-generated, any modifications may be overwritten in the future.

// Success DTO
export class Success  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.algebraics';
    public static readonly ClassName = 'Success';
    public static readonly FullClassName = 'idltest.algebraics.Success';

    public getPackageName(): string { return Success.PackageName; }
    public getClassName(): string { return Success.ClassName; }
    public getFullClassName(): string { return Success.FullClassName; }

    private _message: string;

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

    constructor(data: SuccessSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.message = data.message;
    }

    public serialize(): SuccessSerialized {
        return {
            message: this.message
        };
    }
}

export interface SuccessSerialized  {
    message: string;
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
Introspector.register(Success.FullClassName, {
        full: Success.FullClassName,
        short: Success.ClassName,
        package: Success.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new Success(),
        fields: [
            {
                name: 'message',
                accessName: 'message',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);