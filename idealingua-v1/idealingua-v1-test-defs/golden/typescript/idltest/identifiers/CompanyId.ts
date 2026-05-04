// Auto-generated, any modifications may be overwritten in the future.

export class CompanyId implements ICompanyId {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.identifiers';
    public static readonly ClassName = 'CompanyId';
    public static readonly FullClassName = 'idltest.identifiers.CompanyId';

    public getPackageName(): string { return CompanyId.PackageName; }
    public getClassName(): string { return CompanyId.ClassName; }
    public getFullClassName(): string { return CompanyId.FullClassName; }

    private _value: string;
    private _iid: number;

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

    public get iid(): number {
        return this._iid;
    }

    public set iid(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field iid is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field iid expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field iid is expected to be an integer, got ' + value);
        }

        this._iid = value;
    }

    constructor(data: string | ICompanyId = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        if (typeof data === 'string') {
            if (!data.startsWith('CompanyId#')) {
                throw new Error('Identifier must start with CompanyId, got ' + data);
            }
            const parts = data.substr(data.indexOf('#') + 1).split(':');
            this.iid = parseInt(decodeURIComponent(parts[0]), 10);
            this.value = decodeURIComponent(parts[1]);
        } else {
            this.value = data.value;
            this.iid = data.iid;
        }
    }

    public toString(): string {
        const suffix = encodeURIComponent(this.iid.toString()) + ':' + encodeURIComponent(this.value);
        return 'CompanyId#' + suffix;
    }

    public serialize(): string {
        return this.toString();
    }
}

export interface ICompanyId {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): string;

    value: string;
    iid: number;
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
Introspector.register(CompanyId.FullClassName, {
        full: CompanyId.FullClassName,
        short: CompanyId.ClassName,
        package: CompanyId.PackageName,
        type: IntrospectorTypes.Id,
        ctor: () => new CompanyId(),
        fields: [
            {
                name: 'value',
                accessName: 'value',
                type: {intro: IntrospectorTypes.Uid}
            },
            {
                name: 'iid',
                accessName: 'iid',
                type: {intro: IntrospectorTypes.I64}
            }
        ]
    } as IIntrospectorIdObject
);