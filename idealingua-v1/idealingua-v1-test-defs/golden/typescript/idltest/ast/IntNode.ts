// Auto-generated, any modifications may be overwritten in the future.

// IntNode Interface
export interface IntNode {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): IntNodeStructSerialized;

    lit: number;
}

export interface IntNodeStructSerialized {
    lit: number;
}

export class IntNodeStruct implements IntNode {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.ast.IntNode';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.ast.IntNode.Struct';

    public getPackageName(): string { return IntNodeStruct.PackageName; }
    public getClassName(): string { return IntNodeStruct.ClassName; }
    public getFullClassName(): string { return IntNodeStruct.FullClassName; }

    private _lit: number;

    public get lit(): number {
        return this._lit;
    }

    public set lit(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field lit is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field lit expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field lit is expected to be an integer, got ' + value);
        }

        this._lit = value;
    }

    constructor(data: IntNodeStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.lit = data.lit;
    }

    public serialize(): IntNodeStructSerialized {
        return {
            lit: this.lit
        };
    }

    // Polymorphic section below. If a new type to be registered, use IntNodeStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: IntNodeStruct| IntNodeStructSerialized): IntNode}} = {
        // This basic registration will happen below [IntNodeStruct.FullClassName]: IntNodeStruct
    };

    public static register(className: string, ctor: {new (data?: IntNodeStruct| IntNodeStructSerialized): IntNode}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: IntNodeStructSerialized}): IntNode {
        const polymorphicId = Object.keys(data)[0];
        const ctor = IntNodeStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for IntNodeStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(IntNodeStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in IntNodeStruct._knownPolymorphic;
    }
}

IntNodeStruct.register(IntNodeStruct.FullClassName, IntNodeStruct);

// Introspector registration
import {
    Introspector,
    IntrospectorTypes,
    IIntrospectorUserType,
    IIntrospectorGenericType,
    IIntrospectorMapType,
    IIntrospectorMixinObject,
    IIntrospectorDataObject
} from '../../irt';
Introspector.register('idltest.ast.IntNode', {
        full: 'idltest.ast.IntNode',
        short: 'IntNode',
        package: 'idltest.ast',
        type: IntrospectorTypes.Mixin,
        ctor: () => new IntNodeStruct(),
        fields: [
            {
                name: 'lit',
                accessName: 'lit',
                type: {intro: IntrospectorTypes.I32}
            }
        ],
        implementations: IntNodeStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(IntNodeStruct.FullClassName, {
        full: IntNodeStruct.FullClassName,
        short: IntNodeStruct.ClassName,
        package: IntNodeStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new IntNodeStruct(),
        fields: [
            {
                name: 'lit',
                accessName: 'lit',
                type: {intro: IntrospectorTypes.I32}
            }
        ]
    } as IIntrospectorDataObject
);