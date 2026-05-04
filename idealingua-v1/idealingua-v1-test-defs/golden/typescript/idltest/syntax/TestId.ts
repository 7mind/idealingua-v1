// Auto-generated, any modifications may be overwritten in the future.

export class TestId implements ITestId {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.syntax';
    public static readonly ClassName = 'TestId';
    public static readonly FullClassName = 'idltest.syntax.TestId';

    public getPackageName(): string { return TestId.PackageName; }
    public getClassName(): string { return TestId.ClassName; }
    public getFullClassName(): string { return TestId.FullClassName; }

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

    constructor(data: string | ITestId = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        if (typeof data === 'string') {
            if (!data.startsWith('TestId#')) {
                throw new Error('Identifier must start with TestId, got ' + data);
            }
            const parts = data.substr(data.indexOf('#') + 1).split(':');
            this.value = decodeURIComponent(parts[0]);
        } else {
            this.value = data.value;
        }
    }

    public toString(): string {
        const suffix = encodeURIComponent(this.value);
        return 'TestId#' + suffix;
    }

    public serialize(): string {
        return this.toString();
    }
}

export interface ITestId {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): string;

    value: string;
}

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorIdObject
} from '../../irt';
Introspector.register(TestId.FullClassName, {
        full: TestId.FullClassName,
        short: TestId.ClassName,
        package: TestId.PackageName,
        type: IntrospectorTypes.Id,
        ctor: () => new TestId(),
        fields: [
            {
                name: 'value',
                accessName: 'value',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorIdObject
);