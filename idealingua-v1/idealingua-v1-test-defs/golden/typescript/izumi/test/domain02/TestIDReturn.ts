// Auto-generated, any modifications may be overwritten in the future.

export class TestIDReturn implements ITestIDReturn {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain02';
    public static readonly ClassName = 'TestIDReturn';
    public static readonly FullClassName = 'izumi.test.domain02.TestIDReturn';

    public getPackageName(): string { return TestIDReturn.PackageName; }
    public getClassName(): string { return TestIDReturn.ClassName; }
    public getFullClassName(): string { return TestIDReturn.FullClassName; }

    private _a: number;

    public get a(): number {
        return this._a;
    }

    public set a(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field a is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field a expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field a is expected to be an integer, got ' + value);
        }

        this._a = value;
    }

    constructor(data: string | ITestIDReturn = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        if (typeof data === 'string') {
            if (!data.startsWith('TestIDReturn#')) {
                throw new Error('Identifier must start with TestIDReturn, got ' + data);
            }
            const parts = data.substr(data.indexOf('#') + 1).split(':');
            this.a = parseInt(decodeURIComponent(parts[0]), 10);
        } else {
            this.a = data.a;
        }
    }

    public toString(): string {
        const suffix = encodeURIComponent(this.a.toString());
        return 'TestIDReturn#' + suffix;
    }

    public serialize(): string {
        return this.toString();
    }
}

export interface ITestIDReturn {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): string;

    a: number;
}

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorIdObject
} from '../../../irt';
Introspector.register(TestIDReturn.FullClassName, {
        full: TestIDReturn.FullClassName,
        short: TestIDReturn.ClassName,
        package: TestIDReturn.PackageName,
        type: IntrospectorTypes.Id,
        ctor: () => new TestIDReturn(),
        fields: [
            {
                name: 'a',
                accessName: 'a',
                type: {intro: IntrospectorTypes.I32}
            }
        ]
    } as IIntrospectorIdObject
);