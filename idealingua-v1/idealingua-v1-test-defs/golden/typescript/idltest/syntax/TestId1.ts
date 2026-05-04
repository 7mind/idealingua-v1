// Auto-generated, any modifications may be overwritten in the future.

export class TestId1 implements ITestId1 {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.syntax';
    public static readonly ClassName = 'TestId1';
    public static readonly FullClassName = 'idltest.syntax.TestId1';

    public getPackageName(): string { return TestId1.PackageName; }
    public getClassName(): string { return TestId1.ClassName; }
    public getFullClassName(): string { return TestId1.FullClassName; }

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

    constructor(data: string | ITestId1 = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        if (typeof data === 'string') {
            if (!data.startsWith('TestId1#')) {
                throw new Error('Identifier must start with TestId1, got ' + data);
            }
            const parts = data.substr(data.indexOf('#') + 1).split(':');
            this.value = decodeURIComponent(parts[0]);
        } else {
            this.value = data.value;
        }
    }

    public toString(): string {
        const suffix = encodeURIComponent(this.value);
        return 'TestId1#' + suffix;
    }

    public serialize(): string {
        return this.toString();
    }
}

export interface ITestId1 {
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
Introspector.register(TestId1.FullClassName, {
        full: TestId1.FullClassName,
        short: TestId1.ClassName,
        package: TestId1.PackageName,
        type: IntrospectorTypes.Id,
        ctor: () => new TestId1(),
        fields: [
            {
                name: 'value',
                accessName: 'value',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorIdObject
);