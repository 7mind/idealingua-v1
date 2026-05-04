// Auto-generated, any modifications may be overwritten in the future.
import {
    JSONLike,
    JSONLikeSerialized,
    JSONLikeHelpers
} from './JSONLike';

// JLArray DTO
export class JLArray  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.json';
    public static readonly ClassName = 'JLArray';
    public static readonly FullClassName = 'idltest.json.JLArray';

    public getPackageName(): string { return JLArray.PackageName; }
    public getClassName(): string { return JLArray.ClassName; }
    public getFullClassName(): string { return JLArray.FullClassName; }

    private _values: JSONLike[];

    public get values(): JSONLike[] {
        return this._values;
    }

    public set values(value: JSONLike[]) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field values is not optional');
        }
        this._values = value;
    }

    constructor(data: JLArraySerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            this.values = [];
            return;
        }

        this.values = data.values.map(e => { return JSONLikeHelpers.deserialize(e); });
    }

    public serialize(): JLArraySerialized {
        return {
            values: this.values.map(e => { return JSONLikeHelpers.serialize(e); })
        };
    }
}

export interface JLArraySerialized  {
    values: {[key: string]: any}[];
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
Introspector.register(JLArray.FullClassName, {
        full: JLArray.FullClassName,
        short: JLArray.ClassName,
        package: JLArray.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new JLArray(),
        fields: [
            {
                name: 'values',
                accessName: 'values',
                type: {intro: IntrospectorTypes.List, value: {intro: IntrospectorTypes.Adt, full: 'idltest.json.JSONLike'} as IIntrospectorUserType} as IIntrospectorGenericType
            }
        ]
    } as IIntrospectorDataObject
);