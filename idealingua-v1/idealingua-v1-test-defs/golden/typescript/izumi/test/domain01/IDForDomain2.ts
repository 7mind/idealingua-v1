// Auto-generated, any modifications may be overwritten in the future.

export class IDForDomain2 implements IIDForDomain2 {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain01';
    public static readonly ClassName = 'IDForDomain2';
    public static readonly FullClassName = 'izumi.test.domain01.IDForDomain2';

    public getPackageName(): string { return IDForDomain2.PackageName; }
    public getClassName(): string { return IDForDomain2.ClassName; }
    public getFullClassName(): string { return IDForDomain2.FullClassName; }

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

    constructor(data: string | IIDForDomain2 = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        if (typeof data === 'string') {
            if (!data.startsWith('IDForDomain2#')) {
                throw new Error('Identifier must start with IDForDomain2, got ' + data);
            }
            const parts = data.substr(data.indexOf('#') + 1).split(':');
            this.a = parseInt(decodeURIComponent(parts[0]), 10);
        } else {
            this.a = data.a;
        }
    }

    public toString(): string {
        const suffix = encodeURIComponent(this.a.toString());
        return 'IDForDomain2#' + suffix;
    }

    public serialize(): string {
        return this.toString();
    }
}

export interface IIDForDomain2 {
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
Introspector.register(IDForDomain2.FullClassName, {
        full: IDForDomain2.FullClassName,
        short: IDForDomain2.ClassName,
        package: IDForDomain2.PackageName,
        type: IntrospectorTypes.Id,
        ctor: () => new IDForDomain2(),
        fields: [
            {
                name: 'a',
                accessName: 'a',
                type: {intro: IntrospectorTypes.I32}
            }
        ]
    } as IIntrospectorIdObject
);