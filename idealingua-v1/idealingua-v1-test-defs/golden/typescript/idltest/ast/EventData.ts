// Auto-generated, any modifications may be overwritten in the future.

// EventData DTO
export class EventData  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.ast';
    public static readonly ClassName = 'EventData';
    public static readonly FullClassName = 'idltest.ast.EventData';

    public getPackageName(): string { return EventData.PackageName; }
    public getClassName(): string { return EventData.ClassName; }
    public getFullClassName(): string { return EventData.FullClassName; }

    constructor(data: EventDataSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

    }

    public serialize(): EventDataSerialized {
        return {
        };
    }
}

export interface EventDataSerialized  {
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
Introspector.register(EventData.FullClassName, {
        full: EventData.FullClassName,
        short: EventData.ClassName,
        package: EventData.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new EventData(),
        fields: [

        ]
    } as IIntrospectorDataObject
);