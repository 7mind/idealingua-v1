// Auto-generated, any modifications may be overwritten in the future.

// AdtA1 DTO
export class AdtA1  {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain02';
    public static readonly ClassName = 'AdtA1';
    public static readonly FullClassName = 'izumi.test.domain02.AdtA1';

    public getPackageName(): string { return AdtA1.PackageName; }
    public getClassName(): string { return AdtA1.ClassName; }
    public getFullClassName(): string { return AdtA1.FullClassName; }

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

    constructor(data: AdtA1Serialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.a = data.a;
    }

    public serialize(): AdtA1Serialized {
        return {
            a: this.a
        };
    }
}

export interface AdtA1Serialized  {
    a: number;
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
Introspector.register(AdtA1.FullClassName, {
        full: AdtA1.FullClassName,
        short: AdtA1.ClassName,
        package: AdtA1.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new AdtA1(),
        fields: [
            {
                name: 'a',
                accessName: 'a',
                type: {intro: IntrospectorTypes.I32}
            }
        ]
    } as IIntrospectorDataObject
);