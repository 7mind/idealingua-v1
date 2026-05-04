// Auto-generated, any modifications may be overwritten in the future.
import {
    TestDto,
    TestDtoSerialized
} from './TestDto';

// TestOneliners DTO
export class TestOneliners  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.syntax';
    public static readonly ClassName = 'TestOneliners';
    public static readonly FullClassName = 'idltest.syntax.TestOneliners';

    public getPackageName(): string { return TestOneliners.PackageName; }
    public getClassName(): string { return TestOneliners.ClassName; }
    public getFullClassName(): string { return TestOneliners.FullClassName; }

    private _testDto: TestDto;
    private _str: string;
    private _i08: number;

    public get testDto(): TestDto {
        return this._testDto;
    }

    public set testDto(value: TestDto) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field testDto is not optional');
        }
        this._testDto = value;
    }

    public get str(): string {
        return this._str;
    }

    public set str(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field str is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field str expects type string, got ' + value);
        }

        this._str = value;
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

    constructor(data: TestOnelinersSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.testDto = new TestDto(data.testDto);
        this.str = data.str;
        this.i08 = data.i08;
    }

    public serialize(): TestOnelinersSerialized {
        return {
            testDto: this.testDto.serialize(),
            str: this.str,
            i08: this.i08
        };
    }
}

export interface TestOnelinersSerialized  {
    testDto: TestDtoSerialized;
    str: string;
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
Introspector.register(TestOneliners.FullClassName, {
        full: TestOneliners.FullClassName,
        short: TestOneliners.ClassName,
        package: TestOneliners.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new TestOneliners(),
        fields: [
            {
                name: 'testDto',
                accessName: 'testDto',
                type: {intro: IntrospectorTypes.Data, full: 'idltest.syntax.TestDto'} as IIntrospectorUserType
            },
            {
                name: 'str',
                accessName: 'str',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'i08',
                accessName: 'i08',
                type: {intro: IntrospectorTypes.I08}
            }
        ]
    } as IIntrospectorDataObject
);