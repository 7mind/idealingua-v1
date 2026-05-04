// Auto-generated, any modifications may be overwritten in the future.
import {
    EnumType
} from './EnumType';
import {
    ADTType,
    ADTTypeSerialized,
    ADTTypeHelpers
} from './ADTType';
import {
    ServiceDispatcher,
    Marshaller,
    Void,
    IncomingData,
    OutgoingData,
    ServerSocketTransport,
    Either,
    Left as EitherLeft,
    Right as EitherRight
} from '../../irt'

// TestBuzzer
// Models
class InEmpty implements IncomingData {
    constructor(data: InEmptySerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

    }

    public serialize(): InEmptySerialized {
        return {
        };
    }
}

interface InEmptySerialized {
}

class InUserRegistered implements IncomingData {
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

    constructor(data: InUserRegisteredSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.firstName = data.firstName;
        this.secondName = data.secondName;
    }

    public serialize(): InUserRegisteredSerialized {
        return {
            firstName: this.firstName,
            secondName: this.secondName
        };
    }
}

interface InUserRegisteredSerialized {
    firstName: string;
    secondName: string;
}

class InHello implements IncomingData {
    private _name: string;
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

    constructor(data: InHelloSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.name = data.name;
    }

    public serialize(): InHelloSerialized {
        return {
            name: this.name
        };
    }
}

interface InHelloSerialized {
    name: string;
}

class InEnumInput implements IncomingData {
    private _value: EnumType;
    public get value(): EnumType {
        return this._value;
    }

    public set value(value: EnumType) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field value is not optional');
        }
        this._value = value;
    }

    constructor(data: InEnumInputSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.value = EnumType[data.value as keyof typeof EnumType];
    }

    public serialize(): InEnumInputSerialized {
        return {
            value: EnumType[this.value]
        };
    }
}

interface InEnumInputSerialized {
    value: string;
}

class InEnumInputVoid implements IncomingData {
    private _value: EnumType;
    public get value(): EnumType {
        return this._value;
    }

    public set value(value: EnumType) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field value is not optional');
        }
        this._value = value;
    }

    constructor(data: InEnumInputVoidSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.value = EnumType[data.value as keyof typeof EnumType];
    }

    public serialize(): InEnumInputVoidSerialized {
        return {
            value: EnumType[this.value]
        };
    }
}

interface InEnumInputVoidSerialized {
    value: string;
}

class InAdtInput implements IncomingData {
    private _value: ADTType;
    public get value(): ADTType {
        return this._value;
    }

    public set value(value: ADTType) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field value is not optional');
        }
        this._value = value;
    }

    constructor(data: InAdtInputSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.value = ADTTypeHelpers.deserialize(data.value);
    }

    public serialize(): InAdtInputSerialized {
        return {
            value: ADTTypeHelpers.serialize(this.value)
        };
    }
}

interface InAdtInputSerialized {
    value: {[key: string]: any};
}

class InAdtInputVoid implements IncomingData {
    private _value: ADTType;
    public get value(): ADTType {
        return this._value;
    }

    public set value(value: ADTType) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field value is not optional');
        }
        this._value = value;
    }

    constructor(data: InAdtInputVoidSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.value = ADTTypeHelpers.deserialize(data.value);
    }

    public serialize(): InAdtInputVoidSerialized {
        return {
            value: ADTTypeHelpers.serialize(this.value)
        };
    }
}

interface InAdtInputVoidSerialized {
    value: {[key: string]: any};
}

// Client
export interface ITestBuzzerClient {
    empty(): Promise<void>
    userRegistered(firstName: string, secondName: string): Promise<void>
    hello(name: string): Promise<string>
    enumInput(value: EnumType): Promise<string>
    enumInputVoid(value: EnumType): Promise<void>
    adtInput(value: ADTType): Promise<string>
    adtInputVoid(value: ADTType): Promise<void>
}

export class TestBuzzerClient implements ITestBuzzerClient {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.events';
    public static readonly ClassName = 'TestBuzzer';
    public static readonly FullClassName = 'idltest.events.TestBuzzer';

    public getPackageName(): string { return TestBuzzerClient.PackageName; }
    public getClassName(): string { return TestBuzzerClient.ClassName; }
    public getFullClassName(): string { return TestBuzzerClient.FullClassName; }

    protected _transport: ServerSocketTransport;

    constructor(transport: ServerSocketTransport) {
        this._transport = transport;
    }

    private send<I extends IncomingData, O extends OutgoingData>(method: string, data: I, inputType: {new(): I}, outputType: {new(data: any): O} ): Promise<O> {
        return new Promise((resolve, reject) => {
            this._transport.send(TestBuzzerClient.ClassName, method, data)
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
    public empty(): Promise<void> {
        const __data = new InEmpty();

        return new Promise((resolve, reject) => {
            this._transport.send(TestBuzzerClient.ClassName, 'empty', __data)
                .then(() => {
                  resolve();
                })
                .catch((err: any) => {
                    reject(err);
                });
            });
    }

    public userRegistered(firstName: string, secondName: string): Promise<void> {
        const __data = new InUserRegistered();
        __data.firstName = firstName;
        __data.secondName = secondName;
        return new Promise((resolve, reject) => {
            this._transport.send(TestBuzzerClient.ClassName, 'userRegistered', __data)
                .then(() => {
                  resolve();
                })
                .catch((err: any) => {
                    reject(err);
                });
            });
    }

    public hello(name: string): Promise<string> {
        const __data = new InHello();
        __data.name = name;
        return new Promise((resolve, reject) => {
            this._transport.send(TestBuzzerClient.ClassName, 'hello', __data)
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

    public enumInput(value: EnumType): Promise<string> {
        const __data = new InEnumInput();
        __data.value = value;
        return new Promise((resolve, reject) => {
            this._transport.send(TestBuzzerClient.ClassName, 'enumInput', __data)
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

    public enumInputVoid(value: EnumType): Promise<void> {
        const __data = new InEnumInputVoid();
        __data.value = value;
        return new Promise((resolve, reject) => {
            this._transport.send(TestBuzzerClient.ClassName, 'enumInputVoid', __data)
                .then(() => {
                  resolve();
                })
                .catch((err: any) => {
                    reject(err);
                });
            });
    }

    public adtInput(value: ADTType): Promise<string> {
        const __data = new InAdtInput();
        __data.value = value;
        return new Promise((resolve, reject) => {
            this._transport.send(TestBuzzerClient.ClassName, 'adtInput', __data)
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

    public adtInputVoid(value: ADTType): Promise<void> {
        const __data = new InAdtInputVoid();
        __data.value = value;
        return new Promise((resolve, reject) => {
            this._transport.send(TestBuzzerClient.ClassName, 'adtInputVoid', __data)
                .then(() => {
                  resolve();
                })
                .catch((err: any) => {
                    reject(err);
                });
            });
    }
}
// Dispatcher
export interface ITestBuzzerBuzzerHandlers<C> {
    empty(context: C): Promise<void>
    userRegistered(context: C, firstName: string, secondName: string): Promise<void>
    hello(context: C, name: string): Promise<string>
    enumInput(context: C, value: EnumType): Promise<string>
    enumInputVoid(context: C, value: EnumType): Promise<void>
    adtInput(context: C, value: ADTType): Promise<string>
    adtInputVoid(context: C, value: ADTType): Promise<void>
}

export class TestBuzzerDispatcher<C, D> implements ServiceDispatcher<C, D> {
    private static readonly methods: string[] = [
        "empty",
        "userRegistered",
        "hello",
        "enumInput",
        "enumInputVoid",
        "adtInput",
        "adtInputVoid"
    ];
    protected marshaller: Marshaller<D>;
    protected handlers: ITestBuzzerBuzzerHandlers<C>;

    constructor(marshaller: Marshaller<D>, handlers: ITestBuzzerBuzzerHandlers<C>) {
        this.marshaller = marshaller;
        this.handlers = handlers;
    }

    public getSupportedService(): string {
        return 'TestBuzzer';
    }

    public getSupportedMethods(): string[] {
        return  TestBuzzerDispatcher.methods;
    }

    public dispatch(context: C, method: string, data: D | undefined): Promise<D> {
        switch (method) {
            case "empty": {
                // No input params for this method
                return new Promise((resolve, reject) => {
                    try {
                        this.handlers.empty(context)
                            .then((res: void) => {
                                resolve(this.marshaller.Marshal<Void>(Void.instance, true));
                            })
                            .catch((err) => {
                                reject(err);
                            });
                    } catch (err) {
                        reject(err);
                    }
                });
            }

            case "userRegistered": {
                const obj = new InUserRegistered(this.marshaller.Unmarshal<InUserRegisteredSerialized>(data, true));
                return new Promise((resolve, reject) => {
                    try {
                        this.handlers.userRegistered(context, obj.firstName, obj.secondName)
                            .then((res: void) => {
                                resolve(this.marshaller.Marshal<Void>(Void.instance, true));
                            })
                            .catch((err) => {
                                reject(err);
                            });
                    } catch (err) {
                        reject(err);
                    }
                });
            }

            case "hello": {
                const obj = new InHello(this.marshaller.Unmarshal<InHelloSerialized>(data, true));
                return new Promise((resolve, reject) => {
                    try {
                        this.handlers.hello(context, obj.name)
                            .then((res: string) => {
                                const serialized = this.marshaller.Marshal<string>(res, true);
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

            case "enumInput": {
                const obj = new InEnumInput(this.marshaller.Unmarshal<InEnumInputSerialized>(data, true));
                return new Promise((resolve, reject) => {
                    try {
                        this.handlers.enumInput(context, obj.value)
                            .then((res: string) => {
                                const serialized = this.marshaller.Marshal<string>(res, true);
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

            case "enumInputVoid": {
                const obj = new InEnumInputVoid(this.marshaller.Unmarshal<InEnumInputVoidSerialized>(data, true));
                return new Promise((resolve, reject) => {
                    try {
                        this.handlers.enumInputVoid(context, obj.value)
                            .then((res: void) => {
                                resolve(this.marshaller.Marshal<Void>(Void.instance, true));
                            })
                            .catch((err) => {
                                reject(err);
                            });
                    } catch (err) {
                        reject(err);
                    }
                });
            }

            case "adtInput": {
                const obj = new InAdtInput(this.marshaller.Unmarshal<InAdtInputSerialized>(data, true));
                return new Promise((resolve, reject) => {
                    try {
                        this.handlers.adtInput(context, obj.value)
                            .then((res: string) => {
                                const serialized = this.marshaller.Marshal<string>(res, true);
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

            case "adtInputVoid": {
                const obj = new InAdtInputVoid(this.marshaller.Unmarshal<InAdtInputVoidSerialized>(data, true));
                return new Promise((resolve, reject) => {
                    try {
                        this.handlers.adtInputVoid(context, obj.value)
                            .then((res: void) => {
                                resolve(this.marshaller.Marshal<Void>(Void.instance, true));
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
                throw new Error(`Method ${method} is not supported by TestBuzzerDispatcher.`);
        }
    }
}

// Buzzer Handlers Base
export abstract class TestBuzzerBuzzerHandlers<C, D> extends TestBuzzerDispatcher<C, D> implements ITestBuzzerBuzzerHandlers<C> {
    constructor(marshaller: Marshaller<D>) {
        super(marshaller, null);
        this.handlers = this;
    }

    public empty(context: C): Promise<void> {
        throw new Error('Not implemented.');
    }

    public userRegistered(context: C, firstName: string, secondName: string): Promise<void> {
        throw new Error('Not implemented.');
    }

    public hello(context: C, name: string): Promise<string> {
        throw new Error('Not implemented.');
    }

    public enumInput(context: C, value: EnumType): Promise<string> {
        throw new Error('Not implemented.');
    }

    public enumInputVoid(context: C, value: EnumType): Promise<void> {
        throw new Error('Not implemented.');
    }

    public adtInput(context: C, value: ADTType): Promise<string> {
        throw new Error('Not implemented.');
    }

    public adtInputVoid(context: C, value: ADTType): Promise<void> {
        throw new Error('Not implemented.');
    }
}