import assert from 'node:assert/strict'
import { mkdtemp, rm, symlink } from 'node:fs/promises'
import { tmpdir } from 'node:os'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.resolve(fileURLToPath(new URL('..', import.meta.url)))
const directory = await mkdtemp(path.join(tmpdir(), 'megalith-lint-'))
const oxlint = fileURLToPath(new URL('./bin/oxlint', import.meta.resolve('oxlint/package.json')))
const dependencies = path.dirname(
  path.dirname(fileURLToPath(import.meta.resolve('vue/package.json')))
)
const sources = [
  { code: 'const task = (): Promise<void> => Promise.resolve()\nvoid task()\n' },
  {
    code: 'const task = (): Promise<void> => Promise.resolve()\ntask()\n',
    rule: 'no-floating-promises'
  },
  {
    code: 'const run = (callback: () => void) => callback()\nrun(async () => { await Promise.resolve() })\n',
    rule: 'no-misused-promises'
  },
  { code: 'async function run() { await 1 }\nvoid run()\n', rule: 'await-thenable' }
]

try {
  await symlink(dependencies, path.join(directory, 'node_modules'), 'dir')
  for (const project of ['app', 'vitest', 'bun']) {
    await Bun.write(
      path.join(directory, 'tsconfig.json'),
      JSON.stringify({
        extends: path.join(root, `tsconfig.${project}.json`),
        compilerOptions: { composite: false },
        include: ['./*'],
        exclude: []
      })
    )
    const file = path.join(directory, 'Example.ts')
    for (const { code, rule } of sources) {
      const source = code + 'export {}\n'
      await Bun.write(file, source)
      const child = Bun.spawn(
        [
          process.execPath,
          '--bun',
          oxlint,
          '--config',
          path.join(root, '.oxlintrc.json'),
          '--deny-warnings',
          '--format',
          'json',
          file
        ],
        {
          cwd: directory,
          env: { ...process.env, OXLINT_TSGOLINT_PATH: path.join(root, 'scripts/tsrs-lint.ts') },
          stdout: 'pipe',
          stderr: 'pipe',
          timeout: 30_000
        }
      )
      const [stdout, stderr, exitCode] = await Promise.all([
        new Response(child.stdout).text(),
        new Response(child.stderr).text(),
        child.exited
      ])
      const output = stdout + stderr
      if (rule) {
        assert.notEqual(exitCode, 0, `Type-aware lint accepted ${rule} in ${project}`)
        assert.ok(output.includes(rule) && output.includes('Example.ts'), output)
      } else {
        assert.equal(exitCode, 0, output)
      }
    }
    await rm(file)
  }
  console.log('Type-aware lint smoke passed for app, test, and Bun TypeScript projects.')
} finally {
  await rm(directory, { recursive: true, force: true })
}
