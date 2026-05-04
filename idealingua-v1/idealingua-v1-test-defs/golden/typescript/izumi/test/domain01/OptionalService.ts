// Auto-generated, any modifications may be overwritten in the future.
import {
    NestedClass,
    NestedClassSerialized
} from './NestedClass';
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

// OptionalService client
// Models
class InOptionalMethod implements IncomingData {
    private _a: NestedClass | undefined;
    private _b: number | undefined;
    private _c: NestedClass | undefined;
    public get a(): NestedClass | undefined {
        return this._a;
    }

    public set a(value: NestedClass | undefined) {
        if (typeof value === 'undefined' || value === null) {
            this._a = undefined;
            return;
        }
        this._a = value;
    }

    public get b(): number | undefined {
        return this._b;
    }

    public set b(value: number | undefined) {
        if (typeof value === 'undefined' || value === null) {
            this._b = undefined;
            return;
        }

        if (typeof value !== 'number') {
            throw new Error('Field b expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field b is expected to be an integer, got ' + value);
        }

        this._b = value;
    }

    public get c(): NestedClass | undefined {
        return this._c;
    }

    public set c(value: NestedClass | undefined) {
        if (typeof value === 'undefined' || value === null) {
            this._c = undefined;
            return;
        }
        this._c = value;
    }

    constructor(data: InOptionalMethodSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.a = typeof data.a !== 'undefined' ? new NestedClass(data.a) : undefined;
        this.b = typeof data.b !== 'undefined' ? data.b : undefined;
        this.c = typeof data.c !== 'undefined' ? new NestedClass(data.c) : undefined;
    }

    public serialize(): InOptionalMethodSerialized {
        return {
            a: typeof this.a !== 'undefined' ? this.a.serialize() : undefined,
            b: typeof this.b !== 'undefined' ? this.b : undefined,
            c: typeof this.c !== 'undefined' ? this.c.serialize() : undefined
        };
    }
}

interface InOptionalMethodSerialized {
    a: NestedClassSerialized | undefined;
    b: number | undefined;
    c: NestedClassSerialized | undefined;
}

// Client
export interface IOptionalServiceClient {
    optionalMethod(a: NestedClass | undefined, b: number | undefined, c: NestedClass | undefined): Promise<number>
}

export class OptionalServiceClient implements IOptionalServiceClient {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain01';
    public static readonly ClassName = 'OptionalService';
    public static readonly FullClassName = 'izumi.test.domain01.OptionalService';

    public getPackageName(): string { return OptionalServiceClient.PackageName; }
    public getClassName(): string { return OptionalServiceClient.ClassName; }
    public getFullClassName(): string { return OptionalServiceClient.FullClassName; }

    protected _transport: ClientTransport;

    constructor(transport: ClientTransport) {
        this._transport = transport;
    }

    private send<I extends IncomingData, O extends OutgoingData>(method: string, data: I, inputType: {new(): I}, outputType: {new(data: any): O} ): Promise<O> {
        return new Promise((resolve, reject) => {
            this._transport.send(OptionalServiceClient.ClassName, method, data)
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
    public optionalMethod(a: NestedClass | undefined, b: number | undefined, c: NestedClass | undefined): Promise<number> {
        const __data = new InOptionalMethod();
        __data.a = a;
        __data.b = b;
        __data.c = c;
        return new Promise((resolve, reject) => {
            this._transport.send(OptionalServiceClient.ClassName, 'optionalMethod', __data)
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
}
// Dispatcher
export interface IOptionalServiceServer<C> {
    optionalMethod(context: C, a: NestedClass | undefined, b: number | undefined, c: NestedClass | undefined): Promise<number>
}

export class OptionalServiceDispatcher<C, D> implements ServiceDispatcher<C, D> {
    private static readonly methods: string[] = [
        "optionalMethod"
    ];
    protected marshaller: Marshaller<D>;
    protected server: IOptionalServiceServer<C>;

    constructor(marshaller: Marshaller<D>, server: IOptionalServiceServer<C>) {
        this.marshaller = marshaller;
        this.server = server;
    }

    public getSupportedService(): string {
        return 'OptionalService';
    }

    public getSupportedMethods(): string[] {
        return  OptionalServiceDispatcher.methods;
    }

    public dispatch(context: C, method: string, data: D | undefined): Promise<D> {
        switch (method) {
            case "optionalMethod": {
                const obj = new InOptionalMethod(this.marshaller.Unmarshal<InOptionalMethodSerialized>(data));
                return new Promise((resolve, reject) => {
                    try {
                        this.server.optionalMethod(context, obj.a, obj.b, obj.c)
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

            default:
                throw new Error(`Method ${method} is not supported by OptionalServiceDispatcher.`);
        }
    }
}

// Base Server
export abstract class OptionalServiceServer<C, D> extends OptionalServiceDispatcher<C, D> implements IOptionalServiceServer<C> {
    constructor(marshaller: Marshaller<D>) {
        super(marshaller, null);
        this.server = this;
    }

    public optionalMethod(context: C, a: NestedClass | undefined, b: number | undefined, c: NestedClass | undefined): Promise<number> {
        throw new Error('Not implemented.');
    }
}