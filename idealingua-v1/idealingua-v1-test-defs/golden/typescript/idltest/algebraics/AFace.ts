// Auto-generated, any modifications may be overwritten in the future.

// AFace Interface
export interface AFace {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): AFaceStructSerialized;

    a: number;
}

export interface AFaceStructSerialized {
    a: number;
}

export class AFaceStruct implements AFace {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.algebraics.AFace';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.algebraics.AFace.Struct';

    public getPackageName(): string { return AFaceStruct.PackageName; }
    public getClassName(): string { return AFaceStruct.ClassName; }
    public getFullClassName(): string { return AFaceStruct.FullClassName; }

    private _a: number;

    public get a(): number {
        return this._a;
    }

    public set a(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field a is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field a expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field a is expected to be an integer, got ' + value);
        }

        this._a = value;
    }

    constructor(data: AFaceStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.a = data.a;
    }

    public serialize(): AFaceStructSerialized {
        return {
            a: this.a
        };
    }

    // Polymorphic section below. If a new type to be registered, use AFaceStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: AFaceStruct| AFaceStructSerialized): AFace}} = {
        // This basic registration will happen below [AFaceStruct.FullClassName]: AFaceStruct
    };

    public static register(className: string, ctor: {new (data?: AFaceStruct| AFaceStructSerialized): AFace}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: AFaceStructSerialized}): AFace {
        const polymorphicId = Object.keys(data)[0];
        const ctor = AFaceStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for AFaceStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(AFaceStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in AFaceStruct._knownPolymorphic;
    }
}

AFaceStruct.register(AFaceStruct.FullClassName, AFaceStruct);

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
Introspector.register('idltest.algebraics.AFace', {
        full: 'idltest.algebraics.AFace',
        short: 'AFace',
        package: 'idltest.algebraics',
        type: IntrospectorTypes.Mixin,
        ctor: () => new AFaceStruct(),
        fields: [
            {
                name: 'a',
                accessName: 'a',
                type: {intro: IntrospectorTypes.I32}
            }
        ],
        implementations: AFaceStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(AFaceStruct.FullClassName, {
        full: AFaceStruct.FullClassName,
        short: AFaceStruct.ClassName,
        package: AFaceStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new AFaceStruct(),
        fields: [
            {
                name: 'a',
                accessName: 'a',
                type: {intro: IntrospectorTypes.I32}
            }
        ]
    } as IIntrospectorDataObject
);