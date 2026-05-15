// Hand-rolled minimal sample app for M5 acceptance of idl-regress (C# adapter).
// Covers the same 3 fixtures as the Scala/TS sample apps:
//   idltest.dtofields.IntPair.Struct × 2 (zero + mixed)
//   idltest.dtofields.WHPair.Struct  × 1 (default)
// A future LLM-generated sample app supersedes this.

using System;
using Newtonsoft.Json;
using Idltest.Dtofields;

namespace IdlRegress.CSharpDriver {
  public static class Program {
    private static readonly JsonSerializerSettings S = new JsonSerializerSettings {
      NullValueHandling = NullValueHandling.Ignore,
      Formatting = Formatting.None,
    };

    public static int Main(string[] args) {
      // idltest.dtofields.IntPair.Struct — two scenarios.
      Emit("idltest.dtofields.IntPair.Struct", "zero",    new IntPairStruct(0,  0));
      Emit("idltest.dtofields.IntPair.Struct", "mixed",   new IntPairStruct(17, -3));
      // idltest.dtofields.WHPair.Struct — one scenario.
      Emit("idltest.dtofields.WHPair.Struct",  "default", new WHPairStruct(640, 480));
      return 0;
    }

    private static void Emit(string wireId, string scenario, object v) {
      var json = JsonConvert.SerializeObject(v, S);
      Console.WriteLine($"{wireId}\t{scenario}\t{json}");
    }
  }
}
