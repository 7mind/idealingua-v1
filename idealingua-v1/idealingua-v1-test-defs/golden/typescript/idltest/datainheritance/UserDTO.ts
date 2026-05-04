// Auto-generated, any modifications may be overwritten in the future.
import {
    ParameterDTO,
    ParameterDTOSerialized
} from './ParameterDTO';

// UserDTO DTO
export class UserDTO  {
    // Runtime identification methods
    public static readonly PackageName = 'idltest.datainheritance';
    public static readonly ClassName = 'UserDTO';
    public static readonly FullClassName = 'idltest.datainheritance.UserDTO';

    public getPackageName(): string { return UserDTO.PackageName; }
    public getClassName(): string { return UserDTO.ClassName; }
    public getFullClassName(): string { return UserDTO.FullClassName; }

    private _value: ParameterDTO;

    public get value(): ParameterDTO {
        return this._value;
    }

    public set value(value: ParameterDTO) {
        if (typeof value === 'undefined' || value === null) {
            throw new Error('Field value is not optional');
        }
        this._value = value;
    }

    constructor(data: UserDTOSerialized = undefined) {
        if (typeof data === 'undefined' || data === null) {
            return;
        }

        this.value = new ParameterDTO(data.value);
    }

    public serialize(): UserDTOSerialized {
        return {
            value: this.value.serialize()
        };
    }
}

export interface UserDTOSerialized  {
    value: ParameterDTOSerialized;
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
Introspector.register(UserDTO.FullClassName, {
        full: UserDTO.FullClassName,
        short: UserDTO.ClassName,
        package: UserDTO.PackageName,
        type: IntrospectorTypes.Data,
        ctor: () => new UserDTO(),
        fields: [
            {
                name: 'value',
                accessName: 'value',
                type: {intro: IntrospectorTypes.Data, full: 'idltest.datainheritance.ParameterDTO'} as IIntrospectorUserType
            }
        ]
    } as IIntrospectorDataObject
);