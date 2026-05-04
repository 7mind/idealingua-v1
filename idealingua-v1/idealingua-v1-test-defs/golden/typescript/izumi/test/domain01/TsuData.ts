// Auto-generated, any modifications may be overwritten in the future.
import { Formatter } from '../../../irt';

// TsuData DTO
export class TsuData  {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain01';
    public static readonly ClassName = 'TsuData';
    public static readonly FullClassName = 'izumi.test.domain01.TsuData';

    public getPackageName(): string { return TsuData.PackageName; }
    public getClassName(): string { return TsuData.ClassName; }
    public getFullClassName(): string { return TsuData.FullClassName; }

    private _since: Date;

    public get since(): Date {
        return this._since;
    }

    public set since(value: Date) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field since is not optional');
        }

        if (!(value instanceof Date)) {
            throw new Error('Field since expects type Date, got ' + value);
        }
        this._since = value;
    }

    public get sinceAsString(): string {
        return Formatter.writeUTCDateTime(this._since);
    }

    public set sinceAsString(value: string) {
        if (typeof value !== 'string') {
            throw new Error('sinceAsString expects type string, got ' + value);
        }
        this._since = Formatter.readUTCDateTime(value);
    }

    constructor(data: TsuDataSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.sinceAsString = data.since;
    }

    public serialize(): TsuDataSerialized {
        return {
            since: this.sinceAsString
        };
    }
}

export interface TsuDataSerialized  {
    since: string;
}

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorDataObject
} from '../../../irt';
Introspector.register(TsuData.FullClassName, {
        full: TsuData.FullClassName,
        short: TsuData.ClassName,
        package: TsuData.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new TsuData(),
        fields: [
            {
                name: 'since',
                accessName: 'since',
                type: {intro: IntrospectorTypes.Tsu}
            }
        ]
    } as IIntrospectorDataObject
);