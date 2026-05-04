// Auto-generated, any modifications may be overwritten in the future.
import {
    NullableObj,
    NullableObjSerialized
} from './NullableObj';

// ListObj DTO
export class ListObj  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.dtofields';
    public static readonly ClassName = 'ListObj';
    public static readonly FullClassName = 'idltest.dtofields.ListObj';

    public getPackageName(): string { return ListObj.PackageName; }
    public getClassName(): string { return ListObj.ClassName; }
    public getFullClassName(): string { return ListObj.FullClassName; }

    private _all: NullableObj[];

    public get all(): NullableObj[] {
        return this._all;
    }

    public set all(value: NullableObj[]) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field all is not optional');
        }
        this._all = value;
    }

    constructor(data: ListObjSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            this.all = [];
            return;
        }

        this.all = data.all.map(e => { return new NullableObj(e); });
    }

    public serialize(): ListObjSerialized {
        return {
            all: this.all.map(e => { return e.serialize(); })
        };
    }
}

export interface ListObjSerialized  {
    all: NullableObjSerialized[];
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
Introspector.register(ListObj.FullClassName, {
        full: ListObj.FullClassName,
        short: ListObj.ClassName,
        package: ListObj.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new ListObj(),
        fields: [
            {
                name: 'all',
                accessName: 'all',
                type: {intro: IntrospectorTypes.List, value: {intro: IntrospectorTypes.Data, full: 'idltest.dtofields.NullableObj'} as IIntrospectorUserType} as IIntrospectorGenericType
            }
        ]
    } as IIntrospectorDataObject
);