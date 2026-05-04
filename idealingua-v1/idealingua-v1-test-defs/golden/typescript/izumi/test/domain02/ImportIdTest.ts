// Auto-generated, any modifications may be overwritten in the future.
import {
    ImportAppId,
    GenericFailure,
    GenericFailureSerialized,
    GenericFailureData,
    GenericFailureDataStruct,
    GenericFailureDataStructSerialized
} from '../domain01';

// ImportIdTest Interface
export interface ImportIdTest {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): ImportIdTestStructSerialized;

    id: ImportAppId;
    fail: GenericFailure;
    mix: GenericFailureData;
}

export interface ImportIdTestStructSerialized {
    id: string;
    fail: GenericFailureSerialized;
    mix: {[key: string]: GenericFailureDataStructSerialized};
}

export class ImportIdTestStruct implements ImportIdTest {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain02.ImportIdTest';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'izumi.test.domain02.ImportIdTest.Struct';

    public getPackageName(): string { return ImportIdTestStruct.PackageName; }
    public getClassName(): string { return ImportIdTestStruct.ClassName; }
    public getFullClassName(): string { return ImportIdTestStruct.FullClassName; }

    private _id: ImportAppId;
    private _fail: GenericFailure;
    private _mix: GenericFailureData;

    public get id(): ImportAppId {
        return this._id;
    }

    public set id(value: ImportAppId) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field id is not optional');
        }
        this._id = value;
    }

    public get fail(): GenericFailure {
        return this._fail;
    }

    public set fail(value: GenericFailure) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field fail is not optional');
        }
        this._fail = value;
    }

    public get mix(): GenericFailureData {
        return this._mix;
    }

    public set mix(value: GenericFailureData) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field mix is not optional');
        }
        this._mix = value;
    }

    constructor(data: ImportIdTestStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.id = new ImportAppId(data.id);
        this.fail = new GenericFailure(data.fail);
        this.mix = GenericFailureDataStruct.create(data.mix);
    }

    public serialize(): ImportIdTestStructSerialized {
        return {
            id: this.id.serialize(),
            fail: this.fail.serialize(),
            mix: {[this.mix.getFullClassName()]: this.mix.serialize()}
        };
    }

    // Polymorphic section below. If a new type to be registered, use ImportIdTestStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: ImportIdTestStruct| ImportIdTestStructSerialized): ImportIdTest}} = {
        // This basic registration will happen below [ImportIdTestStruct.FullClassName]: ImportIdTestStruct
    };

    public static register(className: string, ctor: {new (data?: ImportIdTestStruct| ImportIdTestStructSerialized): ImportIdTest}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: ImportIdTestStructSerialized}): ImportIdTest {
        const polymorphicId = Object.keys(data)[0];
        const ctor = ImportIdTestStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for ImportIdTestStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(ImportIdTestStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in ImportIdTestStruct._knownPolymorphic;
    }
}

ImportIdTestStruct.register(ImportIdTestStruct.FullClassName, ImportIdTestStruct);

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorMixinObject,
    IIntrospectorDataObject
} from '../../../irt';
Introspector.register('izumi.test.domain02.ImportIdTest', {
        full: 'izumi.test.domain02.ImportIdTest',
        short: 'ImportIdTest',
        package: 'izumi.test.domain02',
        type: IntrospectorTypes.Mixin,
        ctor: () => new ImportIdTestStruct(),
        fields: [
            {
                name: 'id',
                accessName: 'id',
                type: {intro: IntrospectorTypes.Id, full: 'izumi.test.domain01.ImportAppId'} as IIntrospectorUserType
            },
            {
                name: 'fail',
                accessName: 'fail',
                type: {intro: IntrospectorTypes.Data, full: 'izumi.test.domain01.GenericFailure'} as IIntrospectorUserType
            },
            {
                name: 'mix',
                accessName: 'mix',
                type: {intro: IntrospectorTypes.Mixin, full: 'izumi.test.domain01.GenericFailureData'} as IIntrospectorUserType
            }
        ],
        implementations: ImportIdTestStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(ImportIdTestStruct.FullClassName, {
        full: ImportIdTestStruct.FullClassName,
        short: ImportIdTestStruct.ClassName,
        package: ImportIdTestStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new ImportIdTestStruct(),
        fields: [
            {
                name: 'id',
                accessName: 'id',
                type: {intro: IntrospectorTypes.Id, full: 'izumi.test.domain01.ImportAppId'} as IIntrospectorUserType
            },
            {
                name: 'fail',
                accessName: 'fail',
                type: {intro: IntrospectorTypes.Data, full: 'izumi.test.domain01.GenericFailure'} as IIntrospectorUserType
            },
            {
                name: 'mix',
                accessName: 'mix',
                type: {intro: IntrospectorTypes.Mixin, full: 'izumi.test.domain01.GenericFailureData'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorDataObject
);