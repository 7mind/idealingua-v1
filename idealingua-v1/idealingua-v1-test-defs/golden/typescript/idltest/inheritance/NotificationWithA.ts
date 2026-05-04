// Auto-generated, any modifications may be overwritten in the future.
import {
    Notification,
    NotificationStruct,
    NotificationStructSerialized
} from './Notification';

// NotificationWithA Interface
export interface NotificationWithA extends Notification {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): NotificationWithAStructSerialized;
}
export interface NotificationWithAStructSerialized extends NotificationStructSerialized {
}

export class NotificationWithAStruct implements NotificationWithA {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.inheritance.NotificationWithA';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.inheritance.NotificationWithA.Struct';

    public getPackageName(): string { return NotificationWithAStruct.PackageName; }
    public getClassName(): string { return NotificationWithAStruct.ClassName; }
    public getFullClassName(): string { return NotificationWithAStruct.FullClassName; }

    constructor(data: NotificationWithAStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

    }

    public serialize(): NotificationWithAStructSerialized {
        return {
        };
    }

    // Polymorphic section below. If a new type to be registered, use NotificationWithAStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: NotificationWithAStruct| NotificationWithAStructSerialized): NotificationWithA}} = {
        // This basic registration will happen below [NotificationWithAStruct.FullClassName]: NotificationWithAStruct
    };

    public static register(className: string, ctor: {new (data?: NotificationWithAStruct| NotificationWithAStructSerialized): NotificationWithA}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: NotificationWithAStructSerialized}): NotificationWithA {
        const polymorphicId = Object.keys(data)[0];
        const ctor = NotificationWithAStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for NotificationWithAStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(NotificationWithAStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in NotificationWithAStruct._knownPolymorphic;
    }
}

NotificationWithAStruct.register(NotificationWithAStruct.FullClassName, NotificationWithAStruct);
NotificationStruct.register(NotificationWithAStruct.FullClassName, NotificationWithAStruct);

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
Introspector.register('idltest.inheritance.NotificationWithA', {
        full: 'idltest.inheritance.NotificationWithA',
        short: 'NotificationWithA',
        package: 'idltest.inheritance',
        type: IntrospectorTypes.Mixin,
        ctor: () => new NotificationWithAStruct(),
        fields: [

        ],
        implementations: NotificationWithAStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(NotificationWithAStruct.FullClassName, {
        full: NotificationWithAStruct.FullClassName,
        short: NotificationWithAStruct.ClassName,
        package: NotificationWithAStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new NotificationWithAStruct(),
        fields: [

        ]
    } as IIntrospectorDataObject
);