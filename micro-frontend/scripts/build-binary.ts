import { mkdir, unlink } from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.resolve(fileURLToPath(new URL('..', import.meta.url)))
const clientRoot = path.join(root, 'dist/client')
const binaryRoot = path.join(root, 'dist/bin')
const binaryPath = path.join(binaryRoot, 'megalith-frontend')
const generatedEntry = path.join(root, 'dist/standalone-entry.ts')
const publicAssetsPath = path.join(clientRoot, '.vite/public-assets.json')

const files: string[] = []
const glob = new Bun.Glob('**/*')
for await (const file of glob.scan({ cwd: clientRoot, dot: true, onlyFiles: true })) {
  files.push(file.replaceAll(path.sep, '/'))
}

const publicAssets = Object.fromEntries(
  files
    .filter((file) => file !== 'index.html' && !file.startsWith('.vite/'))
    .sort()
    .map((file) => [`/${file}`, file])
)

await mkdir(path.dirname(publicAssetsPath), { recursive: true })
await mkdir(binaryRoot, { recursive: true })
await Bun.write(publicAssetsPath, `${JSON.stringify(publicAssets, null, 2)}\n`)
await Bun.write(
  generatedEntry,
  `await import('../server/telemetry.ts')
const [{ startProductionServer }, { render }] = await Promise.all([
  import('../server/production.ts'),
  import('./server/entry-server.js')
])
await startProductionServer(render)
`
)

try {
  const result = await Bun.build({
    entrypoints: [generatedEntry],
    target: 'bun',
    format: 'esm',
    sourcemap: 'inline',
    minify: { syntax: true, whitespace: true, identifiers: false },
    compile: {
      outfile: binaryPath,
      assets: [clientRoot]
    }
  })

  if (!result.success) {
    for (const log of result.logs) console.error(log)
    process.exitCode = 1
  } else {
    console.log(`Standalone executable created at ${path.relative(root, binaryPath)}`)
  }
} finally {
  await unlink(generatedEntry).catch(() => undefined)
}
