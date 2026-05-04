// Auto-generated, any modifications may be overwritten in the future.

// AdtA2 DTO
export class AdtA2  {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain02';
    public static readonly ClassName = 'AdtA2';
    public static readonly FullClassName = 'izumi.test.domain02.AdtA2';

    public getPackageName(): string { return AdtA2.PackageName; }
    public getClassName(): string { return AdtA2.ClassName; }
    public getFullClassName(): string { return AdtA2.FullClassName; }

    private _b: number;

    public get b(): number {
        return this._b;
    }

    public set b(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field b is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field b expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field b is expected to be an integer, got ' + value);
        }

        this._b = value;
    }

    constructor(data: AdtA2Serialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.b = data.b;
    }

    public serialize(): AdtA2Serialized {
        return {
            b: this.b
        };
    }
}

export interface AdtA2Serialized  {
    b: number;
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
Introspector.register(AdtA2.FullClassName, {
        full: AdtA2.FullClassName,
        short: AdtA2.ClassName,
        package: AdtA2.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new AdtA2(),
        fields: [
            {
                name: 'b',
                accessName: 'b',
                type: {intro: IntrospectorTypes.I32}
            }
        ]
    } as IIntrospectorDataObject
);