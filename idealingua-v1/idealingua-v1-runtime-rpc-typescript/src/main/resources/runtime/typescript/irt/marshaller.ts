import { Void } from './void';

export interface Marshaller<T, R = T> {
    Marshal<I>(data: I, toRaw?: boolean): R | T
    Unmarshal<O>(data: R | T, fromRaw?: boolean): O
}

export interface JSONMarshaller extends Marshaller<string, string | object | number | boolean> {
}

export class JSONMarshallerImpl implements JSONMarshaller {
    private pretty: boolean;

    public constructor(pretty: boolean = false) {
        this.pretty = pretty;
    }

    public Marshal<I>(data: I, toRaw?: boolean) {
        if (data instanceof Void) {
            return toRaw ? {} : '{}';
        }

        const dataAny = data as any;
        const serialized = typeof dataAny['serialize'] === 'function' ? dataAny['serialize']() : data;
        if (toRaw) {
            return serialized;
        }

        if (this.pretty) {
            return JSON.stringify(serialized, null, 4);
        } else {
            return JSON.stringify(serialized);
        }
    }

    public Unmarshal<O>(data: string | object | number | boolean, fromRaw?: boolean): O {
        if (fromRaw) {
            // @ts-ignore
            return data as O;
        }
        // @ts-ignore
        return JSON.parse(data as string);
    }
}
