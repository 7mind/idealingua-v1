// Auto-generated, any modifications may be overwritten in the future.

// NestedClass DTO
export class NestedClass  {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain01';
    public static readonly ClassName = 'NestedClass';
    public static readonly FullClassName = 'izumi.test.domain01.NestedClass';

    public getPackageName(): string { return NestedClass.PackageName; }
    public getClassName(): string { return NestedClass.ClassName; }
    public getFullClassName(): string { return NestedClass.FullClassName; }

    private _c: NestedClass | undefined;

    public get c(): NestedClass | undefined {
        return this._c;
    }

    public set c(value: NestedClass | undefined) {
        if (typeof value === 'undefined' || value === null) {
            this._c = undefined;
            return;
        }
        this._c = value;
    }

    constructor(data: NestedClassSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.c = typeof data.c !== 'undefined' ? new NestedClass(data.c) : undefined;
    }

    public serialize(): NestedClassSerialized {
        return {
            c: typeof this.c !== 'undefined' ? this.c.serialize() : undefined
        };
    }
}

export interface NestedClassSerialized  {
    c: NestedClassSerialized | undefined;
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
Introspector.register(NestedClass.FullClassName, {
        full: NestedClass.FullClassName,
        short: NestedClass.ClassName,
        package: NestedClass.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new NestedClass(),
        fields: [
            {
                name: 'c',
                accessName: 'c',
                type: {intro: IntrospectorTypes.Opt, value: {intro: IntrospectorTypes.Data, full: 'izumi.test.domain01.NestedClass'} as IIntrospectorUserType} as IIntrospectorGenericType
            }
        ]
    } as IIntrospectorDataObject
);