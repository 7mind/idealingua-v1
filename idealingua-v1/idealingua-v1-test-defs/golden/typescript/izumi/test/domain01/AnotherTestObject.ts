// Auto-generated, any modifications may be overwritten in the future.
import {
    PrivateMixinParentStruct,
    PrivateMixinParentStructSerialized
} from './PrivateMixinParent';
import {
    ExtendedMixinStruct,
    ExtendedMixinStructSerialized
} from './ExtendedMixin';
import {
    PrivateMixinStruct,
    PrivateMixinStructSerialized
} from './PrivateMixin';
import {
    PrivateMixinPrivateParentStruct,
    PrivateMixinPrivateParentStructSerialized
} from './PrivateMixinPrivateParent';

// AnotherTestObject DTO
export class AnotherTestObject  {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain01';
    public static readonly ClassName = 'AnotherTestObject';
    public static readonly FullClassName = 'izumi.test.domain01.AnotherTestObject';

    public getPackageName(): string { return AnotherTestObject.PackageName; }
    public getClassName(): string { return AnotherTestObject.ClassName; }
    public getFullClassName(): string { return AnotherTestObject.FullClassName; }

    private _parent_embedded: string;
    private _parent: string;
    private _embedded: boolean;
    private _own: number;

    public get parent_embedded(): string {
        return this._parent_embedded;
    }

    public set parent_embedded(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field parent_embedded is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field parent_embedded expects type string, got ' + value);
        }

        this._parent_embedded = value;
    }

    public get parent(): string {
        return this._parent;
    }

    public set parent(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field parent is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field parent expects type string, got ' + value);
        }

        this._parent = value;
    }

    public get embedded(): boolean {
        return this._embedded;
    }

    public set embedded(value: boolean) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field embedded is not optional');
        }

        if (typeof value !== 'boolean') {
            throw new Error('Field embedded expects boolean type, got ' + value);
        }

        this._embedded = value;
    }

    public get own(): number {
        return this._own;
    }

    public set own(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field own is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field own expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field own is expected to be an integer, got ' + value);
        }

        if (value < -128) {
            throw new Error('Field own is expected to be not less than -128, got ' + value);
        }

        if (value > 127) {
            throw new Error('Field own is expected to be not greater than 127, got ' + value);
        }

        this._own = value;
    }

    constructor(data: AnotherTestObjectSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.parent_embedded = data.parent_embedded;
        this.parent = data.parent;
        this.embedded = data.embedded;
        this.own = data.own;
    }

    public serialize(): AnotherTestObjectSerialized {
        return {
            parent_embedded: this.parent_embedded,
            parent: this.parent,
            embedded: this.embedded,
            own: this.own
        };
    }
}

export interface AnotherTestObjectSerialized  {
    parent_embedded: string;
    parent: string;
    embedded: boolean;
    own: number;
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
Introspector.register(AnotherTestObject.FullClassName, {
        full: AnotherTestObject.FullClassName,
        short: AnotherTestObject.ClassName,
        package: AnotherTestObject.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new AnotherTestObject(),
        fields: [
            {
                name: 'own',
                accessName: 'own',
                type: {intro: IntrospectorTypes.I08}
            },
            {
                name: 'embedded',
                accessName: 'embedded',
                type: {intro: IntrospectorTypes.Bool}
            },
            {
                name: 'parent',
                accessName: 'parent',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'parent_embedded',
                accessName: 'parent_embedded',
                type: {intro: IntrospectorTypes.Str}
            }
        ]
    } as IIntrospectorDataObject
);