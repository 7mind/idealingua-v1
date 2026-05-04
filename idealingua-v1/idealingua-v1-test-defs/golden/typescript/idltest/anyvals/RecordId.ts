// Auto-generated, any modifications may be overwritten in the future.

export class RecordId implements IRecordId {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.anyvals';
    public static readonly ClassName = 'RecordId';
    public static readonly FullClassName = 'idltest.anyvals.RecordId';

    public getPackageName(): string { return RecordId.PackageName; }
    public getClassName(): string { return RecordId.ClassName; }
    public getFullClassName(): string { return RecordId.FullClassName; }

    private _value: string;

    public get value(): string {
        return this._value;
    }

    public set value(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field value is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field value expects type string, got ' + value);
        }

        if (!value.match('^[0-9a-fA-f]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$')) {
            throw new Error('Field value expects guid format, got ' + value);
        }

        this._value = value;
    }

    constructor(data: string | IRecordId = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        if (typeof data === 'string') {
            if (!data.startsWith('RecordId#')) {
                throw new Error('Identifier must start with RecordId, got ' + data);
            }
            const parts = data.substr(data.indexOf('#') + 1).split(':');
            this.value = decodeURIComponent(parts[0]);
        } else {
            this.value = data.value;
        }
    }

    public toString(): string {
        const suffix = encodeURIComponent(this.value);
        return 'RecordId#' + suffix;
    }

    public serialize(): string {
        return this.toString();
    }
}

export interface IRecordId {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): string;

    value: string;
}

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorIdObject
} from '../../irt';
Introspector.register(RecordId.FullClassName, {
        full: RecordId.FullClassName,
        short: RecordId.ClassName,
        package: RecordId.PackageName,
        type: IntrospectorTypes.Id,
        ctor: () => new RecordId(),
        fields: [
            {
                name: 'value',
                accessName: 'value',
                type: {intro: IntrospectorTypes.Uid}
            }
        ]
    } as IIntrospectorIdObject
);