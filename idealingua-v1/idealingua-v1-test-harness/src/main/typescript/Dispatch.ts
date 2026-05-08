// PR-03.3a TypeScript dispatch table — T2 implementation.
import { Point } from 'idltest/dtofields/Point';
import { NullableObj } from 'idltest/dtofields/NullableObj';
import { OptionalObj } from 'idltest/dtofields/OptionalObj';
import { ListObj } from 'idltest/dtofields/ListObj';
import { ComplexID } from 'idltest/identifiers/ComplexID';
import { UserId } from 'idltest/identifiers/UserId';
import { BucketID } from 'idltest/identifiers/BucketID';
import { KVIDGeneric } from 'idltest/identifiers/KVIDGeneric';
import { DepartmentEnum, DepartmentEnumHelpers } from 'idltest/identifiers/DepartmentEnum';
import { UserWithEnumId } from 'idltest/identifiers/UserWithEnumId';
import { AdtTesterHelpers } from 'idltest/algebraics/AdtTester';
import { AdtWithInterfaceHelpers } from 'idltest/algebraics/AdtWithInterface';
import { WithCovarianceStruct } from 'idltest/inheritance/WithCovariance';
import { EmptyStruct } from 'idltest/inheritance/Empty';
import { JSONLikeHelpers } from 'idltest/json/JSONLike';
import { AllTypesStruct } from 'izumi/test/domain01/AllTypes';
import { Name_incoming } from 'idltest/phase/Name_incoming';

type DispatchEntry = {
  deserialize: (json: any) => any;
  serialize: (val: any) => any;
};

export const Dispatch: Record<string, DispatchEntry> = {
  // 1. Plain DTO mixed scalars
  "idltest.dtofields.Point": {
    deserialize: (json) => new Point(json),
    serialize: (val: Point) => val.serialize(),
  },

  // 2. Nullable DTO (single-field)
  "idltest.dtofields.NullableObj": {
    deserialize: (json) => new NullableObj(json),
    serialize: (val: NullableObj) => val.serialize(),
  },

  // 3. Optional wrapper DTO
  "idltest.dtofields.OptionalObj": {
    deserialize: (json) => new OptionalObj(json),
    serialize: (val: OptionalObj) => val.serialize(),
  },

  // 4. List of structs
  "idltest.dtofields.ListObj": {
    deserialize: (json) => new ListObj(json),
    serialize: (val: ListObj) => val.serialize(),
  },

  // 5. Identifier multi-field (ComplexID — wire form is a string)
  "idltest.identifiers.ComplexID": {
    deserialize: (json) => new ComplexID(json),
    serialize: (val: ComplexID) => val.serialize(),
  },

  // 6. Identifier two-field (UserId — wire form is a string)
  "idltest.identifiers.UserId": {
    deserialize: (json) => new UserId(json),
    serialize: (val: UserId) => val.serialize(),
  },

  // 7. Identifier three-field (BucketID — wire form is a string)
  "idltest.identifiers.BucketID": {
    deserialize: (json) => new BucketID(json),
    serialize: (val: BucketID) => val.serialize(),
  },

  // 8. Map with string keys (KVIDGeneric.test: {[key: string]: BucketID})
  "idltest.identifiers.KVIDGeneric": {
    deserialize: (json) => new KVIDGeneric(json),
    serialize: (val: KVIDGeneric) => val.serialize(),
  },

  // 9. Enum (DepartmentEnum — string encoding)
  "idltest.identifiers.DepartmentEnum": {
    deserialize: (json) => DepartmentEnum[json as keyof typeof DepartmentEnum],
    serialize: (val: DepartmentEnum) => DepartmentEnum[val],
  },

  // 10. Enum inside Identifier (UserWithEnumId — wire form is a string)
  "idltest.identifiers.UserWithEnumId": {
    deserialize: (json) => new UserWithEnumId(json),
    serialize: (val: UserWithEnumId) => val.serialize(),
  },

  // 11. ADT multi-branch (AdtTester — discriminator key pattern)
  "idltest.algebraics.AdtTester": {
    deserialize: (json) => AdtTesterHelpers.deserialize(json),
    serialize: (val) => AdtTesterHelpers.serialize(val),
  },

  // 12. ADT with interface branch (AdtWithInterface — AFace + Success branches)
  "idltest.algebraics.AdtWithInterface": {
    deserialize: (json) => AdtWithInterfaceHelpers.deserialize(json),
    serialize: (val) => AdtWithInterfaceHelpers.serialize(val),
  },

  // 13. Interface w/ implementing DTO — WithCovariance.Struct is the concrete class
  "idltest.inheritance.WithCovariance.Struct": {
    deserialize: (json) => new WithCovarianceStruct(json),
    serialize: (val: WithCovarianceStruct) => val.serialize(),
  },

  // 14. Empty struct concrete class
  "idltest.inheritance.Empty.Struct": {
    deserialize: (json) => new EmptyStruct(json),
    serialize: (val: EmptyStruct) => val.serialize(),
  },

  // 15. JSONLike ADT (recursive JSON-like structure with 6 branches)
  "idltest.json.JSONLike": {
    deserialize: (json) => JSONLikeHelpers.deserialize(json),
    serialize: (val) => JSONLikeHelpers.serialize(val),
  },

  // 16. AllTypes.Struct — mixin struct with all primitive types + temporal fields
  // Note: AllTypesStruct.create() expects a polymorphic-keyed object; use constructor directly.
  "izumi.test.domain01.AllTypes.Struct": {
    deserialize: (json) => new AllTypesStruct(json),
    serialize: (val: AllTypesStruct) => val.serialize(),
  },

  // 17. Cross-domain reference — Name_incoming (idltest.phase)
  "idltest.phase.Name_incoming": {
    deserialize: (json) => new Name_incoming(json),
    serialize: (val: Name_incoming) => val.serialize(),
  },
};
