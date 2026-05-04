// Auto-generated, any modifications may be overwritten in the future.

// JLNull DTO
export class JLNull  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.json';
    public static readonly ClassName = 'JLNull';
    public static readonly FullClassName = 'idltest.json.JLNull';

    public getPackageName(): string { return JLNull.PackageName; }
    public getClassName(): string { return JLNull.ClassName; }
    public getFullClassName(): string { return JLNull.FullClassName; }

    constructor(data: JLNullSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

    }

    public serialize(): JLNullSerialized {
        return {
        };
    }
}

export interface JLNullSerialized  {
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
Introspector.register(JLNull.FullClassName, {
        full: JLNull.FullClassName,
        short: JLNull.ClassName,
        package: JLNull.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new JLNull(),
        fields: [

        ]
    } as IIntrospectorDataObject
);