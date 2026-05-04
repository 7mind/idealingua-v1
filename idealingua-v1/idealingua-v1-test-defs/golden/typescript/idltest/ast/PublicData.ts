// Auto-generated, any modifications may be overwritten in the future.

// PublicData DTO
export class PublicData  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.ast';
    public static readonly ClassName = 'PublicData';
    public static readonly FullClassName = 'idltest.ast.PublicData';

    public getPackageName(): string { return PublicData.PackageName; }
    public getClassName(): string { return PublicData.ClassName; }
    public getFullClassName(): string { return PublicData.FullClassName; }

    constructor(data: PublicDataSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

    }

    public serialize(): PublicDataSerialized {
        return {
        };
    }
}

export interface PublicDataSerialized  {
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
Introspector.register(PublicData.FullClassName, {
        full: PublicData.FullClassName,
        short: PublicData.ClassName,
        package: PublicData.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new PublicData(),
        fields: [

        ]
    } as IIntrospectorDataObject
);