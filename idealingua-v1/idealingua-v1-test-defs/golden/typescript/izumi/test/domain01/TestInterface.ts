// Auto-generated, any modifications may be overwritten in the future.

// TestInterface Interface
export interface TestInterface {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): TestInterfaceStructSerialized;

    userId: string;
    accountBalance: number;
    latestLogin: number;
    keys: {[key: string]: string};
    nicknames: string[];
}

export interface TestInterfaceStructSerialized {
    userId: string;
    accountBalance: number;
    latestLogin: number;
    keys: {[key: string]: string};
    nicknames: string[];
}

export class TestInterfaceStruct implements TestInterface {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain01.TestInterface';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'izumi.test.domain01.TestInterface.Struct';

    public getPackageName(): string { return TestInterfaceStruct.PackageName; }
    public getClassName(): string { return TestInterfaceStruct.ClassName; }
    public getFullClassName(): string { return TestInterfaceStruct.FullClassName; }

    private _userId: string;
    private _accountBalance: number;
    private _latestLogin: number;
    private _keys: {[key: string]: string};
    private _nicknames: string[];

    public get userId(): string {
        return this._userId;
    }

    public set userId(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field userId is not optional');
        }
        this._userId = value;
    }

    public get accountBalance(): number {
        return this._accountBalance;
    }

    public set accountBalance(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field accountBalance is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field accountBalance expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field accountBalance is expected to be an integer, got ' + value);
        }

        this._accountBalance = value;
    }

    public get latestLogin(): number {
        return this._latestLogin;
    }

    public set latestLogin(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field latestLogin is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field latestLogin expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field latestLogin is expected to be an integer, got ' + value);
        }

        this._latestLogin = value;
    }

    public get keys(): {[key: string]: string} {
        return this._keys;
    }

    public set keys(value: {[key: string]: string}) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field keys is not optional');
        }
        this._keys = value;
    }

    public get nicknames(): string[] {
        return this._nicknames;
    }

    public set nicknames(value: string[]) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field nicknames is not optional');
        }
        this._nicknames = value;
    }

    constructor(data: TestInterfaceStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            this.keys = {};
            this.nicknames = [];
            return;
        }

        this.userId = data.userId;
        this.accountBalance = data.accountBalance;
        this.latestLogin = data.latestLogin;
        this.keys = Object.keys(data.keys).reduce<any>((previous, current) => {previous[current] = data.keys[current as any]; return previous; }, {});
        this.nicknames = data.nicknames.slice();
    }

    public serialize(): TestInterfaceStructSerialized {
        return {
            userId: this.userId,
            accountBalance: this.accountBalance,
            latestLogin: this.latestLogin,
            keys: Object.keys(this.keys).reduce<any>((previous, current) => {previous[current] = this.keys[current as any]; return previous; }, {}),
            nicknames: this.nicknames.slice()
        };
    }

    // Polymorphic section below. If a new type to be registered, use TestInterfaceStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: TestInterfaceStruct| TestInterfaceStructSerialized): TestInterface}} = {
        // This basic registration will happen below [TestInterfaceStruct.FullClassName]: TestInterfaceStruct
    };

    public static register(className: string, ctor: {new (data?: TestInterfaceStruct| TestInterfaceStructSerialized): TestInterface}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: TestInterfaceStructSerialized}): TestInterface {
        const polymorphicId = Object.keys(data)[0];
        const ctor = TestInterfaceStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for TestInterfaceStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(TestInterfaceStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in TestInterfaceStruct._knownPolymorphic;
    }
}

TestInterfaceStruct.register(TestInterfaceStruct.FullClassName, TestInterfaceStruct);

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorMixinObject,
    IIntrospectorDataObject
} from '../../../irt';
Introspector.register('izumi.test.domain01.TestInterface', {
        full: 'izumi.test.domain01.TestInterface',
        short: 'TestInterface',
        package: 'izumi.test.domain01',
        type: IntrospectorTypes.Mixin,
        ctor: () => new TestInterfaceStruct(),
        fields: [
            {
                name: 'userId',
                accessName: 'userId',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'accountBalance',
                accessName: 'accountBalance',
                type: {intro: IntrospectorTypes.I32}
            },
            {
                name: 'latestLogin',
                accessName: 'latestLogin',
                type: {intro: IntrospectorTypes.I64}
            },
            {
                name: 'keys',
                accessName: 'keys',
                type: {intro: IntrospectorTypes.Map, key: {intro: IntrospectorTypes.Str}, value: {intro: IntrospectorTypes.Str}} as IIntrospectorMapType
            },
            {
                name: 'nicknames',
                accessName: 'nicknames',
                type: {intro: IntrospectorTypes.List, value: {intro: IntrospectorTypes.Str}} as IIntrospectorGenericType
            }
        ],
        implementations: TestInterfaceStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(TestInterfaceStruct.FullClassName, {
        full: TestInterfaceStruct.FullClassName,
        short: TestInterfaceStruct.ClassName,
        package: TestInterfaceStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new TestInterfaceStruct(),
        fields: [
            {
                name: 'userId',
                accessName: 'userId',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'accountBalance',
                accessName: 'accountBalance',
                type: {intro: IntrospectorTypes.I32}
            },
            {
                name: 'latestLogin',
                accessName: 'latestLogin',
                type: {intro: IntrospectorTypes.I64}
            },
            {
                name: 'keys',
                accessName: 'keys',
                type: {intro: IntrospectorTypes.Map, key: {intro: IntrospectorTypes.Str}, value: {intro: IntrospectorTypes.Str}} as IIntrospectorMapType
            },
            {
                name: 'nicknames',
                accessName: 'nicknames',
                type: {intro: IntrospectorTypes.List, value: {intro: IntrospectorTypes.Str}} as IIntrospectorGenericType
            }
        ]
    } as IIntrospectorDataObject
);