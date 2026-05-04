// Auto-generated, any modifications may be overwritten in the future.
import {
    JSONLike,
    JSONLikeSerialized,
    JSONLikeHelpers
} from './JSONLike';

// JLObject DTO
export class JLObject  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.json';
    public static readonly ClassName = 'JLObject';
    public static readonly FullClassName = 'idltest.json.JLObject';

    public getPackageName(): string { return JLObject.PackageName; }
    public getClassName(): string { return JLObject.ClassName; }
    public getFullClassName(): string { return JLObject.FullClassName; }

    private _fields: {[key: string]: JSONLike};

    public get fields(): {[key: string]: JSONLike} {
        return this._fields;
    }

    public set fields(value: {[key: string]: JSONLike}) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field fields is not optional');
        }
        this._fields = value;
    }

    constructor(data: JLObjectSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            this.fields = {};
            return;
        }

        this.fields = Object.keys(data.fields).reduce<any>((previous, current) => {previous[current] = JSONLikeHelpers.deserialize(data.fields[current as any]); return previous; }, {});
    }

    public serialize(): JLObjectSerialized {
        return {
            fields: Object.keys(this.fields).reduce<any>((previous, current) => {previous[current] = JSONLikeHelpers.serialize(this.fields[current as any]); return previous; }, {})
        };
    }
}

export interface JLObjectSerialized  {
    fields: {[key: string]: {[key: string]: any}};
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
Introspector.register(JLObject.FullClassName, {
        full: JLObject.FullClassName,
        short: JLObject.ClassName,
        package: JLObject.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new JLObject(),
        fields: [
            {
                name: 'fields',
                accessName: 'fields',
                type: {intro: IntrospectorTypes.Map, key: {intro: IntrospectorTypes.Str}, value: {intro: IntrospectorTypes.Adt, full: 'idltest.json.JSONLike'} as IIntrospectorUserType} as IIntrospectorMapType
            }
        ]
    } as IIntrospectorDataObject
);