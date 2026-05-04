// Auto-generated, any modifications may be overwritten in the future.
import {
    TestEnum
} from './TestEnum';

// EnumHolder DTO
export class EnumHolder  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.enums';
    public static readonly ClassName = 'EnumHolder';
    public static readonly FullClassName = 'idltest.enums.EnumHolder';

    public getPackageName(): string { return EnumHolder.PackageName; }
    public getClassName(): string { return EnumHolder.ClassName; }
    public getFullClassName(): string { return EnumHolder.FullClassName; }

    private _en: TestEnum;

    public get en(): TestEnum {
        return this._en;
    }

    public set en(value: TestEnum) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field en is not optional');
        }
        this._en = value;
    }

    constructor(data: EnumHolderSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.en = TestEnum[data.en as keyof typeof TestEnum];
    }

    public serialize(): EnumHolderSerialized {
        return {
            en: TestEnum[this.en]
        };
    }
}

export interface EnumHolderSerialized  {
    en: string;
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
Introspector.register(EnumHolder.FullClassName, {
        full: EnumHolder.FullClassName,
        short: EnumHolder.ClassName,
        package: EnumHolder.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new EnumHolder(),
        fields: [
            {
                name: 'en',
                accessName: 'en',
                type: {intro: IntrospectorTypes.Enum, full: 'idltest.enums.TestEnum'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorDataObject
);