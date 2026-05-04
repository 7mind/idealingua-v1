// Auto-generated, any modifications may be overwritten in the future.
import {
    BucketID
} from './BucketID';

// KVIDGeneric DTO
export class KVIDGeneric  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.identifiers';
    public static readonly ClassName = 'KVIDGeneric';
    public static readonly FullClassName = 'idltest.identifiers.KVIDGeneric';

    public getPackageName(): string { return KVIDGeneric.PackageName; }
    public getClassName(): string { return KVIDGeneric.ClassName; }
    public getFullClassName(): string { return KVIDGeneric.FullClassName; }

    private _test: {[key: string]: BucketID};

    public get test(): {[key: string]: BucketID} {
        return this._test;
    }

    public set test(value: {[key: string]: BucketID}) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field test is not optional');
        }
        this._test = value;
    }

    constructor(data: KVIDGenericSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            this.test = {};
            return;
        }

        this.test = Object.keys(data.test).reduce<any>((previous, current) => {previous[current] = new BucketID(data.test[current as any]); return previous; }, {});
    }

    public serialize(): KVIDGenericSerialized {
        return {
            test: Object.keys(this.test).reduce<any>((previous, current) => {previous[current] = this.test[current as any].serialize(); return previous; }, {})
        };
    }
}

export interface KVIDGenericSerialized  {
    test: {[key: string]: string};
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
Introspector.register(KVIDGeneric.FullClassName, {
        full: KVIDGeneric.FullClassName,
        short: KVIDGeneric.ClassName,
        package: KVIDGeneric.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new KVIDGeneric(),
        fields: [
            {
                name: 'test',
                accessName: 'test',
                type: {intro: IntrospectorTypes.Map, key: {intro: IntrospectorTypes.Str}, value: {intro: IntrospectorTypes.Id, full: 'idltest.identifiers.BucketID'} as IIntrospectorUserType} as IIntrospectorMapType
            }
        ]
    } as IIntrospectorDataObject
);