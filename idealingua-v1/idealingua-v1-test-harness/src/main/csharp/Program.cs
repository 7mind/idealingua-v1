using System;
using System.IO;
using System.Text;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace IdealinguaV1Harness.CSharpDriver {
    public static class Program {
        public static int Main(string[] args) {
            // T4 smoke test: DriverCrashed
            // Environment.Exit(1);

            // PR-03.4 T1: daemon subcommand — added before existing seed/batch branches.
            if (args.Length == 1 && args[0] == "daemon") {
                using var reader = new StreamReader(Console.OpenStandardInput(), Encoding.UTF8);
                string line;
                while ((line = reader.ReadLine()) != null) {
                    JObject req;
                    try { req = JObject.Parse(line); }
                    catch (Exception e) {
                        Console.Out.WriteLine(new JObject {
                            ["id"] = null, ["ok"] = false, ["kind"] = "DecodeFailed", ["detail"] = $"parse: {e.Message}"
                        }.ToString(Formatting.None));
                        Console.Out.Flush();
                        continue;
                    }
                    var reqType = (string)req["req"];
                    if (reqType == "shutdown") return 0;
                    if (reqType == "roundtrip") {
                        var wireId = (string)req["wireId"];
                        var json   = (string)req["json"];
                        var id     = (string)req["id"];
                        if (!Dispatch.Entries.TryGetValue(wireId, out var entry)) {
                            Console.Out.WriteLine(new JObject {
                                ["id"] = id, ["ok"] = false, ["kind"] = "UnknownWireId", ["detail"] = $"No dispatch entry for {wireId}"
                            }.ToString(Formatting.None));
                            Console.Out.Flush();
                            continue;
                        }
                        object typed;
                        try { typed = entry.Deserialize(json); }
                        catch (Exception e) {
                            Console.Out.WriteLine(new JObject {
                                ["id"] = id, ["ok"] = false, ["kind"] = "DecodeFailed", ["detail"] = $"deserialize: {e.Message}"
                            }.ToString(Formatting.None));
                            Console.Out.Flush();
                            continue;
                        }
                        string reEncodedJson;
                        try { reEncodedJson = entry.Serialize(typed); }
                        catch (Exception e) {
                            Console.Out.WriteLine(new JObject {
                                ["id"] = id, ["ok"] = false, ["kind"] = "DecodeFailed", ["detail"] = $"serialize: {e.Message}"
                            }.ToString(Formatting.None));
                            Console.Out.Flush();
                            continue;
                        }
                        Console.Out.WriteLine(new JObject {
                            ["id"] = id, ["ok"] = true, ["reEncodedJson"] = reEncodedJson
                        }.ToString(Formatting.None));
                        Console.Out.Flush();
                    }
                }
                return 0;
            }

            if (args.Length == 2 && args[0] == "seed") {
                try {
                    FixtureSeeder.Seed(args[1]);
                    return 0;
                } catch (Exception e) {
                    Console.Error.WriteLine($"seeder fatal: {e.Message}\n{e.StackTrace}");
                    return 1;
                }
            }

            try {
                using var reader = new StreamReader(Console.OpenStandardInput(), Encoding.UTF8);
                var stdin = reader.ReadToEnd();
                var batch = JObject.Parse(stdin);
                var requests = (JArray)batch["requests"];
                var results = new JArray();

                foreach (JObject req in requests) {
                    var wireId      = (string)req["wireId"];
                    var fixturePath = (string)req["fixturePath"];
                    var fixtureJson = (string)req["fixtureJson"];

                    if (!Dispatch.Entries.TryGetValue(wireId, out var entry)) {
                        results.Add(new JObject {
                            ["wireId"]      = wireId,
                            ["fixturePath"] = fixturePath,
                            ["ok"]          = false,
                            ["kind"]        = "UnknownWireId",
                            ["detail"]      = $"No dispatch entry for {wireId}",
                        });
                        continue;
                    }

                    object typedValue;
                    try {
                        typedValue = entry.Deserialize(fixtureJson);
                    } catch (Exception e) {
                        results.Add(new JObject {
                            ["wireId"]      = wireId,
                            ["fixturePath"] = fixturePath,
                            ["ok"]          = false,
                            ["kind"]        = "DecodeFailed",
                            ["detail"]      = $"deserialize: {e.Message}",
                        });
                        continue;
                    }

                    string reEncodedJson;
                    try {
                        reEncodedJson = entry.Serialize(typedValue);
                    } catch (Exception e) {
                        results.Add(new JObject {
                            ["wireId"]      = wireId,
                            ["fixturePath"] = fixturePath,
                            ["ok"]          = false,
                            ["kind"]        = "DecodeFailed",
                            ["detail"]      = $"serialize: {e.Message}",
                        });
                        continue;
                    }

                    results.Add(new JObject {
                        ["wireId"]        = wireId,
                        ["fixturePath"]   = fixturePath,
                        ["ok"]            = true,
                        ["reEncodedJson"] = reEncodedJson,
                    });
                }

                var response = new JObject { ["results"] = results };
                Console.Out.Write(response.ToString(Formatting.None));
                return 0;
            } catch (Exception e) {
                Console.Error.WriteLine($"driver fatal: {e.Message}");
                return 1;
            }
        }
    }
}
