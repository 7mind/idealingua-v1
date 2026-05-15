// PR-03.3a TypeScript driver — batch (T2) + PR-03.4 daemon (T1) modes.
import { Dispatch } from './Dispatch';
import * as readline from 'node:readline';

type FixtureRequest = { wireId: string; fixturePath: string; fixtureJson: string };
type FixtureResult  = { wireId: string; fixturePath: string; ok: boolean; kind?: string; detail?: string; reEncodedJson?: string };

function doRoundtrip(wireId: string, json: string): { ok: boolean; kind?: string; detail?: string; reEncodedJson?: string } {
  const entry = Dispatch[wireId];
  if (!entry) {
    return { ok: false, kind: "UnknownWireId", detail: `No dispatch entry for ${wireId}` };
  }
  let parsed: any;
  try { parsed = JSON.parse(json); }
  catch (e: any) { return { ok: false, kind: "DecodeFailed", detail: `parse: ${e.message}` }; }
  let typedValue: any;
  try { typedValue = entry.deserialize(parsed); }
  catch (e: any) { return { ok: false, kind: "DecodeFailed", detail: `deserialize: ${e.message}` }; }
  let reEncodedObj: any;
  try { reEncodedObj = entry.serialize(typedValue); }
  catch (e: any) { return { ok: false, kind: "DecodeFailed", detail: `serialize: ${e.message}` }; }
  return { ok: true, reEncodedJson: JSON.stringify(reEncodedObj) };
}

if (process.argv.includes('--daemon')) {
  // PR-03.4 daemon mode: line-oriented JSON-per-line `roundtrip` + `shutdown`.
  const rl = readline.createInterface({ input: process.stdin, terminal: false });
  rl.on('line', (line) => {
    let req: any;
    try { req = JSON.parse(line); }
    catch (e: any) {
      process.stdout.write(JSON.stringify({ id: null, ok: false, kind: "DecodeFailed", detail: `parse: ${e.message}` }) + '\n');
      return;
    }
    if (req.req === 'shutdown') { rl.close(); process.exit(0); }
    if (req.req === 'roundtrip') {
      const result = doRoundtrip(req.wireId, req.json);
      process.stdout.write(JSON.stringify({ id: req.id, ...result }) + '\n');
    }
  });
} else {
  // PR-03.3a batch mode: read all of stdin, return all results.
  (async () => {
    const stdin = await new Promise<string>((resolve, reject) => {
      let buf = "";
      process.stdin.setEncoding("utf8");
      process.stdin.on("data", chunk => buf += chunk);
      process.stdin.on("end", () => resolve(buf));
      process.stdin.on("error", reject);
    });
    const batch: { requests: FixtureRequest[] } = JSON.parse(stdin);
    const results: FixtureResult[] = batch.requests.map(req => ({
      wireId: req.wireId,
      fixturePath: req.fixturePath,
      ...doRoundtrip(req.wireId, req.fixtureJson),
    }));
    process.stdout.write(JSON.stringify({ results }));
  })().catch(e => {
    process.stderr.write(`driver fatal: ${(e as Error).message}\n`);
    process.exit(1);
  });
}
