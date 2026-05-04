// Auto-generated, any modifications may be overwritten in the future.

// Adt2 DTO
export class Adt2  {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain02';
    public static readonly ClassName = 'Adt2';
    public static readonly FullClassName = 'izumi.test.domain02.Adt2';

    public getPackageName(): string { return Adt2.PackageName; }
    public getClassName(): string { return Adt2.ClassName; }
    public getFullClassName(): string { return Adt2.FullClassName; }

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

    constructor(data: Adt2Serialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.b = data.b;
    }

    public serialize(): Adt2Serialized {
        return {
            b: this.b
        };
    }
}

export interface Adt2Serialized  {
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
Introspector.register(Adt2.FullClassName, {
        full: Adt2.FullClassName,
        short: Adt2.ClassName,
        package: Adt2.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new Adt2(),
        fields: [
            {
                name: 'b',
                accessName: 'b',
                type: {intro: IntrospectorTypes.I32}
            }
        ]
    } as IIntrospectorDataObject
);