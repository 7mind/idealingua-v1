// Auto-generated, any modifications may be overwritten in the future.

// BranchB DTO
export class BranchB  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.events';
    public static readonly ClassName = 'BranchB';
    public static readonly FullClassName = 'idltest.events.BranchB';

    public getPackageName(): string { return BranchB.PackageName; }
    public getClassName(): string { return BranchB.ClassName; }
    public getFullClassName(): string { return BranchB.FullClassName; }

    private _b: string;

    public get b(): string {
        return this._b;
    }

    public set b(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field b is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field b expects type string, got ' + value);
        }

        this._b = value;
    }

    constructor(data: BranchBSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.b = data.b;
    }

    public serialize(): BranchBSerialized {
        return {
            b: this.b
        };
    }
}

export interface BranchBSerialized  {
    b: string;
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
Introspector.register(BranchB.FullClassName, {
        full: BranchB.FullClassName,
        short: BranchB.ClassName,
        package: BranchB.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new BranchB(),
        fields: [
            {
                name: 'b',
                accessName: 'b',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);