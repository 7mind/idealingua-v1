// Auto-generated, any modifications may be overwritten in the future.
import {
    ExtendedMixin,
    ExtendedMixinStruct,
    ExtendedMixinStructSerialized
} from './ExtendedMixin';

// PrivateTestObject DTO
export class PrivateTestObject implements ExtendedMixin  {
    // Runtime identification methods
    public static readonly PackageName = 'izumi.test.domain01';
    public static readonly ClassName = 'PrivateTestObject';
    public static readonly FullClassName = 'izumi.test.domain01.PrivateTestObject';

    public getPackageName(): string { return PrivateTestObject.PackageName; }
    public getClassName(): string { return PrivateTestObject.ClassName; }
    public getFullClassName(): string { return PrivateTestObject.FullClassName; }

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

    constructor(data: PrivateTestObjectSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.parent_embedded = data.parent_embedded;
        this.parent = data.parent;
        this.embedded = data.embedded;
        this.own = data.own;
    }

    public toExtendedMixinSerialized(): ExtendedMixinStructSerialized {
        return {
            parent_embedded: this.parent_embedded,
            parent: this.parent,
            embedded: this.embedded,
            own: this.own
        };
    }

    public toExtendedMixin(): ExtendedMixinStruct {
        return new ExtendedMixinStruct(this.toExtendedMixinSerialized());
    }

    public loadExtendedMixinSerialized(slice: ExtendedMixinStructSerialized) {
        this.parent_embedded = slice.parent_embedded;
        this.parent = slice.parent;
        this.embedded = slice.embedded;
        this.own = slice.own;
    }

    public loadExtendedMixin(slice: ExtendedMixinStruct) {
        this.loadExtendedMixinSerialized(slice.serialize());
    }

    public serialize(): PrivateTestObjectSerialized {
        return {
            parent_embedded: this.parent_embedded,
            parent: this.parent,
            embedded: this.embedded,
            own: this.own
        };
    }
}

export interface PrivateTestObjectSerialized extends ExtendedMixinStructSerialized  {
    parent_embedded: string;
    parent: string;
    embedded: boolean;
    own: number;
}

ExtendedMixinStruct.register(PrivateTestObject.FullClassName, PrivateTestObject);

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorDataObject
} from '../../../irt';
Introspector.register(PrivateTestObject.FullClassName, {
        full: PrivateTestObject.FullClassName,
        short: PrivateTestObject.ClassName,
        package: PrivateTestObject.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new PrivateTestObject(),
        fields: [
            {
                name: 'parent_embedded',
                accessName: 'parent_embedded',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'parent',
                accessName: 'parent',
                type: {intro: IntrospectorTypes.Str}
            },
            {
                name: 'embedded',
                accessName: 'embedded',
                type: {intro: IntrospectorTypes.Bool}
            },
            {
                name: 'own',
                accessName: 'own',
                type: {intro: IntrospectorTypes.I08}
            }
        ]
    } as IIntrospectorDataObject
);