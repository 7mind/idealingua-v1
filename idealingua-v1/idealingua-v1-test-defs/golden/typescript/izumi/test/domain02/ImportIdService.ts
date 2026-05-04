// Auto-generated, any modifications may be overwritten in the future.
import {
    AdtA2,
    AdtA2Serialized
} from './AdtA2';
import {
    ImportAppId,
    GenericFailureData,
    GenericFailureDataStruct,
    GenericFailureDataStructSerialized,
    GenericFailure,
    GenericFailureSerialized
} from '../domain01';
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

// ImportIdService client
// Models
class InSome implements IncomingData {
    private _id: ImportAppId;
    public get id(): ImportAppId {
        return this._id;
    }

    public set id(value: ImportAppId) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field id is not optional');
        }
        this._id = value;
    }

    constructor(data: InSomeSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.id = new ImportAppId(data.id);
    }

    public serialize(): InSomeSerialized {
        return {
            id: this.id.serialize()
        };
    }
}

interface InSomeSerialized {
    id: string;
}

class InMixi implements IncomingData {
    private _par: GenericFailureData;
    public get par(): GenericFailureData {
        return this._par;
    }

    public set par(value: GenericFailureData) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field par is not optional');
        }
        this._par = value;
    }

    constructor(data: InMixiSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.par = GenericFailureDataStruct.create(data.par);
    }

    public serialize(): InMixiSerialized {
        return {
            par: {[this.par.getFullClassName()]: this.par.serialize()}
        };
    }
}

interface InMixiSerialized {
    par: {[key: string]: GenericFailureDataStructSerialized};
}

type OutMixi = AdtA2 | GenericFailureData;
type OutMixiSerialized = AdtA2Serialized | {[key: string]: GenericFailureDataStructSerialized}

class OutMixiHelpers {
    public static isInstanceOf(o: any): boolean {
        if (!o['getClassName'] || typeof o['getClassName'] !== 'function') {
            return false;
        }
        const fullClassName = o.getFullClassName();
        return o instanceof AdtA2 || GenericFailureDataStruct.isRegisteredType(fullClassName);
    }

    public static serialize(adt: OutMixi): {[key: string]: AdtA2Serialized | GenericFailureDataStructSerialized} {
        let className = adt.getClassName();
        const fullClassName = adt.getFullClassName();
        let serialized: any = undefined;

        if (GenericFailureDataStruct.isRegisteredType(fullClassName)) {
            className = 'GenericFailureData'; serialized = {[fullClassName]: adt.serialize()};
        }

        return {
            [className]: serialized || adt.serialize()
        };
    }

    public static deserialize(data: {[key: string]: AdtA2Serialized | GenericFailureDataStructSerialized}): OutMixi {
        const id = Object.keys(data)[0];
        const content = (data as any)[id];
        switch (id) {
            case 'AdtA2': return new AdtA2(content as any);
            case 'GenericFailureData': return GenericFailureDataStruct.create(content as any);
            default:
                throw new Error('Unknown type id ' + id + ' for OutMixi');
        }
    }
}

class InUpdate implements IncomingData {
    private _id: ImportAppId;
    public get id(): ImportAppId {
        return this._id;
    }

    public set id(value: ImportAppId) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field id is not optional');
        }
        this._id = value;
    }

    constructor(data: InUpdateSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.id = new ImportAppId(data.id);
    }

    public serialize(): InUpdateSerialized {
        return {
            id: this.id.serialize()
        };
    }
}

interface InUpdateSerialized {
    id: string;
}

type OutUpdate = AdtA2 | GenericFailure;
type OutUpdateSerialized = AdtA2Serialized | GenericFailureSerialized

class OutUpdateHelpers {
    public static isInstanceOf(o: any): boolean {
        if (!o['getClassName'] || typeof o['getClassName'] !== 'function') {
            return false;
        }

        return o instanceof AdtA2 || o instanceof GenericFailure;
    }

    public static serialize(adt: OutUpdate): {[key: string]: AdtA2Serialized | GenericFailureSerialized} {
        let className = adt.getClassName();

        return {
            [className]: adt.serialize()
        };
    }

    public static deserialize(data: {[key: string]: AdtA2Serialized | GenericFailureSerialized}): OutUpdate {
        const id = Object.keys(data)[0];
        const content = (data as any)[id];
        switch (id) {
            case 'AdtA2': return new AdtA2(content as any);
            case 'GenericFailure': return new GenericFailure(content as any);
            default:
                throw new Error('Unknown type id ' + id + ' for OutUpdate');
        }
    }
}

// Client
export interface IImportIdServiceClient {
    some(id: ImportAppId): Promise<number>
    mixi(par: GenericFailureData): Promise<AdtA2 | GenericFailureData>
    update(id: ImportAppId): Promise<AdtA2 | GenericFailure>
}

export class ImportIdServiceClient implements IImportIdServiceClient {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain02';
    public static readonly ClassName = 'ImportIdService';
    public static readonly FullClassName = 'izumi.test.domain02.ImportIdService';

    public getPackageName(): string { return ImportIdServiceClient.PackageName; }
    public getClassName(): string { return ImportIdServiceClient.ClassName; }
    public getFullClassName(): string { return ImportIdServiceClient.FullClassName; }

    protected _transport: ClientTransport;

    constructor(transport: ClientTransport) {
        this._transport = transport;
    }

    private send<I extends IncomingData, O extends OutgoingData>(method: string, data: I, inputType: {new(): I}, outputType: {new(data: any): O} ): Promise<O> {
        return new Promise((resolve, reject) => {
            this._transport.send(ImportIdServiceClient.ClassName, method, data)
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
    public some(id: ImportAppId): Promise<number> {
        const __data = new InSome();
        __data.id = id;
        return new Promise((resolve, reject) => {
            this._transport.send(ImportIdServiceClient.ClassName, 'some', __data)
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

    public mixi(par: GenericFailureData): Promise<AdtA2 | GenericFailureData> {
        const __data = new InMixi();
        __data.par = par;
        return new Promise((resolve, reject) => {
            this._transport.send(ImportIdServiceClient.ClassName, 'mixi', __data)
                .then((data: any) => {
                    try {
                        resolve(OutMixiHelpers.deserialize(data));
                    } catch(err) {
                        reject(err);
                    }
                 })
                .catch((err: any) => {
                    reject(err);
                });
        });
    }

    public update(id: ImportAppId): Promise<AdtA2 | GenericFailure> {
        const __data = new InUpdate();
        __data.id = id;
        return new Promise((resolve, reject) => {
            this._transport.send(ImportIdServiceClient.ClassName, 'update', __data)
                .then((data: any) => {
                    try {
                        resolve(OutUpdateHelpers.deserialize(data));
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
export interface IImportIdServiceServer<C> {
    some(context: C, id: ImportAppId): Promise<number>
    mixi(context: C, par: GenericFailureData): Promise<AdtA2 | GenericFailureData>
    update(context: C, id: ImportAppId): Promise<AdtA2 | GenericFailure>
}

export class ImportIdServiceDispatcher<C, D> implements ServiceDispatcher<C, D> {
    private static readonly methods: string[] = [
        "some",
        "mixi",
        "update"
    ];
    protected marshaller: Marshaller<D>;
    protected server: IImportIdServiceServer<C>;

    constructor(marshaller: Marshaller<D>, server: IImportIdServiceServer<C>) {
        this.marshaller = marshaller;
        this.server = server;
    }

    public getSupportedService(): string {
        return 'ImportIdService';
    }

    public getSupportedMethods(): string[] {
        return  ImportIdServiceDispatcher.methods;
    }

    public dispatch(context: C, method: string, data: D | undefined): Promise<D> {
        switch (method) {
            case "some": {
                const obj = new InSome(this.marshaller.Unmarshal<InSomeSerialized>(data));
                return new Promise((resolve, reject) => {
                    try {
                        this.server.some(context, obj.id)
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

            case "mixi": {
                const obj = new InMixi(this.marshaller.Unmarshal<InMixiSerialized>(data));
                return new Promise((resolve, reject) => {
                    try {
                        this.server.mixi(context, obj.par)
                            .then((res: AdtA2 | GenericFailureData) => {
                                const serialized = this.marshaller.Marshal<object>(OutMixiHelpers.serialize(res));
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

            case "update": {
                const obj = new InUpdate(this.marshaller.Unmarshal<InUpdateSerialized>(data));
                return new Promise((resolve, reject) => {
                    try {
                        this.server.update(context, obj.id)
                            .then((res: AdtA2 | GenericFailure) => {
                                const serialized = this.marshaller.Marshal<object>(OutUpdateHelpers.serialize(res));
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
                throw new Error(`Method ${method} is not supported by ImportIdServiceDispatcher.`);
        }
    }
}

// Base Server
export abstract class ImportIdServiceServer<C, D> extends ImportIdServiceDispatcher<C, D> implements IImportIdServiceServer<C> {
    constructor(marshaller: Marshaller<D>) {
        super(marshaller, null);
        this.server = this;
    }

    public some(context: C, id: ImportAppId): Promise<number> {
        throw new Error('Not implemented.');
    }

    public mixi(context: C, par: GenericFailureData): Promise<AdtA2 | GenericFailureData> {
        throw new Error('Not implemented.');
    }

    public update(context: C, id: ImportAppId): Promise<AdtA2 | GenericFailure> {
        throw new Error('Not implemented.');
    }
}