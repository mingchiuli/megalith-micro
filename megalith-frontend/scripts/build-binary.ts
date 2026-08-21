import { mkdir, unlink } from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import type { BunPlugin } from 'bun'

const root = path.resolve(fileURLToPath(new URL('..', import.meta.url)))
const clientRoot = path.join(root, 'dist/client')
const binaryRoot = path.join(root, 'dist/bin')
const binaryPath = path.join(binaryRoot, 'megalith-frontend')
const generatedEntry = path.join(root, 'dist/standalone-entry.ts')
const publicAssetsPath = path.join(clientRoot, '.vite/public-assets.json')
const runtimeAssets = ['node_modules/jsdom/lib/jsdom/browser/default-stylesheet.css']
const cssTreeRoot = path.join(root, 'node_modules/css-tree')

const cssTreeStandalonePlugin: BunPlugin = {
  name: 'css-tree-standalone-data',
  setup(build) {
    build.onLoad(
      {
        filter:
          /[\\/]node_modules[\\/]css-tree[\\/](?:lib[\\/](?:data|version)\.js|cjs[\\/](?:data|version)\.cjs)$/
      },
      async ({ path: modulePath }) => {
        const relativePath = path.relative(cssTreeRoot, modulePath).replaceAll(path.sep, '/')
        const bundledPath = relativePath.replace(/^lib\//, 'dist/').replace(/^cjs\//, 'dist/')

        return {
          contents: await Bun.file(path.join(cssTreeRoot, bundledPath)).text(),
          loader: 'js'
        }
      }
    )
  }
}

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
    plugins: [cssTreeStandalonePlugin],
    compile: {
      outfile: binaryPath,
      assets: [clientRoot, ...runtimeAssets]
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
