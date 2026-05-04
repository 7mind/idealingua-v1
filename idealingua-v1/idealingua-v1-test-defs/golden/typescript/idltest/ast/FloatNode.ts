// Auto-generated, any modifications may be overwritten in the future.

// FloatNode Interface
export interface FloatNode {
    getPackageName(): string;
    getClassName(): string;
    getFullClassName(): string;
    serialize(): FloatNodeStructSerialized;

    lit: number;
}

export interface FloatNodeStructSerialized {
    lit: number;
}

export class FloatNodeStruct implements FloatNode {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.ast.FloatNode';
    public static readonly ClassName = 'Struct';
    public static readonly FullClassName = 'idltest.ast.FloatNode.Struct';

    public getPackageName(): string { return FloatNodeStruct.PackageName; }
    public getClassName(): string { return FloatNodeStruct.ClassName; }
    public getFullClassName(): string { return FloatNodeStruct.FullClassName; }

    private _lit: number;

    public get lit(): number {
        // Precision: 32
        return this._lit;
    }

    public set lit(value: number) {
        // Precision: 32
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field lit is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field lit expects type number, got ' + value);
        }

        this._lit = value;
    }

    constructor(data: FloatNodeStructSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.lit = data.lit;
    }

    public serialize(): FloatNodeStructSerialized {
        return {
            lit: this.lit
        };
    }

    // Polymorphic section below. If a new type to be registered, use FloatNodeStruct.register method
    // which will add it to the known list. You can also overwrite the existing registrations
    // in order to provide extended functionality on existing models, preserving the original class name.

    private static _knownPolymorphic: {[key: string]: {new (data?: FloatNodeStruct| FloatNodeStructSerialized): FloatNode}} = {
        // This basic registration will happen below [FloatNodeStruct.FullClassName]: FloatNodeStruct
    };

    public static register(className: string, ctor: {new (data?: FloatNodeStruct| FloatNodeStructSerialized): FloatNode}): void {
        this._knownPolymorphic[className] = ctor;
    }

    public static create(data: {[key: string]: FloatNodeStructSerialized}): FloatNode {
        const polymorphicId = Object.keys(data)[0];
        const ctor = FloatNodeStruct._knownPolymorphic[polymorphicId];
        if (!ctor) {
          throw new Error('Unknown polymorphic type ' + polymorphicId + ' for FloatNodeStruct.Create');
        }

        return new ctor(data[polymorphicId]);
    }

    public static getRegisteredTypes(): string[] {
        return Object.keys(FloatNodeStruct._knownPolymorphic);
    }

    public static isRegisteredType(key: string): boolean {
        return key in FloatNodeStruct._knownPolymorphic;
    }
}

FloatNodeStruct.register(FloatNodeStruct.FullClassName, FloatNodeStruct);

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
Introspector.register('idltest.ast.FloatNode', {
        full: 'idltest.ast.FloatNode',
        short: 'FloatNode',
        package: 'idltest.ast',
        type: IntrospectorTypes.Mixin,
        ctor: () => new FloatNodeStruct(),
        fields: [
            {
                name: 'lit',
                accessName: 'lit',
                type: {intro: IntrospectorTypes.F32}
            }
        ],
        implementations: FloatNodeStruct.getRegisteredTypes
    } as IIntrospectorMixinObject
);
Introspector.register(FloatNodeStruct.FullClassName, {
        full: FloatNodeStruct.FullClassName,
        short: FloatNodeStruct.ClassName,
        package: FloatNodeStruct.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new FloatNodeStruct(),
        fields: [
            {
                name: 'lit',
                accessName: 'lit',
                type: {intro: IntrospectorTypes.F32}
            }
        ]
    } as IIntrospectorDataObject
);