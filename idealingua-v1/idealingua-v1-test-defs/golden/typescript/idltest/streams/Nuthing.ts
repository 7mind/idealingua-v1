// Auto-generated, any modifications may be overwritten in the future.

// Nuthing DTO
export class Nuthing  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.streams';
    public static readonly ClassName = 'Nuthing';
    public static readonly FullClassName = 'idltest.streams.Nuthing';

    public getPackageName(): string { return Nuthing.PackageName; }
    public getClassName(): string { return Nuthing.ClassName; }
    public getFullClassName(): string { return Nuthing.FullClassName; }

    constructor(data: NuthingSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

    }

    public serialize(): NuthingSerialized {
        return {
        };
    }
}

export interface NuthingSerialized  {
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
Introspector.register(Nuthing.FullClassName, {
        full: Nuthing.FullClassName,
        short: Nuthing.ClassName,
        package: Nuthing.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new Nuthing(),
        fields: [

        ]
    } as IIntrospectorDataObject
);