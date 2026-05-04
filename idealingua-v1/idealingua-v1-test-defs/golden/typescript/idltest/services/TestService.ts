// Auto-generated, any modifications may be overwritten in the future.
import {
    ErrorData,
    ErrorDataStruct,
    ErrorDataStructSerialized
} from './ErrorData';
import {
    Package,
    PackageSerialized
} from './Package';
import {
    Environment
} from './Environment';
import {
    SuccessDataData,
    SuccessDataDataSerialized
} from './SuccessDataData';
import {
    SuccessData,
    SuccessDataStruct,
    SuccessDataStructSerialized
} from './SuccessData';
import {
    TestServiceEnum
} from './TestServiceEnum';
import {
    ServiceDispatcher,
    Marshaller,
    Void,
    IncomingData,
    OutgoingData,
    ClientTransport,
    Either,
    Left as EitherLeft,
    Right as EitherRight
} from '../../irt'

// TestService client
// Models
class InUnitToUnit implements IncomingData {
    constructor(data: InUnitToUnitSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

    }

    public serialize(): InUnitToUnitSerialized {
        return {
        };
    }
}

interface InUnitToUnitSerialized {
}

class InAnotherVoid implements IncomingData {
    constructor(data: InAnotherVoidSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

    }

    public serialize(): InAnotherVoidSerialized {
        return {
        };
    }
}

interface InAnotherVoidSerialized {
}

export class OutAnotherVoid implements OutgoingData {
    constructor(data: OutAnotherVoidSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

    }

    public serialize(): OutAnotherVoidSerialized {
        return {
        };
    }
}

export interface OutAnotherVoidSerialized {
}

class InUnitResult implements IncomingData {
    private _package: Package;
    public get package_(): Package {
        return this._package;
    }

    public set package_(value: Package) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field package_ is not optional');
        }
        this._package = value;
    }

    constructor(data: InUnitResultSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.package_ = new Package(data.package);
    }

    public serialize(): InUnitResultSerialized {
        return {
            package: this.package_.serialize()
        };
    }
}

interface InUnitResultSerialized {
    package: PackageSerialized;
}

class InParameterless implements IncomingData {
    constructor(data: InParameterlessSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

    }

    public serialize(): InParameterlessSerialized {
        return {
        };
    }
}

interface InParameterlessSerialized {
}

class InSimpleMethod implements IncomingData {
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

    constructor(data: InSimpleMethodSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.a = data.a;
    }

    public serialize(): InSimpleMethodSerialized {
        return {
            a: this.a
        };
    }
}

interface InSimpleMethodSerialized {
    a: string;
}

class InSimpleIntMethod implements IncomingData {
    private _a: number;
    public get a(): number {
        return this._a;
    }

    public set a(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field a is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field a expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field a is expected to be an integer, got ' + value);
        }

        this._a = value;
    }

    constructor(data: InSimpleIntMethodSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.a = data.a;
    }

    public serialize(): InSimpleIntMethodSerialized {
        return {
            a: this.a
        };
    }
}

interface InSimpleIntMethodSerialized {
    a: number;
}

class InSimpleMethodWithGenerics implements IncomingData {
    private _a: string[];
    public get a(): string[] {
        return this._a;
    }

    public set a(value: string[]) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field a is not optional');
        }
        this._a = value;
    }

    constructor(data: InSimpleMethodWithGenericsSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.a = data.a.slice();
    }

    public serialize(): InSimpleMethodWithGenericsSerialized {
        return {
            a: this.a.slice()
        };
    }
}

interface InSimpleMethodWithGenericsSerialized {
    a: string[];
}

class InSimple implements IncomingData {
    constructor(data: InSimpleSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

    }

    public serialize(): InSimpleSerialized {
        return {
        };
    }
}

interface InSimpleSerialized {
}

export class OutSimple implements OutgoingData {
    constructor(data: OutSimpleSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

    }

    public serialize(): OutSimpleSerialized {
        return {
        };
    }
}

export interface OutSimpleSerialized {
}

class InSimpleEnum implements IncomingData {
    private _v: TestServiceEnum;
    public get v(): TestServiceEnum {
        return this._v;
    }

    public set v(value: TestServiceEnum) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field v is not optional');
        }
        this._v = value;
    }

    constructor(data: InSimpleEnumSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.v = TestServiceEnum[data.v as keyof typeof TestServiceEnum];
    }

    public serialize(): InSimpleEnumSerialized {
        return {
            v: TestServiceEnum[this.v]
        };
    }
}

interface InSimpleEnumSerialized {
    v: string;
}

class InSimpleEnum2 implements IncomingData {
    private _e: Environment;
    public get e(): Environment {
        return this._e;
    }

    public set e(value: Environment) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field e is not optional');
        }
        this._e = value;
    }

    constructor(data: InSimpleEnum2Serialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.e = Environment[data.e as keyof typeof Environment];
    }

    public serialize(): InSimpleEnum2Serialized {
        return {
            e: Environment[this.e]
        };
    }
}

interface InSimpleEnum2Serialized {
    e: string;
}

class InReturnsList implements IncomingData {
    private _e: Environment;
    public get e(): Environment {
        return this._e;
    }

    public set e(value: Environment) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field e is not optional');
        }
        this._e = value;
    }

    constructor(data: InReturnsListSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.e = Environment[data.e as keyof typeof Environment];
    }

    public serialize(): InReturnsListSerialized {
        return {
            e: Environment[this.e]
        };
    }
}

interface InReturnsListSerialized {
    e: string;
}

class InReturnsMap implements IncomingData {
    private _e: Environment;
    public get e(): Environment {
        return this._e;
    }

    public set e(value: Environment) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field e is not optional');
        }
        this._e = value;
    }

    constructor(data: InReturnsMapSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.e = Environment[data.e as keyof typeof Environment];
    }

    public serialize(): InReturnsMapSerialized {
        return {
            e: Environment[this.e]
        };
    }
}

interface InReturnsMapSerialized {
    e: string;
}

class InSimpleGoReserved implements IncomingData {
    private _package: Package;
    public get package_(): Package {
        return this._package;
    }

    public set package_(value: Package) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field package_ is not optional');
        }
        this._package = value;
    }

    constructor(data: InSimpleGoReservedSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.package_ = new Package(data.package);
    }

    public serialize(): InSimpleGoReservedSerialized {
        return {
            package: this.package_.serialize()
        };
    }
}

interface InSimpleGoReservedSerialized {
    package: PackageSerialized;
}

class InSimpleVoid implements IncomingData {
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

    constructor(data: InSimpleVoidSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.a = data.a;
    }

    public serialize(): InSimpleVoidSerialized {
        return {
            a: this.a
        };
    }
}

interface InSimpleVoidSerialized {
    a: string;
}

class InGreetSingularOut implements IncomingData {
    private _firstName: string;
    private _secondName: string;
    public get firstName(): string {
        return this._firstName;
    }

    public set firstName(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field firstName is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field firstName expects type string, got ' + value);
        }

        this._firstName = value;
    }

    public get secondName(): string {
        return this._secondName;
    }

    public set secondName(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field secondName is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field secondName expects type string, got ' + value);
        }

        this._secondName = value;
    }

    constructor(data: InGreetSingularOutSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.firstName = data.firstName;
        this.secondName = data.secondName;
    }

    public serialize(): InGreetSingularOutSerialized {
        return {
            firstName: this.firstName,
            secondName: this.secondName
        };
    }
}

interface InGreetSingularOutSerialized {
    firstName: string;
    secondName: string;
}

class InGreetImplicitStructOut implements IncomingData {
    private _firstName: string;
    private _secondName: string;
    public get firstName(): string {
        return this._firstName;
    }

    public set firstName(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field firstName is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field firstName expects type string, got ' + value);
        }

        this._firstName = value;
    }

    public get secondName(): string {
        return this._secondName;
    }

    public set secondName(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field secondName is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field secondName expects type string, got ' + value);
        }

        this._secondName = value;
    }

    constructor(data: InGreetImplicitStructOutSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.firstName = data.firstName;
        this.secondName = data.secondName;
    }

    public serialize(): InGreetImplicitStructOutSerialized {
        return {
            firstName: this.firstName,
            secondName: this.secondName
        };
    }
}

interface InGreetImplicitStructOutSerialized {
    firstName: string;
    secondName: string;
}

export class OutGreetImplicitStructOut implements OutgoingData {
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

    constructor(data: OutGreetImplicitStructOutSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.a = data.a;
    }

    public serialize(): OutGreetImplicitStructOutSerialized {
        return {
            a: this.a
        };
    }
}

export interface OutGreetImplicitStructOutSerialized {
    a: string;
}

class InGreetImplicitStructMultilineSyntax implements IncomingData {
    private _region: string;
    private _age: number;
    public get region(): string {
        return this._region;
    }

    public set region(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field region is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field region expects type string, got ' + value);
        }

        this._region = value;
    }

    public get age(): number {
        return this._age;
    }

    public set age(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field age is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field age expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field age is expected to be an integer, got ' + value);
        }

        if (value < -128) {
            throw new Error('Field age is expected to be not less than -128, got ' + value);
        }

        if (value > 127) {
            throw new Error('Field age is expected to be not greater than 127, got ' + value);
        }

        this._age = value;
    }

    constructor(data: InGreetImplicitStructMultilineSyntaxSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.region = data.region;
        this.age = data.age;
    }

    public serialize(): InGreetImplicitStructMultilineSyntaxSerialized {
        return {
            region: this.region,
            age: this.age
        };
    }
}

interface InGreetImplicitStructMultilineSyntaxSerialized {
    region: string;
    age: number;
}

export class OutGreetImplicitStructMultilineSyntax implements OutgoingData {
    private _bullshit: string;
    public get bullshit(): string {
        return this._bullshit;
    }

    public set bullshit(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field bullshit is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field bullshit expects type string, got ' + value);
        }

        this._bullshit = value;
    }

    constructor(data: OutGreetImplicitStructMultilineSyntaxSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.bullshit = data.bullshit;
    }

    public serialize(): OutGreetImplicitStructMultilineSyntaxSerialized {
        return {
            bullshit: this.bullshit
        };
    }
}

export interface OutGreetImplicitStructMultilineSyntaxSerialized {
    bullshit: string;
}

class InGreetImplicitStructureMultilineCurlyBracesSyntax implements IncomingData {
    private _region: string;
    private _age: number;
    public get region(): string {
        return this._region;
    }

    public set region(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field region is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field region expects type string, got ' + value);
        }

        this._region = value;
    }

    public get age(): number {
        return this._age;
    }

    public set age(value: number) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field age is not optional');
        }

        if (typeof value !== 'number') {
            throw new Error('Field age expects type number, got ' + value);
        }

        if (value % 1 !== 0) {
            throw new Error('Field age is expected to be an integer, got ' + value);
        }

        if (value < -128) {
            throw new Error('Field age is expected to be not less than -128, got ' + value);
        }

        if (value > 127) {
            throw new Error('Field age is expected to be not greater than 127, got ' + value);
        }

        this._age = value;
    }

    constructor(data: InGreetImplicitStructureMultilineCurlyBracesSyntaxSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.region = data.region;
        this.age = data.age;
    }

    public serialize(): InGreetImplicitStructureMultilineCurlyBracesSyntaxSerialized {
        return {
            region: this.region,
            age: this.age
        };
    }
}

interface InGreetImplicitStructureMultilineCurlyBracesSyntaxSerialized {
    region: string;
    age: number;
}

export class OutGreetImplicitStructureMultilineCurlyBracesSyntax implements OutgoingData {
    private _bullshit: string;
    public get bullshit(): string {
        return this._bullshit;
    }

    public set bullshit(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field bullshit is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field bullshit expects type string, got ' + value);
        }

        this._bullshit = value;
    }

    constructor(data: OutGreetImplicitStructureMultilineCurlyBracesSyntaxSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.bullshit = data.bullshit;
    }

    public serialize(): OutGreetImplicitStructureMultilineCurlyBracesSyntaxSerialized {
        return {
            bullshit: this.bullshit
        };
    }
}

export interface OutGreetImplicitStructureMultilineCurlyBracesSyntaxSerialized {
    bullshit: string;
}

class InGreetAlgebraicOut implements IncomingData {
    private _firstName: string;
    private _secondName: string;
    public get firstName(): string {
        return this._firstName;
    }

    public set firstName(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field firstName is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field firstName expects type string, got ' + value);
        }

        this._firstName = value;
    }

    public get secondName(): string {
        return this._secondName;
    }

    public set secondName(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field secondName is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field secondName expects type string, got ' + value);
        }

        this._secondName = value;
    }

    constructor(data: InGreetAlgebraicOutSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.firstName = data.firstName;
        this.secondName = data.secondName;
    }

    public serialize(): InGreetAlgebraicOutSerialized {
        return {
            firstName: this.firstName,
            secondName: this.secondName
        };
    }
}

interface InGreetAlgebraicOutSerialized {
    firstName: string;
    secondName: string;
}

type OutGreetAlgebraicOut = SuccessDataData | ErrorData;
type OutGreetAlgebraicOutSerialized = SuccessDataDataSerialized | {[key: string]: ErrorDataStructSerialized}

class OutGreetAlgebraicOutHelpers {
    public static isInstanceOf(o: any): boolean {
        if (!o['getClassName'] || typeof o['getClassName'] !== 'function') {
            return false;
        }
        const fullClassName = o.getFullClassName();
        return o instanceof SuccessDataData || ErrorDataStruct.isRegisteredType(fullClassName);
    }

    public static serialize(adt: OutGreetAlgebraicOut): {[key: string]: SuccessDataDataSerialized | ErrorDataStructSerialized} {
        let className = adt.getClassName();
        const fullClassName = adt.getFullClassName();
        let serialized: any = undefined;

        if (ErrorDataStruct.isRegisteredType(fullClassName)) {
            className = 'ErrorData'; serialized = {[fullClassName]: adt.serialize()};
        }

        return {
            [className]: serialized || adt.serialize()
        };
    }

    public static deserialize(data: {[key: string]: SuccessDataDataSerialized | ErrorDataStructSerialized}): OutGreetAlgebraicOut {
        const id = Object.keys(data)[0];
        const content = (data as any)[id];
        switch (id) {
            case 'SuccessDataData': return new SuccessDataData(content as any);
            case 'ErrorData': return ErrorDataStruct.create(content as any);
            default:
                throw new Error('Unknown type id ' + id + ' for OutGreetAlgebraicOut');
        }
    }
}

class InGreetAlgebraicMultilineSyntax implements IncomingData {
    private _firstName: string;
    private _secondName: string;
    public get firstName(): string {
        return this._firstName;
    }

    public set firstName(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field firstName is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field firstName expects type string, got ' + value);
        }

        this._firstName = value;
    }

    public get secondName(): string {
        return this._secondName;
    }

    public set secondName(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field secondName is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field secondName expects type string, got ' + value);
        }

        this._secondName = value;
    }

    constructor(data: InGreetAlgebraicMultilineSyntaxSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.firstName = data.firstName;
        this.secondName = data.secondName;
    }

    public serialize(): InGreetAlgebraicMultilineSyntaxSerialized {
        return {
            firstName: this.firstName,
            secondName: this.secondName
        };
    }
}

interface InGreetAlgebraicMultilineSyntaxSerialized {
    firstName: string;
    secondName: string;
}

type OutGreetAlgebraicMultilineSyntax = SuccessDataData | ErrorData;
type OutGreetAlgebraicMultilineSyntaxSerialized = SuccessDataDataSerialized | {[key: string]: ErrorDataStructSerialized}

class OutGreetAlgebraicMultilineSyntaxHelpers {
    public static isInstanceOf(o: any): boolean {
        if (!o['getClassName'] || typeof o['getClassName'] !== 'function') {
            return false;
        }
        const fullClassName = o.getFullClassName();
        return o instanceof SuccessDataData || ErrorDataStruct.isRegisteredType(fullClassName);
    }

    public static serialize(adt: OutGreetAlgebraicMultilineSyntax): {[key: string]: SuccessDataDataSerialized | ErrorDataStructSerialized} {
        let className = adt.getClassName();
        const fullClassName = adt.getFullClassName();
        let serialized: any = undefined;

        if (ErrorDataStruct.isRegisteredType(fullClassName)) {
            className = 'ErrorData'; serialized = {[fullClassName]: adt.serialize()};
        }

        return {
            [className]: serialized || adt.serialize()
        };
    }

    public static deserialize(data: {[key: string]: SuccessDataDataSerialized | ErrorDataStructSerialized}): OutGreetAlgebraicMultilineSyntax {
        const id = Object.keys(data)[0];
        const content = (data as any)[id];
        switch (id) {
            case 'SuccessDataData': return new SuccessDataData(content as any);
            case 'ErrorData': return ErrorDataStruct.create(content as any);
            default:
                throw new Error('Unknown type id ' + id + ' for OutGreetAlgebraicMultilineSyntax');
        }
    }
}

class InAlternative implements IncomingData {
    private _firstName: string;
    private _secondName: string;
    public get firstName(): string {
        return this._firstName;
    }

    public set firstName(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field firstName is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field firstName expects type string, got ' + value);
        }

        this._firstName = value;
    }

    public get secondName(): string {
        return this._secondName;
    }

    public set secondName(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field secondName is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field secondName expects type string, got ' + value);
        }

        this._secondName = value;
    }

    constructor(data: InAlternativeSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.firstName = data.firstName;
        this.secondName = data.secondName;
    }

    public serialize(): InAlternativeSerialized {
        return {
            firstName: this.firstName,
            secondName: this.secondName
        };
    }
}

interface InAlternativeSerialized {
    firstName: string;
    secondName: string;
}

type OutAlternative = Either<ErrorData, SuccessData>;
type OutAlternativeSerialized = {[key in 'Success' | 'Failure']?: any};

class OutAlternativeHelpers {
    public static serialize(either: OutAlternative): OutAlternativeSerialized {
        return either.isRight() ? {
            'Success': {[either.value.getFullClassName()]: either.value.serialize()}
        } : {
            'Failure': {[(either as any).value.getFullClassName()]: (either as any).value.serialize()}
        };
    }

    public static deserialize(data: OutAlternativeSerialized): OutAlternative {
        const id = Object.keys(data)[0];
        const content = (data as any)[id];
        switch (id) {
            case 'Success': return new EitherRight<ErrorData, SuccessData>(SuccessDataStruct.create(content as any));
            case 'Failure': return new EitherLeft<ErrorData, SuccessData>(ErrorDataStruct.create(content as any));
            default: throw new Error(`Unexpected key ${id} in either object.`);
        }
    }
}

class InAlternativeSame implements IncomingData {
    private _firstName: string;
    private _secondName: string;
    public get firstName(): string {
        return this._firstName;
    }

    public set firstName(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field firstName is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field firstName expects type string, got ' + value);
        }

        this._firstName = value;
    }

    public get secondName(): string {
        return this._secondName;
    }

    public set secondName(value: string) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field secondName is not optional');
        }

        if (typeof value !== 'string') {
            throw new Error('Field secondName expects type string, got ' + value);
        }

        this._secondName = value;
    }

    constructor(data: InAlternativeSameSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.firstName = data.firstName;
        this.secondName = data.secondName;
    }

    public serialize(): InAlternativeSameSerialized {
        return {
            firstName: this.firstName,
            secondName: this.secondName
        };
    }
}

interface InAlternativeSameSerialized {
    firstName: string;
    secondName: string;
}

type OutAlternativeSame = Either<SuccessData, SuccessData>;
type OutAlternativeSameSerialized = {[key in 'Success' | 'Failure']?: any};

class OutAlternativeSameHelpers {
    public static serialize(either: OutAlternativeSame): OutAlternativeSameSerialized {
        return either.isRight() ? {
            'Success': {[either.value.getFullClassName()]: either.value.serialize()}
        } : {
            'Failure': {[(either as any).value.getFullClassName()]: (either as any).value.serialize()}
        };
    }

    public static deserialize(data: OutAlternativeSameSerialized): OutAlternativeSame {
        const id = Object.keys(data)[0];
        const content = (data as any)[id];
        switch (id) {
            case 'Success': return new EitherRight<SuccessData, SuccessData>(SuccessDataStruct.create(content as any));
            case 'Failure': return new EitherLeft<SuccessData, SuccessData>(SuccessDataStruct.create(content as any));
            default: throw new Error(`Unexpected key ${id} in either object.`);
        }
    }
}

class InAlternativeGeneric implements IncomingData {
    constructor(data: InAlternativeGenericSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

    }

    public serialize(): InAlternativeGenericSerialized {
        return {
        };
    }
}

interface InAlternativeGenericSerialized {
}

type OutAlternativeGeneric = Either<ErrorData[], SuccessData[]>;
type OutAlternativeGenericSerialized = {[key in 'Success' | 'Failure']?: any};

class OutAlternativeGenericHelpers {
    public static serialize(either: OutAlternativeGeneric): OutAlternativeGenericSerialized {
        return either.isRight() ? {
            'Success': either.value.map((e: any) => { return {[e.getFullClassName()]: e.serialize()}; })
        } : {
            'Failure': (either as any).value.map((e: any) => { return {[e.getFullClassName()]: e.serialize()}; })
        };
    }

    public static deserialize(data: OutAlternativeGenericSerialized): OutAlternativeGeneric {
        const id = Object.keys(data)[0];
        const content = (data as any)[id];
        switch (id) {
            case 'Success': return new EitherRight<ErrorData[], SuccessData[]>(content.map((e: any) => { return SuccessDataStruct.create(e as any); }));
            case 'Failure': return new EitherLeft<ErrorData[], SuccessData[]>(content.map((e: any) => { return ErrorDataStruct.create(e as any); }));
            default: throw new Error(`Unexpected key ${id} in either object.`);
        }
    }
}

class InAlternativeGeneric2 implements IncomingData {
    constructor(data: InAlternativeGeneric2Serialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

    }

    public serialize(): InAlternativeGeneric2Serialized {
        return {
        };
    }
}

interface InAlternativeGeneric2Serialized {
}

type OutAlternativeGeneric2 = Either<{[key: string]: ErrorData}, {[key: string]: SuccessData}>;
type OutAlternativeGeneric2Serialized = {[key in 'Success' | 'Failure']?: any};

class OutAlternativeGeneric2Helpers {
    public static serialize(either: OutAlternativeGeneric2): OutAlternativeGeneric2Serialized {
        return either.isRight() ? {
            'Success': Object.keys(either.value).reduce<any>((previous, current) => {previous[current] = {[either.value[current as any].getFullClassName()]: either.value[current as any].serialize()}; return previous; }, {})
        } : {
            'Failure': Object.keys((either as any).value).reduce<any>((previous, current) => {previous[current] = {[(either as any).value[current as any].getFullClassName()]: (either as any).value[current as any].serialize()}; return previous; }, {})
        };
    }

    public static deserialize(data: OutAlternativeGeneric2Serialized): OutAlternativeGeneric2 {
        const id = Object.keys(data)[0];
        const content = (data as any)[id];
        switch (id) {
            case 'Success': return new EitherRight<{[key: string]: ErrorData}, {[key: string]: SuccessData}>(Object.keys(content).reduce<any>((previous, current) => {previous[current] = SuccessDataStruct.create(content[current as any] as any); return previous; }, {}));
            case 'Failure': return new EitherLeft<{[key: string]: ErrorData}, {[key: string]: SuccessData}>(Object.keys(content).reduce<any>((previous, current) => {previous[current] = ErrorDataStruct.create(content[current as any] as any); return previous; }, {}));
            default: throw new Error(`Unexpected key ${id} in either object.`);
        }
    }
}

// Client
export interface ITestServiceClient {
    unitToUnit(): Promise<void>
    anotherVoid(): Promise<OutAnotherVoid>
    unitResult(package_: Package): Promise<void>
    parameterless(): Promise<string>
    simpleMethod(a: string): Promise<string>
    simpleIntMethod(a: number): Promise<number>
    simpleMethodWithGenerics(a: string[]): Promise<string[]>
    simple(): Promise<OutSimple>
    simpleEnum(v: TestServiceEnum): Promise<string>
    simpleEnum2(e: Environment): Promise<string>
    returnsList(e: Environment): Promise<Package[]>
    returnsMap(e: Environment): Promise<{[key: string]: Package}>
    simpleGoReserved(package_: Package): Promise<boolean>
    simpleVoid(a: string): Promise<void>
    greetSingularOut(firstName: string, secondName: string): Promise<string>
    greetImplicitStructOut(firstName: string, secondName: string): Promise<OutGreetImplicitStructOut>
    greetImplicitStructMultilineSyntax(region: string, age: number): Promise<OutGreetImplicitStructMultilineSyntax>
    greetImplicitStructureMultilineCurlyBracesSyntax(region: string, age: number): Promise<OutGreetImplicitStructureMultilineCurlyBracesSyntax>
    greetAlgebraicOut(firstName: string, secondName: string): Promise<SuccessDataData | ErrorData>
    greetAlgebraicMultilineSyntax(firstName: string, secondName: string): Promise<SuccessDataData | ErrorData>
    alternative(firstName: string, secondName: string): Promise<Either<ErrorData, SuccessData>>
    alternativeSame(firstName: string, secondName: string): Promise<Either<SuccessData, SuccessData>>
    alternativeGeneric(): Promise<Either<ErrorData[], SuccessData[]>>
    alternativeGeneric2(): Promise<Either<{[key: string]: ErrorData}, {[key: string]: SuccessData}>>
}

export class TestServiceClient implements ITestServiceClient {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.services';
    public static readonly ClassName = 'TestService';
    public static readonly FullClassName = 'idltest.services.TestService';

    public getPackageName(): string { return TestServiceClient.PackageName; }
    public getClassName(): string { return TestServiceClient.ClassName; }
    public getFullClassName(): string { return TestServiceClient.FullClassName; }

    protected _transport: ClientTransport;

    constructor(transport: ClientTransport) {
        this._transport = transport;
    }

    private send<I extends IncomingData, O extends OutgoingData>(method: string, data: I, inputType: {new(): I}, outputType: {new(data: any): O} ): Promise<O> {
        return new Promise((resolve, reject) => {
            this._transport.send(TestServiceClient.ClassName, method, data)
                .then((data: any) => {
                    try {
                        const output = new outputType(data);
                        resolve(output);
                    }
                    catch (err) {
                        reject(err);
                    }
                })
                .catch((err: any) => {
                    reject(err);
                });
            });
    }
    public unitToUnit(): Promise<void> {
        const __data = new InUnitToUnit();

        return new Promise((resolve, reject) => {
            this._transport.send(TestServiceClient.ClassName, 'unitToUnit', __data)
                .then(() => {
                  resolve();
                })
                .catch((err: any) => {
                    reject(err);
                });
            });
    }

    public anotherVoid(): Promise<OutAnotherVoid> {
        const __data = new InAnotherVoid();

        return this.send('anotherVoid', __data, InAnotherVoid, OutAnotherVoid);
    }

    public unitResult(package_: Package): Promise<void> {
        const __data = new InUnitResult();
        __data.package_ = package_;
        return new Promise((resolve, reject) => {
            this._transport.send(TestServiceClient.ClassName, 'unitResult', __data)
                .then(() => {
                  resolve();
                })
                .catch((err: any) => {
                    reject(err);
                });
            });
    }

    public parameterless(): Promise<string> {
        const __data = new InParameterless();

        return new Promise((resolve, reject) => {
            this._transport.send(TestServiceClient.ClassName, 'parameterless', __data)
                .then((data: any) => {
                    try {
                        const output = data;
                        resolve(output);
                    }
                    catch(err) {
                        reject(err);
                    }
                })
                .catch((err: any) => {
                    reject(err);
                });
            });
    }

    public simpleMethod(a: string): Promise<string> {
        const __data = new InSimpleMethod();
        __data.a = a;
        return new Promise((resolve, reject) => {
            this._transport.send(TestServiceClient.ClassName, 'simpleMethod', __data)
                .then((data: any) => {
                    try {
                        const output = data;
                        resolve(output);
                    }
                    catch(err) {
                        reject(err);
                    }
                })
                .catch((err: any) => {
                    reject(err);
                });
            });
    }

    public simpleIntMethod(a: number): Promise<number> {
        const __data = new InSimpleIntMethod();
        __data.a = a;
        return new Promise((resolve, reject) => {
            this._transport.send(TestServiceClient.ClassName, 'simpleIntMethod', __data)
                .then((data: any) => {
                    try {
                        const output = data;
                        resolve(output);
                    }
                    catch(err) {
                        reject(err);
                    }
                })
                .catch((err: any) => {
                    reject(err);
                });
            });
    }

    public simpleMethodWithGenerics(a: string[]): Promise<string[]> {
        const __data = new InSimpleMethodWithGenerics();
        __data.a = a;
        return new Promise((resolve, reject) => {
            this._transport.send(TestServiceClient.ClassName, 'simpleMethodWithGenerics', __data)
                .then((data: any) => {
                    try {
                        const output = data.slice();
                        resolve(output);
                    }
                    catch(err) {
                        reject(err);
                    }
                })
                .catch((err: any) => {
                    reject(err);
                });
            });
    }

    public simple(): Promise<OutSimple> {
        const __data = new InSimple();

        return this.send('simple', __data, InSimple, OutSimple);
    }

    public simpleEnum(v: TestServiceEnum): Promise<string> {
        const __data = new InSimpleEnum();
        __data.v = v;
        return new Promise((resolve, reject) => {
            this._transport.send(TestServiceClient.ClassName, 'simpleEnum', __data)
                .then((data: any) => {
                    try {
                        const output = data;
                        resolve(output);
                    }
                    catch(err) {
                        reject(err);
                    }
                })
                .catch((err: any) => {
                    reject(err);
                });
            });
    }

    public simpleEnum2(e: Environment): Promise<string> {
        const __data = new InSimpleEnum2();
        __data.e = e;
        return new Promise((resolve, reject) => {
            this._transport.send(TestServiceClient.ClassName, 'simpleEnum2', __data)
                .then((data: any) => {
                    try {
                        const output = data;
                        resolve(output);
                    }
                    catch(err) {
                        reject(err);
                    }
                })
                .catch((err: any) => {
                    reject(err);
                });
            });
    }

    public returnsList(e: Environment): Promise<Package[]> {
        const __data = new InReturnsList();
        __data.e = e;
        return new Promise((resolve, reject) => {
            this._transport.send(TestServiceClient.ClassName, 'returnsList', __data)
                .then((data: any) => {
                    try {
                        const output = data.map((e: any) => { return new Package(e as any); });
                        resolve(output);
                    }
                    catch(err) {
                        reject(err);
                    }
                })
                .catch((err: any) => {
                    reject(err);
                });
            });
    }

    public returnsMap(e: Environment): Promise<{[key: string]: Package}> {
        const __data = new InReturnsMap();
        __data.e = e;
        return new Promise((resolve, reject) => {
            this._transport.send(TestServiceClient.ClassName, 'returnsMap', __data)
                .then((data: any) => {
                    try {
                        const output = Object.keys(data).reduce<any>((previous, current) => {previous[current] = new Package(data[current as any] as any); return previous; }, {});
                        resolve(output);
                    }
                    catch(err) {
                        reject(err);
                    }
                })
                .catch((err: any) => {
                    reject(err);
                });
            });
    }

    public simpleGoReserved(package_: Package): Promise<boolean> {
        const __data = new InSimpleGoReserved();
        __data.package_ = package_;
        return new Promise((resolve, reject) => {
            this._transport.send(TestServiceClient.ClassName, 'simpleGoReserved', __data)
                .then((data: any) => {
                    try {
                        const output = data;
                        resolve(output);
                    }
                    catch(err) {
                        reject(err);
                    }
                })
                .catch((err: any) => {
                    reject(err);
                });
            });
    }

    public simpleVoid(a: string): Promise<void> {
        const __data = new InSimpleVoid();
        __data.a = a;
        return new Promise((resolve, reject) => {
            this._transport.send(TestServiceClient.ClassName, 'simpleVoid', __data)
                .then(() => {
                  resolve();
                })
                .catch((err: any) => {
                    reject(err);
                });
            });
    }

    public greetSingularOut(firstName: string, secondName: string): Promise<string> {
        const __data = new InGreetSingularOut();
        __data.firstName = firstName;
        __data.secondName = secondName;
        return new Promise((resolve, reject) => {
            this._transport.send(TestServiceClient.ClassName, 'greetSingularOut', __data)
                .then((data: any) => {
                    try {
                        const output = data;
                        resolve(output);
                    }
                    catch(err) {
                        reject(err);
                    }
                })
                .catch((err: any) => {
                    reject(err);
                });
            });
    }

    public greetImplicitStructOut(firstName: string, secondName: string): Promise<OutGreetImplicitStructOut> {
        const __data = new InGreetImplicitStructOut();
        __data.firstName = firstName;
        __data.secondName = secondName;
        return this.send('greetImplicitStructOut', __data, InGreetImplicitStructOut, OutGreetImplicitStructOut);
    }

    public greetImplicitStructMultilineSyntax(region: string, age: number): Promise<OutGreetImplicitStructMultilineSyntax> {
        const __data = new InGreetImplicitStructMultilineSyntax();
        __data.region = region;
        __data.age = age;
        return this.send('greetImplicitStructMultilineSyntax', __data, InGreetImplicitStructMultilineSyntax, OutGreetImplicitStructMultilineSyntax);
    }

    public greetImplicitStructureMultilineCurlyBracesSyntax(region: string, age: number): Promise<OutGreetImplicitStructureMultilineCurlyBracesSyntax> {
        const __data = new InGreetImplicitStructureMultilineCurlyBracesSyntax();
        __data.region = region;
        __data.age = age;
        return this.send('greetImplicitStructureMultilineCurlyBracesSyntax', __data, InGreetImplicitStructureMultilineCurlyBracesSyntax, OutGreetImplicitStructureMultilineCurlyBracesSyntax);
    }

    public greetAlgebraicOut(firstName: string, secondName: string): Promise<SuccessDataData | ErrorData> {
        const __data = new InGreetAlgebraicOut();
        __data.firstName = firstName;
        __data.secondName = secondName;
        return new Promise((resolve, reject) => {
            this._transport.send(TestServiceClient.ClassName, 'greetAlgebraicOut', __data)
                .then((data: any) => {
                    try {
                        resolve(OutGreetAlgebraicOutHelpers.deserialize(data));
                    } catch(err) {
                        reject(err);
                    }
                 })
                .catch((err: any) => {
                    reject(err);
                });
        });
    }

    public greetAlgebraicMultilineSyntax(firstName: string, secondName: string): Promise<SuccessDataData | ErrorData> {
        const __data = new InGreetAlgebraicMultilineSyntax();
        __data.firstName = firstName;
        __data.secondName = secondName;
        return new Promise((resolve, reject) => {
            this._transport.send(TestServiceClient.ClassName, 'greetAlgebraicMultilineSyntax', __data)
                .then((data: any) => {
                    try {
                        resolve(OutGreetAlgebraicMultilineSyntaxHelpers.deserialize(data));
                    } catch(err) {
                        reject(err);
                    }
                 })
                .catch((err: any) => {
                    reject(err);
                });
        });
    }

    public alternative(firstName: string, secondName: string): Promise<Either<ErrorData, SuccessData>> {
        const __data = new InAlternative();
        __data.firstName = firstName;
        __data.secondName = secondName;
        return new Promise((resolve, reject) => {
            this._transport.send(TestServiceClient.ClassName, 'alternative', __data)
                .then((data: any) => {
                    try {
                        resolve(OutAlternativeHelpers.deserialize(data));
                    } catch(err) {
                        reject(err);
                    }
                 })
                .catch((err: any) => {
                    reject(err);
                });
        });
    }

    public alternativeSame(firstName: string, secondName: string): Promise<Either<SuccessData, SuccessData>> {
        const __data = new InAlternativeSame();
        __data.firstName = firstName;
        __data.secondName = secondName;
        return new Promise((resolve, reject) => {
            this._transport.send(TestServiceClient.ClassName, 'alternativeSame', __data)
                .then((data: any) => {
                    try {
                        resolve(OutAlternativeSameHelpers.deserialize(data));
                    } catch(err) {
                        reject(err);
                    }
                 })
                .catch((err: any) => {
                    reject(err);
                });
        });
    }

    public alternativeGeneric(): Promise<Either<ErrorData[], SuccessData[]>> {
        const __data = new InAlternativeGeneric();

        return new Promise((resolve, reject) => {
            this._transport.send(TestServiceClient.ClassName, 'alternativeGeneric', __data)
                .then((data: any) => {
                    try {
                        resolve(OutAlternativeGenericHelpers.deserialize(data));
                    } catch(err) {
                        reject(err);
                    }
                 })
                .catch((err: any) => {
                    reject(err);
                });
        });
    }

    public alternativeGeneric2(): Promise<Either<{[key: string]: ErrorData}, {[key: string]: SuccessData}>> {
        const __data = new InAlternativeGeneric2();

        return new Promise((resolve, reject) => {
            this._transport.send(TestServiceClient.ClassName, 'alternativeGeneric2', __data)
                .then((data: any) => {
                    try {
                        resolve(OutAlternativeGeneric2Helpers.deserialize(data));
                    } catch(err) {
                        reject(err);
                    }
                 })
                .catch((err: any) => {
                    reject(err);
                });
        });
    }
}
// Dispatcher
export interface ITestServiceServer<C> {
    unitToUnit(context: C): Promise<void>
    anotherVoid(context: C): Promise<OutAnotherVoid>
    unitResult(context: C, package_: Package): Promise<void>
    parameterless(context: C): Promise<string>
    simpleMethod(context: C, a: string): Promise<string>
    simpleIntMethod(context: C, a: number): Promise<number>
    simpleMethodWithGenerics(context: C, a: string[]): Promise<string[]>
    simple(context: C): Promise<OutSimple>
    simpleEnum(context: C, v: TestServiceEnum): Promise<string>
    simpleEnum2(context: C, e: Environment): Promise<string>
    returnsList(context: C, e: Environment): Promise<Package[]>
    returnsMap(context: C, e: Environment): Promise<{[key: string]: Package}>
    simpleGoReserved(context: C, package_: Package): Promise<boolean>
    simpleVoid(context: C, a: string): Promise<void>
    greetSingularOut(context: C, firstName: string, secondName: string): Promise<string>
    greetImplicitStructOut(context: C, firstName: string, secondName: string): Promise<OutGreetImplicitStructOut>
    greetImplicitStructMultilineSyntax(context: C, region: string, age: number): Promise<OutGreetImplicitStructMultilineSyntax>
    greetImplicitStructureMultilineCurlyBracesSyntax(context: C, region: string, age: number): Promise<OutGreetImplicitStructureMultilineCurlyBracesSyntax>
    greetAlgebraicOut(context: C, firstName: string, secondName: string): Promise<SuccessDataData | ErrorData>
    greetAlgebraicMultilineSyntax(context: C, firstName: string, secondName: string): Promise<SuccessDataData | ErrorData>
    alternative(context: C, firstName: string, secondName: string): Promise<Either<ErrorData, SuccessData>>
    alternativeSame(context: C, firstName: string, secondName: string): Promise<Either<SuccessData, SuccessData>>
    alternativeGeneric(context: C): Promise<Either<ErrorData[], SuccessData[]>>
    alternativeGeneric2(context: C): Promise<Either<{[key: string]: ErrorData}, {[key: string]: SuccessData}>>
}

export class TestServiceDispatcher<C, D> implements ServiceDispatcher<C, D> {
    private static readonly methods: string[] = [
        "unitToUnit",
        "anotherVoid",
        "unitResult",
        "parameterless",
        "simpleMethod",
        "simpleIntMethod",
        "simpleMethodWithGenerics",
        "simple",
        "simpleEnum",
        "simpleEnum2",
        "returnsList",
        "returnsMap",
        "simpleGoReserved",
        "simpleVoid",
        "greetSingularOut",
        "greetImplicitStructOut",
        "greetImplicitStructMultilineSyntax",
        "greetImplicitStructureMultilineCurlyBracesSyntax",
        "greetAlgebraicOut",
        "greetAlgebraicMultilineSyntax",
        "alternative",
        "alternativeSame",
        "alternativeGeneric",
        "alternativeGeneric2"
    ];
    protected marshaller: Marshaller<D>;
    protected server: ITestServiceServer<C>;

    constructor(marshaller: Marshaller<D>, server: ITestServiceServer<C>) {
        this.marshaller = marshaller;
        this.server = server;
    }

    public getSupportedService(): string {
        return 'TestService';
    }

    public getSupportedMethods(): string[] {
        return  TestServiceDispatcher.methods;
    }

    public dispatch(context: C, method: string, data: D | undefined): Promise<D> {
        switch (method) {
            case "unitToUnit": {
                // No input params for this method
                return new Promise((resolve, reject) => {
                    try {
                        this.server.unitToUnit(context)
                            .then((res: void) => {
                                resolve(this.marshaller.Marshal<Void>(Void.instance));
                            })
                            .catch((err) => {
                                reject(err);
                            });
                    } catch (err) {
                        reject(err);
                    }
                });
            }

            case "anotherVoid": {
                // No input params for this method
                return new Promise((resolve, reject) => {
                    try {
                        this.server.anotherVoid(context)
                            .then((res: OutAnotherVoid) => {
                                const serialized = this.marshaller.Marshal<OutAnotherVoid>(res);
                                resolve(serialized);
                            })
                            .catch((err) => {
                                reject(err);
                            });
                    } catch (err) {
                        reject(err);
                    }
                });
            }

            case "unitResult": {
                const obj = new InUnitResult(this.marshaller.Unmarshal<InUnitResultSerialized>(data));
                return new Promise((resolve, reject) => {
                    try {
                        this.server.unitResult(context, obj.package_)
                            .then((res: void) => {
                                resolve(this.marshaller.Marshal<Void>(Void.instance));
                            })
                            .catch((err) => {
                                reject(err);
                            });
                    } catch (err) {
                        reject(err);
                    }
                });
            }

            case "parameterless": {
                // No input params for this method
                return new Promise((resolve, reject) => {
                    try {
                        this.server.parameterless(context)
                            .then((res: string) => {
                                const serialized = this.marshaller.Marshal<string>(res);
                                resolve(serialized);
                            })
                            .catch((err) => {
                                reject(err);
                            });
                    } catch (err) {
                        reject(err);
                    }
                });
            }

            case "simpleMethod": {
                const obj = new InSimpleMethod(this.marshaller.Unmarshal<InSimpleMethodSerialized>(data));
                return new Promise((resolve, reject) => {
                    try {
                        this.server.simpleMethod(context, obj.a)
                            .then((res: string) => {
                                const serialized = this.marshaller.Marshal<string>(res);
                                resolve(serialized);
                            })
                            .catch((err) => {
                                reject(err);
                            });
                    } catch (err) {
                        reject(err);
                    }
                });
            }

            case "simpleIntMethod": {
                const obj = new InSimpleIntMethod(this.marshaller.Unmarshal<InSimpleIntMethodSerialized>(data));
                return new Promise((resolve, reject) => {
                    try {
                        this.server.simpleIntMethod(context, obj.a)
                            .then((res: number) => {
                                const serialized = this.marshaller.Marshal<number>(res);
                                resolve(serialized);
                            })
                            .catch((err) => {
                                reject(err);
                            });
                    } catch (err) {
                        reject(err);
                    }
                });
            }

            case "simpleMethodWithGenerics": {
                const obj = new InSimpleMethodWithGenerics(this.marshaller.Unmarshal<InSimpleMethodWithGenericsSerialized>(data));
                return new Promise((resolve, reject) => {
                    try {
                        this.server.simpleMethodWithGenerics(context, obj.a)
                            .then((res: string[]) => {
                                const serialized = this.marshaller.Marshal<string[]>(res);
                                resolve(serialized);
                            })
                            .catch((err) => {
                                reject(err);
                            });
                    } catch (err) {
                        reject(err);
                    }
                });
            }

            case "simple": {
                // No input params for this method
                return new Promise((resolve, reject) => {
                    try {
                        this.server.simple(context)
                            .then((res: OutSimple) => {
                                const serialized = this.marshaller.Marshal<OutSimple>(res);
                                resolve(serialized);
                            })
                            .catch((err) => {
                                reject(err);
                            });
                    } catch (err) {
                        reject(err);
                    }
                });
            }

            case "simpleEnum": {
                const obj = new InSimpleEnum(this.marshaller.Unmarshal<InSimpleEnumSerialized>(data));
                return new Promise((resolve, reject) => {
                    try {
                        this.server.simpleEnum(context, obj.v)
                            .then((res: string) => {
                                const serialized = this.marshaller.Marshal<string>(res);
                                resolve(serialized);
                            })
                            .catch((err) => {
                                reject(err);
                            });
                    } catch (err) {
                        reject(err);
                    }
                });
            }

            case "simpleEnum2": {
                const obj = new InSimpleEnum2(this.marshaller.Unmarshal<InSimpleEnum2Serialized>(data));
                return new Promise((resolve, reject) => {
                    try {
                        this.server.simpleEnum2(context, obj.e)
                            .then((res: string) => {
                                const serialized = this.marshaller.Marshal<string>(res);
                                resolve(serialized);
                            })
                            .catch((err) => {
                                reject(err);
                            });
                    } catch (err) {
                        reject(err);
                    }
                });
            }

            case "returnsList": {
                const obj = new InReturnsList(this.marshaller.Unmarshal<InReturnsListSerialized>(data));
                return new Promise((resolve, reject) => {
                    try {
                        this.server.returnsList(context, obj.e)
                            .then((res: Package[]) => {
                                const serialized = this.marshaller.Marshal<Package[]>(res);
                                resolve(serialized);
                            })
                            .catch((err) => {
                                reject(err);
                            });
                    } catch (err) {
                        reject(err);
                    }
                });
            }

            case "returnsMap": {
                const obj = new InReturnsMap(this.marshaller.Unmarshal<InReturnsMapSerialized>(data));
                return new Promise((resolve, reject) => {
                    try {
                        this.server.returnsMap(context, obj.e)
                            .then((res: {[key: string]: Package}) => {
                                const serialized = this.marshaller.Marshal<{[key: string]: Package}>(res);
                                resolve(serialized);
                            })
                            .catch((err) => {
                                reject(err);
                            });
                    } catch (err) {
                        reject(err);
                    }
                });
            }

            case "simpleGoReserved": {
                const obj = new InSimpleGoReserved(this.marshaller.Unmarshal<InSimpleGoReservedSerialized>(data));
                return new Promise((resolve, reject) => {
                    try {
                        this.server.simpleGoReserved(context, obj.package_)
                            .then((res: boolean) => {
                                const serialized = this.marshaller.Marshal<boolean>(res);
                                resolve(serialized);
                            })
                            .catch((err) => {
                                reject(err);
                            });
                    } catch (err) {
                        reject(err);
                    }
                });
            }

            case "simpleVoid": {
                const obj = new InSimpleVoid(this.marshaller.Unmarshal<InSimpleVoidSerialized>(data));
                return new Promise((resolve, reject) => {
                    try {
                        this.server.simpleVoid(context, obj.a)
                            .then((res: void) => {
                                resolve(this.marshaller.Marshal<Void>(Void.instance));
                            })
                            .catch((err) => {
                                reject(err);
                            });
                    } catch (err) {
                        reject(err);
                    }
                });
            }

            case "greetSingularOut": {
                const obj = new InGreetSingularOut(this.marshaller.Unmarshal<InGreetSingularOutSerialized>(data));
                return new Promise((resolve, reject) => {
                    try {
                        this.server.greetSingularOut(context, obj.firstName, obj.secondName)
                            .then((res: string) => {
                                const serialized = this.marshaller.Marshal<string>(res);
                                resolve(serialized);
                            })
                            .catch((err) => {
                                reject(err);
                            });
                    } catch (err) {
                        reject(err);
                    }
                });
            }

            case "greetImplicitStructOut": {
                const obj = new InGreetImplicitStructOut(this.marshaller.Unmarshal<InGreetImplicitStructOutSerialized>(data));
                return new Promise((resolve, reject) => {
                    try {
                        this.server.greetImplicitStructOut(context, obj.firstName, obj.secondName)
                            .then((res: OutGreetImplicitStructOut) => {
                                const serialized = this.marshaller.Marshal<OutGreetImplicitStructOut>(res);
                                resolve(serialized);
                            })
                            .catch((err) => {
                                reject(err);
                            });
                    } catch (err) {
                        reject(err);
                    }
                });
            }

            case "greetImplicitStructMultilineSyntax": {
                const obj = new InGreetImplicitStructMultilineSyntax(this.marshaller.Unmarshal<InGreetImplicitStructMultilineSyntaxSerialized>(data));
                return new Promise((resolve, reject) => {
                    try {
                        this.server.greetImplicitStructMultilineSyntax(context, obj.region, obj.age)
                            .then((res: OutGreetImplicitStructMultilineSyntax) => {
                                const serialized = this.marshaller.Marshal<OutGreetImplicitStructMultilineSyntax>(res);
                                resolve(serialized);
                            })
                            .catch((err) => {
                                reject(err);
                            });
                    } catch (err) {
                        reject(err);
                    }
                });
            }

            case "greetImplicitStructureMultilineCurlyBracesSyntax": {
                const obj = new InGreetImplicitStructureMultilineCurlyBracesSyntax(this.marshaller.Unmarshal<InGreetImplicitStructureMultilineCurlyBracesSyntaxSerialized>(data));
                return new Promise((resolve, reject) => {
                    try {
                        this.server.greetImplicitStructureMultilineCurlyBracesSyntax(context, obj.region, obj.age)
                            .then((res: OutGreetImplicitStructureMultilineCurlyBracesSyntax) => {
                                const serialized = this.marshaller.Marshal<OutGreetImplicitStructureMultilineCurlyBracesSyntax>(res);
                                resolve(serialized);
                            })
                            .catch((err) => {
                                reject(err);
                            });
                    } catch (err) {
                        reject(err);
                    }
                });
            }

            case "greetAlgebraicOut": {
                const obj = new InGreetAlgebraicOut(this.marshaller.Unmarshal<InGreetAlgebraicOutSerialized>(data));
                return new Promise((resolve, reject) => {
                    try {
                        this.server.greetAlgebraicOut(context, obj.firstName, obj.secondName)
                            .then((res: SuccessDataData | ErrorData) => {
                                const serialized = this.marshaller.Marshal<object>(OutGreetAlgebraicOutHelpers.serialize(res));
                                resolve(serialized);
                            })
                            .catch((err) => {
                                reject(err);
                            });
                    } catch (err) {
                        reject(err);
                    }
                });
            }

            case "greetAlgebraicMultilineSyntax": {
                const obj = new InGreetAlgebraicMultilineSyntax(this.marshaller.Unmarshal<InGreetAlgebraicMultilineSyntaxSerialized>(data));
                return new Promise((resolve, reject) => {
                    try {
                        this.server.greetAlgebraicMultilineSyntax(context, obj.firstName, obj.secondName)
                            .then((res: SuccessDataData | ErrorData) => {
                                const serialized = this.marshaller.Marshal<object>(OutGreetAlgebraicMultilineSyntaxHelpers.serialize(res));
                                resolve(serialized);
                            })
                            .catch((err) => {
                                reject(err);
                            });
                    } catch (err) {
                        reject(err);
                    }
                });
            }

            case "alternative": {
                const obj = new InAlternative(this.marshaller.Unmarshal<InAlternativeSerialized>(data));
                return new Promise((resolve, reject) => {
                    try {
                        this.server.alternative(context, obj.firstName, obj.secondName)
                            .then((res: Either<ErrorData, SuccessData>) => {
                                const serialized = this.marshaller.Marshal<object>(OutAlternativeHelpers.serialize(res));
                                resolve(serialized);
                            })
                            .catch((err) => {
                                reject(err);
                            });
                    } catch (err) {
                        reject(err);
                    }
                });
            }

            case "alternativeSame": {
                const obj = new InAlternativeSame(this.marshaller.Unmarshal<InAlternativeSameSerialized>(data));
                return new Promise((resolve, reject) => {
                    try {
                        this.server.alternativeSame(context, obj.firstName, obj.secondName)
                            .then((res: Either<SuccessData, SuccessData>) => {
                                const serialized = this.marshaller.Marshal<object>(OutAlternativeSameHelpers.serialize(res));
                                resolve(serialized);
                            })
                            .catch((err) => {
                                reject(err);
                            });
                    } catch (err) {
                        reject(err);
                    }
                });
            }

            case "alternativeGeneric": {
                // No input params for this method
                return new Promise((resolve, reject) => {
                    try {
                        this.server.alternativeGeneric(context)
                            .then((res: Either<ErrorData[], SuccessData[]>) => {
                                const serialized = this.marshaller.Marshal<object>(OutAlternativeGenericHelpers.serialize(res));
                                resolve(serialized);
                            })
                            .catch((err) => {
                                reject(err);
                            });
                    } catch (err) {
                        reject(err);
                    }
                });
            }

            case "alternativeGeneric2": {
                // No input params for this method
                return new Promise((resolve, reject) => {
                    try {
                        this.server.alternativeGeneric2(context)
                            .then((res: Either<{[key: string]: ErrorData}, {[key: string]: SuccessData}>) => {
                                const serialized = this.marshaller.Marshal<object>(OutAlternativeGeneric2Helpers.serialize(res));
                                resolve(serialized);
                            })
                            .catch((err) => {
                                reject(err);
                            });
                    } catch (err) {
                        reject(err);
                    }
                });
            }

            default:
                throw new Error(`Method ${method} is not supported by TestServiceDispatcher.`);
        }
    }
}

// Base Server
export abstract class TestServiceServer<C, D> extends TestServiceDispatcher<C, D> implements ITestServiceServer<C> {
    constructor(marshaller: Marshaller<D>) {
        super(marshaller, null);
        this.server = this;
    }

    public unitToUnit(context: C): Promise<void> {
        throw new Error('Not implemented.');
    }

    public anotherVoid(context: C): Promise<OutAnotherVoid> {
        throw new Error('Not implemented.');
    }

    public unitResult(context: C, package_: Package): Promise<void> {
        throw new Error('Not implemented.');
    }

    public parameterless(context: C): Promise<string> {
        throw new Error('Not implemented.');
    }

    public simpleMethod(context: C, a: string): Promise<string> {
        throw new Error('Not implemented.');
    }

    public simpleIntMethod(context: C, a: number): Promise<number> {
        throw new Error('Not implemented.');
    }

    public simpleMethodWithGenerics(context: C, a: string[]): Promise<string[]> {
        throw new Error('Not implemented.');
    }

    public simple(context: C): Promise<OutSimple> {
        throw new Error('Not implemented.');
    }

    public simpleEnum(context: C, v: TestServiceEnum): Promise<string> {
        throw new Error('Not implemented.');
    }

    public simpleEnum2(context: C, e: Environment): Promise<string> {
        throw new Error('Not implemented.');
    }

    public returnsList(context: C, e: Environment): Promise<Package[]> {
        throw new Error('Not implemented.');
    }

    public returnsMap(context: C, e: Environment): Promise<{[key: string]: Package}> {
        throw new Error('Not implemented.');
    }

    public simpleGoReserved(context: C, package_: Package): Promise<boolean> {
        throw new Error('Not implemented.');
    }

    public simpleVoid(context: C, a: string): Promise<void> {
        throw new Error('Not implemented.');
    }

    public greetSingularOut(context: C, firstName: string, secondName: string): Promise<string> {
        throw new Error('Not implemented.');
    }

    public greetImplicitStructOut(context: C, firstName: string, secondName: string): Promise<OutGreetImplicitStructOut> {
        throw new Error('Not implemented.');
    }

    public greetImplicitStructMultilineSyntax(context: C, region: string, age: number): Promise<OutGreetImplicitStructMultilineSyntax> {
        throw new Error('Not implemented.');
    }

    public greetImplicitStructureMultilineCurlyBracesSyntax(context: C, region: string, age: number): Promise<OutGreetImplicitStructureMultilineCurlyBracesSyntax> {
        throw new Error('Not implemented.');
    }

    public greetAlgebraicOut(context: C, firstName: string, secondName: string): Promise<SuccessDataData | ErrorData> {
        throw new Error('Not implemented.');
    }

    public greetAlgebraicMultilineSyntax(context: C, firstName: string, secondName: string): Promise<SuccessDataData | ErrorData> {
        throw new Error('Not implemented.');
    }

    public alternative(context: C, firstName: string, secondName: string): Promise<Either<ErrorData, SuccessData>> {
        throw new Error('Not implemented.');
    }

    public alternativeSame(context: C, firstName: string, secondName: string): Promise<Either<SuccessData, SuccessData>> {
        throw new Error('Not implemented.');
    }

    public alternativeGeneric(context: C): Promise<Either<ErrorData[], SuccessData[]>> {
        throw new Error('Not implemented.');
    }

    public alternativeGeneric2(context: C): Promise<Either<{[key: string]: ErrorData}, {[key: string]: SuccessData}>> {
        throw new Error('Not implemented.');
    }
}