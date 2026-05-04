// Auto-generated, any modifications may be overwritten in the future.
import {
    Notification,
    NotificationStruct,
    NotificationStructSerialized
} from './Notification';

// NotificationWithB Interface
export interface NotificationWithB extends Notification {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): NotificationWithBStructSerialized;
}
export interface NotificationWithBStructSerialized extends NotificationStructSerialized {
}

export class NotificationWithBStruct implements NotificationWithB {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.inheritance.NotificationWithB';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.inheritance.NotificationWithB.Struct';

    public getPackageName(): string { return NotificationWithBStruct.PackageName; }
    public getClassName(): string { return NotificationWithBStruct.ClassName; }
    public getFullClassName(): string { return NotificationWithBStruct.FullClassName; }

    constructor(data: NotificationWithBStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

    }

    public serialize(): NotificationWithBStructSerialized {
        return {
        };
    }

    // Polymorphic section below. If a new type to be registered, use NotificationWithBStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: NotificationWithBStruct| NotificationWithBStructSerialized): NotificationWithB}} = {
        // This basic registration will happen below [NotificationWithBStruct.FullClassName]: NotificationWithBStruct
    };

    public static register(className: string, ctor: {new (data?: NotificationWithBStruct| NotificationWithBStructSerialized): NotificationWithB}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: NotificationWithBStructSerialized}): NotificationWithB {
        const polymorphicId = Object.keys(data)[0];
        const ctor = NotificationWithBStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for NotificationWithBStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(NotificationWithBStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in NotificationWithBStruct._knownPolymorphic;
    }
}

NotificationWithBStruct.register(NotificationWithBStruct.FullClassName, NotificationWithBStruct);
NotificationStruct.register(NotificationWithBStruct.FullClassName, NotificationWithBStruct);

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
Introspector.register('idltest.inheritance.NotificationWithB', {
        full: 'idltest.inheritance.NotificationWithB',
        short: 'NotificationWithB',
        package: 'idltest.inheritance',
        type: IntrospectorTypes.Mixin,
        ctor: () => new NotificationWithBStruct(),
        fields: [

        ],
        implementations: NotificationWithBStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(NotificationWithBStruct.FullClassName, {
        full: NotificationWithBStruct.FullClassName,
        short: NotificationWithBStruct.ClassName,
        package: NotificationWithBStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new NotificationWithBStruct(),
        fields: [

        ]
    } as IIntrospectorDataObject
);