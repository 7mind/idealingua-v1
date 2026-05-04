// Auto-generated, any modifications may be overwritten in the future.

// BranchA DTO
export class BranchA  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.events';
    public static readonly ClassName = 'BranchA';
    public static readonly FullClassName = 'idltest.events.BranchA';

    public getPackageName(): string { return BranchA.PackageName; }
    public getClassName(): string { return BranchA.ClassName; }
    public getFullClassName(): string { return BranchA.FullClassName; }

    private _a: string;

    public get a(): string {
        return this._a;
    }

    public set a(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field a is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field a expects type string, got ' + value);
        }

        this._a = value;
    }

    constructor(data: BranchASerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.a = data.a;
    }

    public serialize(): BranchASerialized {
        return {
            a: this.a
        };
    }
}

export interface BranchASerialized  {
    a: string;
}

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorDataObject
} from '../../irt';
Introspector.register(BranchA.FullClassName, {
        full: BranchA.FullClassName,
        short: BranchA.ClassName,
        package: BranchA.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new BranchA(),
        fields: [
            {
                name: 'a',
                accessName: 'a',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);