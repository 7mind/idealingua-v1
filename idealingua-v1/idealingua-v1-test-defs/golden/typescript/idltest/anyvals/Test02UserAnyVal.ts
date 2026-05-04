// Auto-generated, any modifications may be overwritten in the future.
import {
    Test02DtoAnyVal,
    Test02DtoAnyValSerialized
} from './Test02DtoAnyVal';

// Test02UserAnyVal DTO
export class Test02UserAnyVal  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.anyvals';
    public static readonly ClassName = 'Test02UserAnyVal';
    public static readonly FullClassName = 'idltest.anyvals.Test02UserAnyVal';

    public getPackageName(): string { return Test02UserAnyVal.PackageName; }
    public getClassName(): string { return Test02UserAnyVal.ClassName; }
    public getFullClassName(): string { return Test02UserAnyVal.FullClassName; }

    private _test02DtoAnyVal: Test02DtoAnyVal;
    private _i08: number;

    public get test02DtoAnyVal(): Test02DtoAnyVal {
        return this._test02DtoAnyVal;
    }

    public set test02DtoAnyVal(value: Test02DtoAnyVal) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field test02DtoAnyVal is not optional');
        }
        this._test02DtoAnyVal = value;
    }

    public get i08(): number {
        return this._i08;
    }

    public set i08(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field i08 is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field i08 expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field i08 is expected to be an integer, got ' + value);
        }

        if (value < -128) {
            throw new Error('Field i08 is expected to be not less than -128, got ' + value);
        }

        if (value > 127) {
            throw new Error('Field i08 is expected to be not greater than 127, got ' + value);
        }

        this._i08 = value;
    }

    constructor(data: Test02UserAnyValSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.test02DtoAnyVal = new Test02DtoAnyVal(data.test02DtoAnyVal);
        this.i08 = data.i08;
    }

    public serialize(): Test02UserAnyValSerialized {
        return {
            test02DtoAnyVal: this.test02DtoAnyVal.serialize(),
            i08: this.i08
        };
    }
}

export interface Test02UserAnyValSerialized  {
    test02DtoAnyVal: Test02DtoAnyValSerialized;
    i08: number;
}

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorDataObject
} from '../../irt';
Introspector.register(Test02UserAnyVal.FullClassName, {
        full: Test02UserAnyVal.FullClassName,
        short: Test02UserAnyVal.ClassName,
        package: Test02UserAnyVal.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new Test02UserAnyVal(),
        fields: [
            {
                name: 'test02DtoAnyVal',
                accessName: 'test02DtoAnyVal',
                type: {intro: IntrospectorTypes.Data, full: 'idltest.anyvals.Test02DtoAnyVal'} as IIntrospectorUserType
            },
            {
                name: 'i08',
                accessName: 'i08',
                type: {intro: IntrospectorTypes.I08}
            }
        ]
    } as IIntrospectorDataObject
);