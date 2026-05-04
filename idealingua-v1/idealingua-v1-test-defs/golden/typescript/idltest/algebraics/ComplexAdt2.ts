// Auto-generated, any modifications may be overwritten in the future.
import {
    AdtTestID
} from './AdtTestID';

// ComplexAdt2 DTO
export class ComplexAdt2  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.algebraics';
    public static readonly ClassName = 'ComplexAdt2';
    public static readonly FullClassName = 'idltest.algebraics.ComplexAdt2';

    public getPackageName(): string { return ComplexAdt2.PackageName; }
    public getClassName(): string { return ComplexAdt2.ClassName; }
    public getFullClassName(): string { return ComplexAdt2.FullClassName; }

    private _id: AdtTestID;

    public get id(): AdtTestID {
        return this._id;
    }

    public set id(value: AdtTestID) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field id is not optional');
        }
        this._id = value;
    }

    constructor(data: ComplexAdt2Serialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.id = new AdtTestID(data.id);
    }

    public serialize(): ComplexAdt2Serialized {
        return {
            id: this.id.serialize()
        };
    }
}

export interface ComplexAdt2Serialized  {
    id: string;
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
Introspector.register(ComplexAdt2.FullClassName, {
        full: ComplexAdt2.FullClassName,
        short: ComplexAdt2.ClassName,
        package: ComplexAdt2.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new ComplexAdt2(),
        fields: [
            {
                name: 'id',
                accessName: 'id',
                type: {intro: IntrospectorTypes.Id, full: 'idltest.algebraics.AdtTestID'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorDataObject
);