// Auto-generated, any modifications may be overwritten in the future.
import {
    AdtTestID
} from './AdtTestID';

// ComplexAdt DTO
export class ComplexAdt  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.algebraics';
    public static readonly ClassName = 'ComplexAdt';
    public static readonly FullClassName = 'idltest.algebraics.ComplexAdt';

    public getPackageName(): string { return ComplexAdt.PackageName; }
    public getClassName(): string { return ComplexAdt.ClassName; }
    public getFullClassName(): string { return ComplexAdt.FullClassName; }

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

    constructor(data: ComplexAdtSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.id = new AdtTestID(data.id);
    }

    public serialize(): ComplexAdtSerialized {
        return {
            id: this.id.serialize()
        };
    }
}

export interface ComplexAdtSerialized  {
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
Introspector.register(ComplexAdt.FullClassName, {
        full: ComplexAdt.FullClassName,
        short: ComplexAdt.ClassName,
        package: ComplexAdt.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new ComplexAdt(),
        fields: [
            {
                name: 'id',
                accessName: 'id',
                type: {intro: IntrospectorTypes.Id, full: 'idltest.algebraics.AdtTestID'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorDataObject
);