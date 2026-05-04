// Auto-generated, any modifications may be overwritten in the future.
import { Formatter } from '../../../irt';

// GoTimeImportOptional DTO
export class GoTimeImportOptional  {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain01';
    public static readonly ClassName = 'GoTimeImportOptional';
    public static readonly FullClassName = 'izumi.test.domain01.GoTimeImportOptional';

    public getPackageName(): string { return GoTimeImportOptional.PackageName; }
    public getClassName(): string { return GoTimeImportOptional.ClassName; }
    public getFullClassName(): string { return GoTimeImportOptional.FullClassName; }

    private _o: Date | undefined;

    public get o(): Date | undefined {
        return this._o;
    }

    public set o(value: Date | undefined) {
        if (typeof value === 'undefined' || value === null) {
            this._o = undefined;
            return;
        }

        if (!(value instanceof Date)) {
            throw new Error('Field o expects type Date, got ' + value);
        }
        this._o = value;
    }

    public get oAsString(): string | undefined {
        if (!this._o) {
            return undefined;
        }
        return Formatter.writeLocalDateTime(this._o);
    }

    public set oAsString(value: string | undefined) {
        if (typeof value !== 'string') {
            this._o = undefined;
            return;
        }
        this._o = Formatter.readLocalDateTime(value);
    }

    constructor(data: GoTimeImportOptionalSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.oAsString = typeof data.o !== 'undefined' ? data.o : undefined;
    }

    public serialize(): GoTimeImportOptionalSerialized {
        return {
            o: typeof this.o !== 'undefined' ? this.oAsString : undefined
        };
    }
}

export interface GoTimeImportOptionalSerialized  {
    o: string | undefined;
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
Introspector.register(GoTimeImportOptional.FullClassName, {
        full: GoTimeImportOptional.FullClassName,
        short: GoTimeImportOptional.ClassName,
        package: GoTimeImportOptional.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new GoTimeImportOptional(),
        fields: [
            {
                name: 'o',
                accessName: 'o',
                type: {intro: IntrospectorTypes.Opt, value: {intro: IntrospectorTypes.Tsl}} as IIntrospectorGenericType
            }
        ]
    } as IIntrospectorDataObject
);