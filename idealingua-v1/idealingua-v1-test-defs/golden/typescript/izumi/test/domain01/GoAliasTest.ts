// Auto-generated, any modifications may be overwritten in the future.

// GoAliasTest Interface
export interface GoAliasTest {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): GoAliasTestStructSerialized;

    a: string;
}

export interface GoAliasTestStructSerialized {
    a: string;
}

export class GoAliasTestStruct implements GoAliasTest {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain01.GoAliasTest';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'izumi.test.domain01.GoAliasTest.Struct';

    public getPackageName(): string { return GoAliasTestStruct.PackageName; }
    public getClassName(): string { return GoAliasTestStruct.ClassName; }
    public getFullClassName(): string { return GoAliasTestStruct.FullClassName; }

    private _a: string;

    public get a(): string {
        return this._a;
    }

    public set a(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field a is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field a expects type string, got ' + value);
        }

        this._a = value;
    }

    constructor(data: GoAliasTestStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.a = data.a;
    }

    public serialize(): GoAliasTestStructSerialized {
        return {
            a: this.a
        };
    }

    // Polymorphic section below. If a new type to be registered, use GoAliasTestStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: GoAliasTestStruct| GoAliasTestStructSerialized): GoAliasTest}} = {
        // This basic registration will happen below [GoAliasTestStruct.FullClassName]: GoAliasTestStruct
    };

    public static register(className: string, ctor: {new (data?: GoAliasTestStruct| GoAliasTestStructSerialized): GoAliasTest}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: GoAliasTestStructSerialized}): GoAliasTest {
        const polymorphicId = Object.keys(data)[0];
        const ctor = GoAliasTestStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for GoAliasTestStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(GoAliasTestStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in GoAliasTestStruct._knownPolymorphic;
    }
}

GoAliasTestStruct.register(GoAliasTestStruct.FullClassName, GoAliasTestStruct);

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
Introspector.register('izumi.test.domain01.GoAliasTest', {
        full: 'izumi.test.domain01.GoAliasTest',
        short: 'GoAliasTest',
        package: 'izumi.test.domain01',
        type: IntrospectorTypes.Mixin,
        ctor: () => new GoAliasTestStruct(),
        fields: [
            {
                name: 'a',
                accessName: 'a',
                type: {intro: IntrospectorTypes.Str}
            }
        ],
        implementations: GoAliasTestStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(GoAliasTestStruct.FullClassName, {
        full: GoAliasTestStruct.FullClassName,
        short: GoAliasTestStruct.ClassName,
        package: GoAliasTestStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new GoAliasTestStruct(),
        fields: [
            {
                name: 'a',
                accessName: 'a',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);