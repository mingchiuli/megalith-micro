#!/usr/bin/env bun
import { fileURLToPath } from 'node:url'

// Oxlint's headless protocol needs explicit permission to start Vue content mappers.
const launcher = fileURLToPath(new URL('./bin/tsrs-lint', import.meta.resolve('tsrs/package.json')))
const result = Bun.spawnSync(
  [process.execPath, '--bun', launcher, ...process.argv.slice(2), '--runExternalCode'],
  { stdin: 'inherit', stdout: 'inherit', stderr: 'inherit' }
)
process.exit(result.exitCode)
