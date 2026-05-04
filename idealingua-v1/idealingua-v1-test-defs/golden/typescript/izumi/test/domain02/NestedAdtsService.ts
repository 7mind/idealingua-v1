// Auto-generated, any modifications may be overwritten in the future.
import {
    Adt2,
    Adt2Serialized
} from './Adt2';
import {
    AdtA,
    AdtASerialized,
    AdtAHelpers
} from './AdtA';
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

// NestedAdtsService client
// Models
class InAdtNested implements IncomingData {
    constructor(data: InAdtNestedSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

    }

    public serialize(): InAdtNestedSerialized {
        return {
        };
    }
}

interface InAdtNestedSerialized {
}

type OutAdtNested = AdtA | Adt2;
type OutAdtNestedSerialized = {[key: string]: any} | Adt2Serialized

class OutAdtNestedHelpers {
    public static isInstanceOf(o: any): boolean {
        if (!o['getClassName'] || typeof o['getClassName'] !== 'function') {
            return false;
        }

        return AdtAHelpers.isInstanceOf(o) || o instanceof Adt2;
    }

    public static serialize(adt: OutAdtNested): {[key: string]: AdtASerialized | Adt2Serialized} {
        let className = adt.getClassName();

        let serialized: any = undefined;
        if (AdtAHelpers.isInstanceOf(adt)) {
            className = 'AdtA';
            serialized = AdtAHelpers.serialize(adt as AdtA);
        }

        return {
            [className]: serialized || adt.serialize()
        };
    }

    public static deserialize(data: {[key: string]: AdtASerialized | Adt2Serialized}): OutAdtNested {
        const id = Object.keys(data)[0];
        const content = (data as any)[id];
        switch (id) {
            case 'AdtA': return AdtAHelpers.deserialize(content as any);
            case 'Adt2': return new Adt2(content as any);
            default:
                throw new Error('Unknown type id ' + id + ' for OutAdtNested');
        }
    }
}

// Client
export interface INestedAdtsServiceClient {
    adtNested(): Promise<AdtA | Adt2>
}

export class NestedAdtsServiceClient implements INestedAdtsServiceClient {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain02';
    public static readonly ClassName = 'NestedAdtsService';
    public static readonly FullClassName = 'izumi.test.domain02.NestedAdtsService';

    public getPackageName(): string { return NestedAdtsServiceClient.PackageName; }
    public getClassName(): string { return NestedAdtsServiceClient.ClassName; }
    public getFullClassName(): string { return NestedAdtsServiceClient.FullClassName; }

    protected _transport: ClientTransport;

    constructor(transport: ClientTransport) {
        this._transport = transport;
    }

    private send<I extends IncomingData, O extends OutgoingData>(method: string, data: I, inputType: {new(): I}, outputType: {new(data: any): O} ): Promise<O> {
        return new Promise((resolve, reject) => {
            this._transport.send(NestedAdtsServiceClient.ClassName, method, data)
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
    public adtNested(): Promise<AdtA | Adt2> {
        const __data = new InAdtNested();

        return new Promise((resolve, reject) => {
            this._transport.send(NestedAdtsServiceClient.ClassName, 'adtNested', __data)
                .then((data: any) => {
                    try {
                        resolve(OutAdtNestedHelpers.deserialize(data));
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
export interface INestedAdtsServiceServer<C> {
    adtNested(context: C): Promise<AdtA | Adt2>
}

export class NestedAdtsServiceDispatcher<C, D> implements ServiceDispatcher<C, D> {
    private static readonly methods: string[] = [
        "adtNested"
    ];
    protected marshaller: Marshaller<D>;
    protected server: INestedAdtsServiceServer<C>;

    constructor(marshaller: Marshaller<D>, server: INestedAdtsServiceServer<C>) {
        this.marshaller = marshaller;
        this.server = server;
    }

    public getSupportedService(): string {
        return 'NestedAdtsService';
    }

    public getSupportedMethods(): string[] {
        return  NestedAdtsServiceDispatcher.methods;
    }

    public dispatch(context: C, method: string, data: D | undefined): Promise<D> {
        switch (method) {
            case "adtNested": {
                // No input params for this method
                return new Promise((resolve, reject) => {
                    try {
                        this.server.adtNested(context)
                            .then((res: AdtA | Adt2) => {
                                const serialized = this.marshaller.Marshal<object>(OutAdtNestedHelpers.serialize(res));
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
                throw new Error(`Method ${method} is not supported by NestedAdtsServiceDispatcher.`);
        }
    }
}

// Base Server
export abstract class NestedAdtsServiceServer<C, D> extends NestedAdtsServiceDispatcher<C, D> implements INestedAdtsServiceServer<C> {
    constructor(marshaller: Marshaller<D>) {
        super(marshaller, null);
        this.server = this;
    }

    public adtNested(context: C): Promise<AdtA | Adt2> {
        throw new Error('Not implemented.');
    }
}