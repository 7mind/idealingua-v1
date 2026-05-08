// PR-03.3a TypeScript driver — T2 real per-fixture round-trip.
import { Dispatch } from './Dispatch';

type FixtureRequest = { wireId: string; fixturePath: string; fixtureJson: string };
type FixtureResult  = { wireId: string; fixturePath: string; ok: boolean; kind?: string; detail?: string; reEncodedJson?: string };

(async () => {
  const stdin = await new Promise<string>((resolve, reject) => {
    let buf = "";
    process.stdin.setEncoding("utf8");
    process.stdin.on("data", chunk => buf += chunk);
    process.stdin.on("end", () => resolve(buf));
    process.stdin.on("error", reject);
  });
  const batch: { requests: FixtureRequest[] } = JSON.parse(stdin);
  const results: FixtureResult[] = batch.requests.map(req => {
    const entry = Dispatch[req.wireId];
    if (!entry) {
      return { wireId: req.wireId, fixturePath: req.fixturePath, ok: false, kind: "UnknownWireId", detail: `No dispatch entry for ${req.wireId}` };
    }
    let parsed: any;
    try {
      parsed = JSON.parse(req.fixtureJson);
    } catch (e: any) {
      return { wireId: req.wireId, fixturePath: req.fixturePath, ok: false, kind: "DecodeFailed", detail: `parse: ${e.message}` };
    }
    let typedValue: any;
    try {
      typedValue = entry.deserialize(parsed);
    } catch (e: any) {
      return { wireId: req.wireId, fixturePath: req.fixturePath, ok: false, kind: "DecodeFailed", detail: `deserialize: ${e.message}` };
    }
    let reEncodedObj: any;
    try {
      reEncodedObj = entry.serialize(typedValue);
    } catch (e: any) {
      return { wireId: req.wireId, fixturePath: req.fixturePath, ok: false, kind: "DecodeFailed", detail: `serialize: ${e.message}` };
    }
    const reEncodedJson = JSON.stringify(reEncodedObj);
    return { wireId: req.wireId, fixturePath: req.fixturePath, ok: true, reEncodedJson };
  });
  process.stdout.write(JSON.stringify({ results }));
})().catch(e => {
  process.stderr.write(`driver fatal: ${(e as Error).message}\n`);
  process.exit(1);
});
