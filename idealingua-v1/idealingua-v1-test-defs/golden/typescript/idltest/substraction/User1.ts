// Auto-generated, any modifications may be overwritten in the future.

// User1 Interface
export interface User1 {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): User1StructSerialized;

    name: string;
    id: string;
    pass: string;
}

export interface User1StructSerialized {
    name: string;
    id: string;
    pass: string;
}

export class User1Struct implements User1 {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.substraction.User1';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.substraction.User1.Struct';

    public getPackageName(): string { return User1Struct.PackageName; }
    public getClassName(): string { return User1Struct.ClassName; }
    public getFullClassName(): string { return User1Struct.FullClassName; }

    private _name: string;
    private _id: string;
    private _pass: string;

    public get name(): string {
        return this._name;
    }

    public set name(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field name is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field name expects type string, got ' + value);
        }

        this._name = value;
    }

    public get id(): string {
        return this._id;
    }

    public set id(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field id is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field id expects type string, got ' + value);
        }

        this._id = value;
    }

    public get pass(): string {
        return this._pass;
    }

    public set pass(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field pass is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field pass expects type string, got ' + value);
        }

        this._pass = value;
    }

    constructor(data: User1StructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.name = data.name;
        this.id = data.id;
        this.pass = data.pass;
    }

    public serialize(): User1StructSerialized {
        return {
            name: this.name,
            id: this.id,
            pass: this.pass
        };
    }

    // Polymorphic section below. If a new type to be registered, use User1Struct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: User1Struct| User1StructSerialized): User1}} = {
        // This basic registration will happen below [User1Struct.FullClassName]: User1Struct
    };

    public static register(className: string, ctor: {new (data?: User1Struct| User1StructSerialized): User1}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: User1StructSerialized}): User1 {
        const polymorphicId = Object.keys(data)[0];
        const ctor = User1Struct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for User1Struct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(User1Struct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in User1Struct._knownPolymorphic;
    }
}

User1Struct.register(User1Struct.FullClassName, User1Struct);

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
Introspector.register('idltest.substraction.User1', {
        full: 'idltest.substraction.User1',
        short: 'User1',
        package: 'idltest.substraction',
        type: IntrospectorTypes.Mixin,
        ctor: () => new User1Struct(),
        fields: [
            {
                name: 'name',
                accessName: 'name',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'id',
                accessName: 'id',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'pass',
                accessName: 'pass',
                type: {intro: IntrospectorTypes.Str}
            }
        ],
        implementations: User1Struct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(User1Struct.FullClassName, {
        full: User1Struct.FullClassName,
        short: User1Struct.ClassName,
        package: User1Struct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new User1Struct(),
        fields: [
            {
                name: 'name',
                accessName: 'name',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'id',
                accessName: 'id',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'pass',
                accessName: 'pass',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);