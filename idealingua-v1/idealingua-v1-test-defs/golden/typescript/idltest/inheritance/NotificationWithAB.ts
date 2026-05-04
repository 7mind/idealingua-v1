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

// NotificationWithAB Interface
export interface NotificationWithAB extends NotificationWithA, NotificationWithB {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): NotificationWithABStructSerialized;
}
export interface NotificationWithABStructSerialized extends NotificationWithAStructSerialized, NotificationWithBStructSerialized {
}

export class NotificationWithABStruct implements NotificationWithAB {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.inheritance.NotificationWithAB';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.inheritance.NotificationWithAB.Struct';

    public getPackageName(): string { return NotificationWithABStruct.PackageName; }
    public getClassName(): string { return NotificationWithABStruct.ClassName; }
    public getFullClassName(): string { return NotificationWithABStruct.FullClassName; }

    constructor(data: NotificationWithABStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

    }

    public serialize(): NotificationWithABStructSerialized {
        return {
        };
    }

    // Polymorphic section below. If a new type to be registered, use NotificationWithABStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: NotificationWithABStruct| NotificationWithABStructSerialized): NotificationWithAB}} = {
        // This basic registration will happen below [NotificationWithABStruct.FullClassName]: NotificationWithABStruct
    };

    public static register(className: string, ctor: {new (data?: NotificationWithABStruct| NotificationWithABStructSerialized): NotificationWithAB}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: NotificationWithABStructSerialized}): NotificationWithAB {
        const polymorphicId = Object.keys(data)[0];
        const ctor = NotificationWithABStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for NotificationWithABStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(NotificationWithABStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in NotificationWithABStruct._knownPolymorphic;
    }
}

NotificationWithABStruct.register(NotificationWithABStruct.FullClassName, NotificationWithABStruct);
NotificationWithAStruct.register(NotificationWithABStruct.FullClassName, NotificationWithABStruct);
NotificationStruct.register(NotificationWithABStruct.FullClassName, NotificationWithABStruct);
NotificationWithBStruct.register(NotificationWithABStruct.FullClassName, NotificationWithABStruct);

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
Introspector.register('idltest.inheritance.NotificationWithAB', {
        full: 'idltest.inheritance.NotificationWithAB',
        short: 'NotificationWithAB',
        package: 'idltest.inheritance',
        type: IntrospectorTypes.Mixin,
        ctor: () => new NotificationWithABStruct(),
        fields: [

        ],
        implementations: NotificationWithABStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(NotificationWithABStruct.FullClassName, {
        full: NotificationWithABStruct.FullClassName,
        short: NotificationWithABStruct.ClassName,
        package: NotificationWithABStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new NotificationWithABStruct(),
        fields: [

        ]
    } as IIntrospectorDataObject
);