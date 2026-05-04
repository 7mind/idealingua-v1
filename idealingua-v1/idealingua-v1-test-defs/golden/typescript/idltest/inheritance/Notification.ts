// Auto-generated, any modifications may be overwritten in the future.

// Notification Interface
export interface Notification {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): NotificationStructSerialized;
}
export interface NotificationStructSerialized {
}

export class NotificationStruct implements Notification {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.inheritance.Notification';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.inheritance.Notification.Struct';

    public getPackageName(): string { return NotificationStruct.PackageName; }
    public getClassName(): string { return NotificationStruct.ClassName; }
    public getFullClassName(): string { return NotificationStruct.FullClassName; }

    constructor(data: NotificationStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

    }

    public serialize(): NotificationStructSerialized {
        return {
        };
    }

    // Polymorphic section below. If a new type to be registered, use NotificationStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: NotificationStruct| NotificationStructSerialized): Notification}} = {
        // This basic registration will happen below [NotificationStruct.FullClassName]: NotificationStruct
    };

    public static register(className: string, ctor: {new (data?: NotificationStruct| NotificationStructSerialized): Notification}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: NotificationStructSerialized}): Notification {
        const polymorphicId = Object.keys(data)[0];
        const ctor = NotificationStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for NotificationStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(NotificationStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in NotificationStruct._knownPolymorphic;
    }
}

NotificationStruct.register(NotificationStruct.FullClassName, NotificationStruct);

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorMixinObject,
    IIntrospectorDataObject
} from '../../irt';
Introspector.register('idltest.inheritance.Notification', {
        full: 'idltest.inheritance.Notification',
        short: 'Notification',
        package: 'idltest.inheritance',
        type: IntrospectorTypes.Mixin,
        ctor: () => new NotificationStruct(),
        fields: [

        ],
        implementations: NotificationStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(NotificationStruct.FullClassName, {
        full: NotificationStruct.FullClassName,
        short: NotificationStruct.ClassName,
        package: NotificationStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new NotificationStruct(),
        fields: [

        ]
    } as IIntrospectorDataObject
);