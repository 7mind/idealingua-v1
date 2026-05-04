// Auto-generated, any modifications may be overwritten in the future.
import {
    RTestObject1,
    RTestObject1Serialized,
    AnyValTest2,
    AnyValTest2Struct,
    AnyValTest2StructSerialized,
    IDForDomain2
} from '../domain01';
import {
    DTO1,
    DTO1Serialized
} from './DTO1';
import {
    TestIDReturn
} from './TestIDReturn';
import {
    ServiceDispatcher,
    Marshaller,
    Void,
    IncomingData,
    OutgoingData,
    ClientTransport,
    Either,
    Left as EitherLeft,
    Right as EitherRight
} from '../../../irt'

// TestAliasServ client
// Models
class InGetMassCoupons implements IncomingData {
    private _iterator: RTestObject1 | undefined;
    public get iterator(): RTestObject1 | undefined {
        return this._iterator;
    }

    public set iterator(value: RTestObject1 | undefined) {
        if (typeof value === 'undefined' || value === null) {
            this._iterator = undefined;
            return;
        }
        this._iterator = value;
    }

    constructor(data: InGetMassCouponsSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.iterator = typeof data.iterator !== 'undefined' ? new RTestObject1(data.iterator) : undefined;
    }

    public serialize(): InGetMassCouponsSerialized {
        return {
            iterator: typeof this.iterator !== 'undefined' ? this.iterator.serialize() : undefined
        };
    }
}

interface InGetMassCouponsSerialized {
    iterator: RTestObject1Serialized | undefined;
}

class InIfaceMethod implements IncomingData {
    private _va: AnyValTest2;
    public get va(): AnyValTest2 {
        return this._va;
    }

    public set va(value: AnyValTest2) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field va is not optional');
        }
        this._va = value;
    }

    constructor(data: InIfaceMethodSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.va = AnyValTest2Struct.create(data.va);
    }

    public serialize(): InIfaceMethodSerialized {
        return {
            va: {[this.va.getFullClassName()]: this.va.serialize()}
        };
    }
}

interface InIfaceMethodSerialized {
    va: {[key: string]: AnyValTest2StructSerialized};
}

class InTestADTIdReturn implements IncomingData {
    constructor(data: InTestADTIdReturnSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

    }

    public serialize(): InTestADTIdReturnSerialized {
        return {
        };
    }
}

interface InTestADTIdReturnSerialized {
}

type OutTestADTIdReturn = TestIDReturn | DTO1;
type OutTestADTIdReturnSerialized = string | DTO1Serialized

class OutTestADTIdReturnHelpers {
    public static isInstanceOf(o: any): boolean {
        if (!o['getClassName'] || typeof o['getClassName'] !== 'function') {
            return false;
        }

        return o instanceof TestIDReturn || o instanceof DTO1;
    }

    public static serialize(adt: OutTestADTIdReturn): {[key: string]: string | DTO1Serialized} {
        let className = adt.getClassName();

        return {
            [className]: adt.serialize()
        };
    }

    public static deserialize(data: {[key: string]: string | DTO1Serialized}): OutTestADTIdReturn {
        const id = Object.keys(data)[0];
        const content = (data as any)[id];
        switch (id) {
            case 'TestIDReturn': return new TestIDReturn(content as any);
            case 'DTO1': return new DTO1(content as any);
            default:
                throw new Error('Unknown type id ' + id + ' for OutTestADTIdReturn');
        }
    }
}

class InTestADTIdImportedReturn implements IncomingData {
    constructor(data: InTestADTIdImportedReturnSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

    }

    public serialize(): InTestADTIdImportedReturnSerialized {
        return {
        };
    }
}

interface InTestADTIdImportedReturnSerialized {
}

type OutTestADTIdImportedReturn = IDForDomain2 | DTO1;
type OutTestADTIdImportedReturnSerialized = string | DTO1Serialized

class OutTestADTIdImportedReturnHelpers {
    public static isInstanceOf(o: any): boolean {
        if (!o['getClassName'] || typeof o['getClassName'] !== 'function') {
            return false;
        }

        return o instanceof IDForDomain2 || o instanceof DTO1;
    }

    public static serialize(adt: OutTestADTIdImportedReturn): {[key: string]: string | DTO1Serialized} {
        let className = adt.getClassName();

        return {
            [className]: adt.serialize()
        };
    }

    public static deserialize(data: {[key: string]: string | DTO1Serialized}): OutTestADTIdImportedReturn {
        const id = Object.keys(data)[0];
        const content = (data as any)[id];
        switch (id) {
            case 'ImportedIDForDomain2': return new IDForDomain2(content as any);
            case 'DTO1': return new DTO1(content as any);
            default:
                throw new Error('Unknown type id ' + id + ' for OutTestADTIdImportedReturn');
        }
    }
}

// Client
export interface ITestAliasServClient {
    getMassCoupons(iterator: RTestObject1 | undefined): Promise<number>
    ifaceMethod(va: AnyValTest2): Promise<number>
    testADTIdReturn(): Promise<TestIDReturn | DTO1>
    testADTIdImportedReturn(): Promise<IDForDomain2 | DTO1>
}

export class TestAliasServClient implements ITestAliasServClient {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain02';
    public static readonly ClassName = 'TestAliasServ';
    public static readonly FullClassName = 'izumi.test.domain02.TestAliasServ';

    public getPackageName(): string { return TestAliasServClient.PackageName; }
    public getClassName(): string { return TestAliasServClient.ClassName; }
    public getFullClassName(): string { return TestAliasServClient.FullClassName; }

    protected _transport: ClientTransport;

    constructor(transport: ClientTransport) {
        this._transport = transport;
    }

    private send<I extends IncomingData, O extends OutgoingData>(method: string, data: I, inputType: {new(): I}, outputType: {new(data: any): O} ): Promise<O> {
        return new Promise((resolve, reject) => {
            this._transport.send(TestAliasServClient.ClassName, method, data)
                .then((data: any) => {
                    try {
                        const output = new outputType(data);
                        resolve(output);
                    }
                    catch (err) {
                        reject(err);
                    }
                })
                .catch((err: any) => {
                    reject(err);
                });
            });
    }
    public getMassCoupons(iterator: RTestObject1 | undefined): Promise<number> {
        const __data = new InGetMassCoupons();
        __data.iterator = iterator;
        return new Promise((resolve, reject) => {
            this._transport.send(TestAliasServClient.ClassName, 'getMassCoupons', __data)
                .then((data: any) => {
                    try {
                        const output = data;
                        resolve(output);
                    }
                    catch(err) {
                        reject(err);
                    }
                })
                .catch((err: any) => {
                    reject(err);
                });
            });
    }

    public ifaceMethod(va: AnyValTest2): Promise<number> {
        const __data = new InIfaceMethod();
        __data.va = va;
        return new Promise((resolve, reject) => {
            this._transport.send(TestAliasServClient.ClassName, 'ifaceMethod', __data)
                .then((data: any) => {
                    try {
                        const output = data;
                        resolve(output);
                    }
                    catch(err) {
                        reject(err);
                    }
                })
                .catch((err: any) => {
                    reject(err);
                });
            });
    }

    public testADTIdReturn(): Promise<TestIDReturn | DTO1> {
        const __data = new InTestADTIdReturn();

        return new Promise((resolve, reject) => {
            this._transport.send(TestAliasServClient.ClassName, 'testADTIdReturn', __data)
                .then((data: any) => {
                    try {
                        resolve(OutTestADTIdReturnHelpers.deserialize(data));
                    } catch(err) {
                        reject(err);
                    }
                 })
                .catch((err: any) => {
                    reject(err);
                });
        });
    }

    public testADTIdImportedReturn(): Promise<IDForDomain2 | DTO1> {
        const __data = new InTestADTIdImportedReturn();

        return new Promise((resolve, reject) => {
            this._transport.send(TestAliasServClient.ClassName, 'testADTIdImportedReturn', __data)
                .then((data: any) => {
                    try {
                        resolve(OutTestADTIdImportedReturnHelpers.deserialize(data));
                    } catch(err) {
                        reject(err);
                    }
                 })
                .catch((err: any) => {
                    reject(err);
                });
        });
    }
}
// Dispatcher
export interface ITestAliasServServer<C> {
    getMassCoupons(context: C, iterator: RTestObject1 | undefined): Promise<number>
    ifaceMethod(context: C, va: AnyValTest2): Promise<number>
    testADTIdReturn(context: C): Promise<TestIDReturn | DTO1>
    testADTIdImportedReturn(context: C): Promise<IDForDomain2 | DTO1>
}

export class TestAliasServDispatcher<C, D> implements ServiceDispatcher<C, D> {
    private static readonly methods: string[] = [
        "getMassCoupons",
        "ifaceMethod",
        "testADTIdReturn",
        "testADTIdImportedReturn"
    ];
    protected marshaller: Marshaller<D>;
    protected server: ITestAliasServServer<C>;

    constructor(marshaller: Marshaller<D>, server: ITestAliasServServer<C>) {
        this.marshaller = marshaller;
        this.server = server;
    }

    public getSupportedService(): string {
        return 'TestAliasServ';
    }

    public getSupportedMethods(): string[] {
        return  TestAliasServDispatcher.methods;
    }

    public dispatch(context: C, method: string, data: D | undefined): Promise<D> {
        switch (method) {
            case "getMassCoupons": {
                const obj = new InGetMassCoupons(this.marshaller.Unmarshal<InGetMassCouponsSerialized>(data));
                return new Promise((resolve, reject) => {
                    try {
                        this.server.getMassCoupons(context, obj.iterator)
                            .then((res: number) => {
                                const serialized = this.marshaller.Marshal<number>(res);
                                resolve(serialized);
                            })
                            .catch((err) => {
                                reject(err);
                            });
                    } catch (err) {
                        reject(err);
                    }
                });
            }

            case "ifaceMethod": {
                const obj = new InIfaceMethod(this.marshaller.Unmarshal<InIfaceMethodSerialized>(data));
                return new Promise((resolve, reject) => {
                    try {
                        this.server.ifaceMethod(context, obj.va)
                            .then((res: number) => {
                                const serialized = this.marshaller.Marshal<number>(res);
                                resolve(serialized);
                            })
                            .catch((err) => {
                                reject(err);
                            });
                    } catch (err) {
                        reject(err);
                    }
                });
            }

            case "testADTIdReturn": {
                // No input params for this method
                return new Promise((resolve, reject) => {
                    try {
                        this.server.testADTIdReturn(context)
                            .then((res: TestIDReturn | DTO1) => {
                                const serialized = this.marshaller.Marshal<object>(OutTestADTIdReturnHelpers.serialize(res));
                                resolve(serialized);
                            })
                            .catch((err) => {
                                reject(err);
                            });
                    } catch (err) {
                        reject(err);
                    }
                });
            }

            case "testADTIdImportedReturn": {
                // No input params for this method
                return new Promise((resolve, reject) => {
                    try {
                        this.server.testADTIdImportedReturn(context)
                            .then((res: IDForDomain2 | DTO1) => {
                                const serialized = this.marshaller.Marshal<object>(OutTestADTIdImportedReturnHelpers.serialize(res));
                                resolve(serialized);
                            })
                            .catch((err) => {
                                reject(err);
                            });
                    } catch (err) {
                        reject(err);
                    }
                });
            }

            default:
                throw new Error(`Method ${method} is not supported by TestAliasServDispatcher.`);
        }
    }
}

// Base Server
export abstract class TestAliasServServer<C, D> extends TestAliasServDispatcher<C, D> implements ITestAliasServServer<C> {
    constructor(marshaller: Marshaller<D>) {
        super(marshaller, null);
        this.server = this;
    }

    public getMassCoupons(context: C, iterator: RTestObject1 | undefined): Promise<number> {
        throw new Error('Not implemented.');
    }

    public ifaceMethod(context: C, va: AnyValTest2): Promise<number> {
        throw new Error('Not implemented.');
    }

    public testADTIdReturn(context: C): Promise<TestIDReturn | DTO1> {
        throw new Error('Not implemented.');
    }

    public testADTIdImportedReturn(context: C): Promise<IDForDomain2 | DTO1> {
        throw new Error('Not implemented.');
    }
}