// Auto-generated, any modifications may be overwritten in the future.

export class TestValIdentifier implements ITestValIdentifier {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain01';
    public static readonly ClassName = 'TestValIdentifier';
    public static readonly FullClassName = 'izumi.test.domain01.TestValIdentifier';

    public getPackageName(): string { return TestValIdentifier.PackageName; }
    public getClassName(): string { return TestValIdentifier.ClassName; }
    public getFullClassName(): string { return TestValIdentifier.FullClassName; }

    private _userId: string;

    public get userId(): string {
        return this._userId;
    }

    public set userId(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field userId is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field userId expects type string, got ' + value);
        }

        this._userId = value;
    }

    constructor(data: string | ITestValIdentifier = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        if (typeof data === 'string') {
            if (!data.startsWith('TestValIdentifier#')) {
                throw new Error('Identifier must start with TestValIdentifier, got ' + data);
            }
            const parts = data.substr(data.indexOf('#') + 1).split(':');
            this.userId = decodeURIComponent(parts[0]);
        } else {
            this.userId = data.userId;
        }
    }

    public toString(): string {
        const suffix = encodeURIComponent(this.userId);
        return 'TestValIdentifier#' + suffix;
    }

    public serialize(): string {
        return this.toString();
    }
}

export interface ITestValIdentifier {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): string;

    userId: string;
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
Introspector.register(TestValIdentifier.FullClassName, {
        full: TestValIdentifier.FullClassName,
        short: TestValIdentifier.ClassName,
        package: TestValIdentifier.PackageName,
        type: IntrospectorTypes.Id,
        ctor: () => new TestValIdentifier(),
        fields: [
            {
                name: 'userId',
                accessName: 'userId',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorIdObject
);