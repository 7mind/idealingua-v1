// Auto-generated, any modifications may be overwritten in the future.
import {
    SuccessData,
    SuccessDataStruct,
    SuccessDataStructSerialized
} from './SuccessData';

// SuccessDataData DTO
export class SuccessDataData implements SuccessData  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.services';
    public static readonly ClassName = 'SuccessDataData';
    public static readonly FullClassName = 'idltest.services.SuccessDataData';

    public getPackageName(): string { return SuccessDataData.PackageName; }
    public getClassName(): string { return SuccessDataData.ClassName; }
    public getFullClassName(): string { return SuccessDataData.FullClassName; }

    private _greeting: string;

    public get greeting(): string {
        return this._greeting;
    }

    public set greeting(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field greeting is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field greeting expects type string, got ' + value);
        }

        this._greeting = value;
    }

    constructor(data: SuccessDataDataSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.greeting = data.greeting;
    }

    public toSuccessDataSerialized(): SuccessDataStructSerialized {
        return {
            greeting: this.greeting
        };
    }

    public toSuccessData(): SuccessDataStruct {
        return new SuccessDataStruct(this.toSuccessDataSerialized());
    }

    public loadSuccessDataSerialized(slice: SuccessDataStructSerialized) {
        this.greeting = slice.greeting;
    }

    public loadSuccessData(slice: SuccessDataStruct) {
        this.loadSuccessDataSerialized(slice.serialize());
    }

    public serialize(): SuccessDataDataSerialized {
        return {
            greeting: this.greeting
        };
    }
}

export interface SuccessDataDataSerialized extends SuccessDataStructSerialized  {
    greeting: string;
}

SuccessDataStruct.register(SuccessDataData.FullClassName, SuccessDataData);

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorDataObject
} from '../../irt';
Introspector.register(SuccessDataData.FullClassName, {
        full: SuccessDataData.FullClassName,
        short: SuccessDataData.ClassName,
        package: SuccessDataData.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new SuccessDataData(),
        fields: [
            {
                name: 'greeting',
                accessName: 'greeting',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);