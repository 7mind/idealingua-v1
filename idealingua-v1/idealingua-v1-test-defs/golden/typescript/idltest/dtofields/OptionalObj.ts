// Auto-generated, any modifications may be overwritten in the future.
import {
    NullableObj,
    NullableObjSerialized
} from './NullableObj';

// OptionalObj DTO
export class OptionalObj  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.dtofields';
    public static readonly ClassName = 'OptionalObj';
    public static readonly FullClassName = 'idltest.dtofields.OptionalObj';

    public getPackageName(): string { return OptionalObj.PackageName; }
    public getClassName(): string { return OptionalObj.ClassName; }
    public getFullClassName(): string { return OptionalObj.FullClassName; }

    private _no: NullableObj | undefined;

    public get no(): NullableObj | undefined {
        return this._no;
    }

    public set no(value: NullableObj | undefined) {
        if (typeof value === 'undefined' || value === null) {
            this._no = undefined;
            return;
        }
        this._no = value;
    }

    constructor(data: OptionalObjSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.no = typeof data.no !== 'undefined' ? new NullableObj(data.no) : undefined;
    }

    public serialize(): OptionalObjSerialized {
        return {
            no: typeof this.no !== 'undefined' ? this.no.serialize() : undefined
        };
    }
}

export interface OptionalObjSerialized  {
    no: NullableObjSerialized | undefined;
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
Introspector.register(OptionalObj.FullClassName, {
        full: OptionalObj.FullClassName,
        short: OptionalObj.ClassName,
        package: OptionalObj.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new OptionalObj(),
        fields: [
            {
                name: 'no',
                accessName: 'no',
                type: {intro: IntrospectorTypes.Opt, value: {intro: IntrospectorTypes.Data, full: 'idltest.dtofields.NullableObj'} as IIntrospectorUserType} as IIntrospectorGenericType
            }
        ]
    } as IIntrospectorDataObject
);