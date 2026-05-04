// Auto-generated, any modifications may be overwritten in the future.

export class TestIdentifier implements ITestIdentifier {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain01';
    public static readonly ClassName = 'TestIdentifier';
    public static readonly FullClassName = 'izumi.test.domain01.TestIdentifier';

    public getPackageName(): string { return TestIdentifier.PackageName; }
    public getClassName(): string { return TestIdentifier.ClassName; }
    public getFullClassName(): string { return TestIdentifier.FullClassName; }

    private _userId: string;
    private _context: string;
    private _userType: number;

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

    public get context(): string {
        return this._context;
    }

    public set context(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field context is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field context expects type string, got ' + value);
        }

        this._context = value;
    }

    public get userType(): number {
        return this._userType;
    }

    public set userType(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field userType is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field userType expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field userType is expected to be an integer, got ' + value);
        }

        if (value < -128) {
            throw new Error('Field userType is expected to be not less than -128, got ' + value);
        }

        if (value > 127) {
            throw new Error('Field userType is expected to be not greater than 127, got ' + value);
        }

        this._userType = value;
    }

    constructor(data: string | ITestIdentifier = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        if (typeof data === 'string') {
            if (!data.startsWith('TestIdentifier#')) {
                throw new Error('Identifier must start with TestIdentifier, got ' + data);
            }
            const parts = data.substr(data.indexOf('#') + 1).split(':');
            this.context = decodeURIComponent(parts[0]);
            this.userId = decodeURIComponent(parts[1]);
            this.userType = parseInt(decodeURIComponent(parts[2]), 10);
        } else {
            this.userId = data.userId;
            this.context = data.context;
            this.userType = data.userType;
        }
    }

    public toString(): string {
        const suffix = encodeURIComponent(this.context) + ':' + encodeURIComponent(this.userId) + ':' + encodeURIComponent(this.userType.toString());
        return 'TestIdentifier#' + suffix;
    }

    public serialize(): string {
        return this.toString();
    }
}

export interface ITestIdentifier {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): string;

    userId: string;
    context: string;
    userType: number;
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
Introspector.register(TestIdentifier.FullClassName, {
        full: TestIdentifier.FullClassName,
        short: TestIdentifier.ClassName,
        package: TestIdentifier.PackageName,
        type: IntrospectorTypes.Id,
        ctor: () => new TestIdentifier(),
        fields: [
            {
                name: 'userId',
                accessName: 'userId',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'context',
                accessName: 'context',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'userType',
                accessName: 'userType',
                type: {intro: IntrospectorTypes.I08}
            }
        ]
    } as IIntrospectorIdObject
);