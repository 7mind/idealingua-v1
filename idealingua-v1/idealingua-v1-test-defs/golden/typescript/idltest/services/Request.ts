// Auto-generated, any modifications may be overwritten in the future.

// Request Interface
export interface Request {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): RequestStructSerialized;

    firstName: string;
    secondName: string;
}

export interface RequestStructSerialized {
    firstName: string;
    secondName: string;
}

export class RequestStruct implements Request {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.services.Request';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.services.Request.Struct';

    public getPackageName(): string { return RequestStruct.PackageName; }
    public getClassName(): string { return RequestStruct.ClassName; }
    public getFullClassName(): string { return RequestStruct.FullClassName; }

    private _firstName: string;
    private _secondName: string;

    public get firstName(): string {
        return this._firstName;
    }

    public set firstName(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field firstName is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field firstName expects type string, got ' + value);
        }

        this._firstName = value;
    }

    public get secondName(): string {
        return this._secondName;
    }

    public set secondName(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field secondName is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field secondName expects type string, got ' + value);
        }

        this._secondName = value;
    }

    constructor(data: RequestStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.firstName = data.firstName;
        this.secondName = data.secondName;
    }

    public serialize(): RequestStructSerialized {
        return {
            firstName: this.firstName,
            secondName: this.secondName
        };
    }

    // Polymorphic section below. If a new type to be registered, use RequestStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: RequestStruct| RequestStructSerialized): Request}} = {
        // This basic registration will happen below [RequestStruct.FullClassName]: RequestStruct
    };

    public static register(className: string, ctor: {new (data?: RequestStruct| RequestStructSerialized): Request}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: RequestStructSerialized}): Request {
        const polymorphicId = Object.keys(data)[0];
        const ctor = RequestStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for RequestStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(RequestStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in RequestStruct._knownPolymorphic;
    }
}

RequestStruct.register(RequestStruct.FullClassName, RequestStruct);

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
Introspector.register('idltest.services.Request', {
        full: 'idltest.services.Request',
        short: 'Request',
        package: 'idltest.services',
        type: IntrospectorTypes.Mixin,
        ctor: () => new RequestStruct(),
        fields: [
            {
                name: 'firstName',
                accessName: 'firstName',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'secondName',
                accessName: 'secondName',
                type: {intro: IntrospectorTypes.Str}
            }
        ],
        implementations: RequestStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(RequestStruct.FullClassName, {
        full: RequestStruct.FullClassName,
        short: RequestStruct.ClassName,
        package: RequestStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new RequestStruct(),
        fields: [
            {
                name: 'firstName',
                accessName: 'firstName',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'secondName',
                accessName: 'secondName',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);