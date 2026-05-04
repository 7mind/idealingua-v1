// Auto-generated, any modifications may be overwritten in the future.
import {
    TestInterface,
    TestInterfaceStruct,
    TestInterfaceStructSerialized
} from './TestInterface';

// TestObject DTO
export class TestObject implements TestInterface  {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain01';
    public static readonly ClassName = 'TestObject';
    public static readonly FullClassName = 'izumi.test.domain01.TestObject';

    public getPackageName(): string { return TestObject.PackageName; }
    public getClassName(): string { return TestObject.ClassName; }
    public getFullClassName(): string { return TestObject.FullClassName; }

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

    constructor(data: TestObjectSerialized = undefined) {
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

    public toTestInterfaceSerialized(): TestInterfaceStructSerialized {
        return {
            userId: this.userId,
            accountBalance: this.accountBalance,
            latestLogin: this.latestLogin,
            keys: Object.keys(this.keys).reduce<any>((previous, current) => {previous[current] = this.keys[current as any]; return previous; }, {}),
            nicknames: this.nicknames.slice()
        };
    }

    public toTestInterface(): TestInterfaceStruct {
        return new TestInterfaceStruct(this.toTestInterfaceSerialized());
    }

    public loadTestInterfaceSerialized(slice: TestInterfaceStructSerialized) {
        this.userId = slice.userId;
        this.accountBalance = slice.accountBalance;
        this.latestLogin = slice.latestLogin;
        this.keys = Object.keys(slice.keys).reduce<any>((previous, current) => {previous[current] = slice.keys[current as any]; return previous; }, {});
        this.nicknames = slice.nicknames.slice();
    }

    public loadTestInterface(slice: TestInterfaceStruct) {
        this.loadTestInterfaceSerialized(slice.serialize());
    }

    public serialize(): TestObjectSerialized {
        return {
            userId: this.userId,
            accountBalance: this.accountBalance,
            latestLogin: this.latestLogin,
            keys: Object.keys(this.keys).reduce<any>((previous, current) => {previous[current] = this.keys[current as any]; return previous; }, {}),
            nicknames: this.nicknames.slice()
        };
    }
}

export interface TestObjectSerialized extends TestInterfaceStructSerialized  {
    userId: string;
    accountBalance: number;
    latestLogin: number;
    keys: {[key: string]: string};
    nicknames: string[];
}

TestInterfaceStruct.register(TestObject.FullClassName, TestObject);

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorDataObject
} from '../../../irt';
Introspector.register(TestObject.FullClassName, {
        full: TestObject.FullClassName,
        short: TestObject.ClassName,
        package: TestObject.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new TestObject(),
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