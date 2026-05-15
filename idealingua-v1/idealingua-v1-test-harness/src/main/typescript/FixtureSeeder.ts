// PR-03.3a FixtureSeeder.ts — generates TS wire fixtures from actual encoder output.
// Usage: npx tsx FixtureSeeder.ts <repoRoot>
// Mirrors PR-03.2's FixtureSeeder.scala: kept as a permanent diagnostic tool.
import * as fs from 'fs';
import * as path from 'path';

import { Point } from 'idltest/dtofields/Point';
import { NullableObj } from 'idltest/dtofields/NullableObj';
import { OptionalObj } from 'idltest/dtofields/OptionalObj';
import { ListObj } from 'idltest/dtofields/ListObj';
import { ComplexID } from 'idltest/identifiers/ComplexID';
import { UserId } from 'idltest/identifiers/UserId';
import { BucketID } from 'idltest/identifiers/BucketID';
import { KVIDGeneric } from 'idltest/identifiers/KVIDGeneric';
import { DepartmentEnum } from 'idltest/identifiers/DepartmentEnum';
import { UserWithEnumId } from 'idltest/identifiers/UserWithEnumId';
import { ComplexAdt } from 'idltest/algebraics/ComplexAdt';
import { ComplexAdt2 } from 'idltest/algebraics/ComplexAdt2';
import { AdtTesterHelpers } from 'idltest/algebraics/AdtTester';
import { AdtWithInterfaceHelpers } from 'idltest/algebraics/AdtWithInterface';
import { AFaceStruct } from 'idltest/algebraics/AFace';
import { WithCovarianceStruct } from 'idltest/inheritance/WithCovariance';
import { CovariantStruct } from 'idltest/inheritance/Covariant';
import { EmptyStruct } from 'idltest/inheritance/Empty';
import { JLString } from 'idltest/json/JLString';
import { JSONLikeHelpers } from 'idltest/json/JSONLike';
// AllTypesStruct import skipped — formatter.ts moment() issue under tsx/esbuild; see B4 note below.
import { Name_incoming } from 'idltest/phase/Name_incoming';

const repoRoot = process.argv[2];
if (!repoRoot) {
  console.error('Usage: npx tsx FixtureSeeder.ts <repoRoot>');
  process.exit(1);
}

const fixturesRoot = path.join(repoRoot, 'idealingua-v1/idealingua-v1-test-defs/wire-fixtures/typescript');

function write(wireId: string, scenario: string, value: any): void {
  const dir = path.join(fixturesRoot, wireId);
  fs.mkdirSync(dir, { recursive: true });
  const outPath = path.join(dir, `${scenario}.json`);
  const json = JSON.stringify(value);
  fs.writeFileSync(outPath, json, { encoding: 'utf8' });
  console.log(`  wrote ${wireId}/${scenario}.json  (${json.length} bytes)`);
}

function serializeSingle(val: { serialize(): any }): any {
  return val.serialize();
}

console.log('FixtureSeeder: starting');

// ── B1: Point, NullableObj, Empty ─────────────────────────────────────────────

// 1a. Point basic
{
  const p = new Point({
    w: 10, h: 20, id: 'abc', name: 'point-1', x: 3, y: 4, ownfield: 'of', export: true
  });
  write('idltest.dtofields.Point', 'basic', p.serialize());
}

// 1b. Point extra
{
  const p = new Point({
    w: 0, h: 100, id: 'xyz', name: 'origin', x: 0, y: 0, ownfield: '', export: false
  });
  write('idltest.dtofields.Point', 'extra', p.serialize());
}

// 14. NullableObj
{
  const n = new NullableObj({ a: 99 });
  write('idltest.dtofields.NullableObj', 'basic', n.serialize());
}

// 15. Empty.Struct
{
  const e = new EmptyStruct({});
  write('idltest.inheritance.Empty.Struct', 'empty', e.serialize());
}

// ── B2: ADTs, WithCovariance, JSONLike ─────────────────────────────────────────

// 5a. AdtTester as-ComplexAdt
{
  const adt = new ComplexAdt({ id: 'AdtTestID#alpha' });
  write('idltest.algebraics.AdtTester', 'as-ComplexAdt', AdtTesterHelpers.serialize(adt));
}

// 5b. AdtTester as-ComplexAdt2
{
  const adt = new ComplexAdt2({ id: 'AdtTestID#beta' });
  write('idltest.algebraics.AdtTester', 'as-ComplexAdt2', AdtTesterHelpers.serialize(adt));
}

// 6. AdtWithInterface basic — AFace branch
{
  const aface = AFaceStruct.create({ 'idltest.algebraics.AFace.Struct': { a: 7 } });
  write('idltest.algebraics.AdtWithInterface', 'basic', AdtWithInterfaceHelpers.serialize(aface));
}

// 7. WithCovariance.Struct basic
{
  const cov = CovariantStruct.create({ 'idltest.inheritance.Covariant.Struct': {} });
  const wc = new WithCovarianceStruct({ field: { 'idltest.inheritance.Covariant.Struct': {} } });
  write('idltest.inheritance.WithCovariance.Struct', 'basic', wc.serialize());
}

// 21. JSONLike basic — JLString branch
{
  const jl = new JLString({ value: 'hello' });
  write('idltest.json.JSONLike', 'basic', JSONLikeHelpers.serialize(jl));
}

// ── B3: Optional, List, Map, Enum, phase ─────────────────────────────────────

// 8. OptionalObj with-some
{
  const o = new OptionalObj({ no: { a: 42 } });
  write('idltest.dtofields.OptionalObj', 'with-some', o.serialize());
}

// 9. OptionalObj with-none — TS drops the key (undefined not emitted by JSON.stringify)
{
  const o = new OptionalObj({} as any);
  write('idltest.dtofields.OptionalObj', 'with-none', o.serialize());
}

// 10. ListObj basic
{
  const l = new ListObj({ all: [{ a: 1 }, { a: 2 }] });
  write('idltest.dtofields.ListObj', 'basic', l.serialize());
}

// 11. KVIDGeneric basic
{
  const kv = new KVIDGeneric({
    test: { key1: 'BucketID#3a7f0c12-1234-5678-9abc-fedcba987654:b1:0a1b2c3d-4e5f-6071-8293-a4b5c6d7e8f9' }
  });
  write('idltest.identifiers.KVIDGeneric', 'basic', kv.serialize());
}

// 12. DepartmentEnum basic
{
  const e = DepartmentEnum.Engineering;
  write('idltest.identifiers.DepartmentEnum', 'basic', DepartmentEnum[e]);
}

// 13. UserWithEnumId basic
{
  const u = new UserWithEnumId('UserWithEnumId#0a1b2c3d-4e5f-6071-8293-a4b5c6d7e8f9:Sales:3a7f0c12-1234-5678-9abc-fedcba987654');
  write('idltest.identifiers.UserWithEnumId', 'basic', u.serialize());
}

// 29. Name_incoming basic
{
  const n = new Name_incoming({ name: 'test-name' });
  write('idltest.phase.Name_incoming', 'basic', n.serialize());
}

// ── B1 identifiers ───────────────────────────────────────────────────────────

// 2. ComplexID basic
{
  const c = new ComplexID('ComplexID#BucketID%233a7f0c12-1234-5678-9abc-fedcba987654%3Atest-bucket%3A0a1b2c3d-4e5f-6071-8293-a4b5c6d7e8f9:42:hello:aaaabbbb-cccc-dddd-eeee-ffffaaaabbbb:UserWithEnumId%230a1b2c3d-4e5f-6071-8293-a4b5c6d7e8f9%3AEngineering%3A3a7f0c12-1234-5678-9abc-fedcba987654');
  write('idltest.identifiers.ComplexID', 'basic', c.serialize());
}

// 3. UserId basic
{
  const u = new UserId('UserId#0a1b2c3d-4e5f-6071-8293-a4b5c6d7e8f9:3a7f0c12-1234-5678-9abc-fedcba987654');
  write('idltest.identifiers.UserId', 'basic', u.serialize());
}

// 3b. BucketID basic
{
  const b = new BucketID('BucketID#3a7f0c12-1234-5678-9abc-fedcba987654:my-bucket:0a1b2c3d-4e5f-6071-8293-a4b5c6d7e8f9');
  write('idltest.identifiers.BucketID', 'basic', b.serialize());
}

// ── B4: AllTypes ─────────────────────────────────────────────────────────────
// AllTypes.Struct is SKIPPED in TS fixtures (PR-03.3a).
//
// Root cause: formatter.ts uses `import * as moment from 'moment'`. Under tsx/esbuild CJS
// transform, `import * as` produces `var moment = __toESM(require("moment"))`, where
// __toESM wraps the CJS function in a namespace Object.create(Function.prototype), which
// is non-callable. Node 24 + tsx 4.21 do not set esModuleInterop default behaviour that
// would avoid this. Cannot fix without modifying formatter.ts (a golden file).
// Tracked as F-AllTypes-TS-skip; fix in PR-03.4 (daemon mode allows per-file tsconfig override)
// or when goldens are regenerated with esModuleInterop-aware formatter.

console.log('FixtureSeeder: done');
