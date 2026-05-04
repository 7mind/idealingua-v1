// Auto-generated, any modifications may be overwritten in the future.
import {
    NotificationWithB,
    NotificationWithBStruct,
    NotificationWithBStructSerialized
} from './NotificationWithB';
import {
    NotificationWithA,
    NotificationWithAStruct,
    NotificationWithAStructSerialized
} from './NotificationWithA';
import {
    NotificationStruct,
    NotificationStructSerialized
} from './Notification';

// DataWithAB DTO
export class DataWithAB implements NotificationWithA, NotificationWithB  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.inheritance';
    public static readonly ClassName = 'DataWithAB';
    public static readonly FullClassName = 'idltest.inheritance.DataWithAB';

    public getPackageName(): string { return DataWithAB.PackageName; }
    public getClassName(): string { return DataWithAB.ClassName; }
    public getFullClassName(): string { return DataWithAB.FullClassName; }

    constructor(data: DataWithABSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

    }

    public toNotificationWithASerialized(): NotificationWithAStructSerialized {
        return {
        };
    }

    public toNotificationWithA(): NotificationWithAStruct {
        return new NotificationWithAStruct(this.toNotificationWithASerialized());
    }

    public toNotificationSerialized(): NotificationStructSerialized {
        return {
        };
    }

    public toNotification(): NotificationStruct {
        return new NotificationStruct(this.toNotificationSerialized());
    }

    public toNotificationWithBSerialized(): NotificationWithBStructSerialized {
        return {
        };
    }

    public toNotificationWithB(): NotificationWithBStruct {
        return new NotificationWithBStruct(this.toNotificationWithBSerialized());
    }

    public loadNotificationWithASerialized(slice: NotificationWithAStructSerialized) {
    }

    public loadNotificationWithA(slice: NotificationWithAStruct) {
        this.loadNotificationWithASerialized(slice.serialize());
    }

    public loadNotificationSerialized(slice: NotificationStructSerialized) {
    }

    public loadNotification(slice: NotificationStruct) {
        this.loadNotificationSerialized(slice.serialize());
    }

    public loadNotificationWithBSerialized(slice: NotificationWithBStructSerialized) {
    }

    public loadNotificationWithB(slice: NotificationWithBStruct) {
        this.loadNotificationWithBSerialized(slice.serialize());
    }

    public serialize(): DataWithABSerialized {
        return {
        };
    }
}

export interface DataWithABSerialized extends NotificationWithAStructSerialized, NotificationWithBStructSerialized  {
}

NotificationWithAStruct.register(DataWithAB.FullClassName, DataWithAB);
NotificationStruct.register(DataWithAB.FullClassName, DataWithAB);
NotificationWithBStruct.register(DataWithAB.FullClassName, DataWithAB);

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorDataObject
} from '../../irt';
Introspector.register(DataWithAB.FullClassName, {
        full: DataWithAB.FullClassName,
        short: DataWithAB.ClassName,
        package: DataWithAB.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new DataWithAB(),
        fields: [

        ]
    } as IIntrospectorDataObject
);