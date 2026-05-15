// Hand-rolled minimal sample app for M4 acceptance of idl-regress (TS adapter).
// Covers the same 3 fixtures as the Scala sample app (sample_app.scala):
//   idltest.dtofields.IntPair.Struct × 2 (zero + mixed)
//   idltest.dtofields.WHPair.Struct  × 1 (default)
// A future LLM-generated sample app supersedes this.

import { IntPairStruct } from './idltest/dtofields/IntPair';
import { WHPairStruct }  from './idltest/dtofields/WHPair';

function emit(wireId: string, scenario: string, value: { serialize(): unknown }): void {
  console.log(`${wireId}\t${scenario}\t${JSON.stringify(value.serialize())}`);
}

// idltest.dtofields.IntPair.Struct — two scenarios.
emit('idltest.dtofields.IntPair.Struct', 'zero',  new IntPairStruct({ x: 0,  y: 0  }));
emit('idltest.dtofields.IntPair.Struct', 'mixed', new IntPairStruct({ x: 17, y: -3 }));

// idltest.dtofields.WHPair.Struct — one scenario.
emit('idltest.dtofields.WHPair.Struct',  'default', new WHPairStruct({ w: 640, h: 480 }));
