// Auto-generated, any modifications may be overwritten in the future.
import {
    RecordId
} from './RecordId';

// WithRecordId Interface
export interface WithRecordId {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): WithRecordIdStructSerialized;

    id: RecordId;
}

export interface WithRecordIdStructSerialized {
    id: string;
}

export class WithRecordIdStruct implements WithRecordId {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.anyvals.WithRecordId';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.anyvals.WithRecordId.Struct';

    public getPackageName(): string { return WithRecordIdStruct.PackageName; }
    public getClassName(): string { return WithRecordIdStruct.ClassName; }
    public getFullClassName(): string { return WithRecordIdStruct.FullClassName; }

    private _id: RecordId;

    public get id(): RecordId {
        return this._id;
    }

    public set id(value: RecordId) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field id is not optional');
        }
        this._id = value;
    }

    constructor(data: WithRecordIdStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.id = new RecordId(data.id);
    }

    public serialize(): WithRecordIdStructSerialized {
        return {
            id: this.id.serialize()
        };
    }

    // Polymorphic section below. If a new type to be registered, use WithRecordIdStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: WithRecordIdStruct| WithRecordIdStructSerialized): WithRecordId}} = {
        // This basic registration will happen below [WithRecordIdStruct.FullClassName]: WithRecordIdStruct
    };

    public static register(className: string, ctor: {new (data?: WithRecordIdStruct| WithRecordIdStructSerialized): WithRecordId}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: WithRecordIdStructSerialized}): WithRecordId {
        const polymorphicId = Object.keys(data)[0];
        const ctor = WithRecordIdStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for WithRecordIdStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(WithRecordIdStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in WithRecordIdStruct._knownPolymorphic;
    }
}

WithRecordIdStruct.register(WithRecordIdStruct.FullClassName, WithRecordIdStruct);

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
Introspector.register('idltest.anyvals.WithRecordId', {
        full: 'idltest.anyvals.WithRecordId',
        short: 'WithRecordId',
        package: 'idltest.anyvals',
        type: IntrospectorTypes.Mixin,
        ctor: () => new WithRecordIdStruct(),
        fields: [
            {
                name: 'id',
                accessName: 'id',
                type: {intro: IntrospectorTypes.Id, full: 'idltest.anyvals.RecordId'} as IIntrospectorUserType
            }
        ],
        implementations: WithRecordIdStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(WithRecordIdStruct.FullClassName, {
        full: WithRecordIdStruct.FullClassName,
        short: WithRecordIdStruct.ClassName,
        package: WithRecordIdStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new WithRecordIdStruct(),
        fields: [
            {
                name: 'id',
                accessName: 'id',
                type: {intro: IntrospectorTypes.Id, full: 'idltest.anyvals.RecordId'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorDataObject
);