// Auto-generated, any modifications may be overwritten in the future.
import {
    TestEnum
} from './TestEnum';

// KVEnumGeneric DTO
export class KVEnumGeneric  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.enums';
    public static readonly ClassName = 'KVEnumGeneric';
    public static readonly FullClassName = 'idltest.enums.KVEnumGeneric';

    public getPackageName(): string { return KVEnumGeneric.PackageName; }
    public getClassName(): string { return KVEnumGeneric.ClassName; }
    public getFullClassName(): string { return KVEnumGeneric.FullClassName; }

    private _test: {[key: string]: TestEnum};

    public get test(): {[key: string]: TestEnum} {
        return this._test;
    }

    public set test(value: {[key: string]: TestEnum}) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field test is not optional');
        }
        this._test = value;
    }

    constructor(data: KVEnumGenericSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            this.test = {};
            return;
        }

        this.test = Object.keys(data.test).reduce<any>((previous, current) => {previous[current] = TestEnum[data.test[current as any] as keyof typeof TestEnum]; return previous; }, {});
    }

    public serialize(): KVEnumGenericSerialized {
        return {
            test: Object.keys(this.test).reduce<any>((previous, current) => {previous[current] = TestEnum[this.test[current as any]]; return previous; }, {})
        };
    }
}

export interface KVEnumGenericSerialized  {
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
Introspector.register(KVEnumGeneric.FullClassName, {
        full: KVEnumGeneric.FullClassName,
        short: KVEnumGeneric.ClassName,
        package: KVEnumGeneric.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new KVEnumGeneric(),
        fields: [
            {
                name: 'test',
                accessName: 'test',
                type: {intro: IntrospectorTypes.Map, key: {intro: IntrospectorTypes.Str}, value: {intro: IntrospectorTypes.Enum, full: 'idltest.enums.TestEnum'} as IIntrospectorUserType} as IIntrospectorMapType
            }
        ]
    } as IIntrospectorDataObject
);