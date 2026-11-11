import assert from 'node:assert/strict'
import { mkdtemp, rm, symlink } from 'node:fs/promises'
import { tmpdir } from 'node:os'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.resolve(fileURLToPath(new URL('..', import.meta.url)))
const directory = await mkdtemp(path.join(tmpdir(), 'megalith-type-check-'))
const tsrs = fileURLToPath(new URL('./bin/tsrs', import.meta.resolve('tsrs/package.json')))
const dependencies = path.dirname(
  path.dirname(fileURLToPath(import.meta.resolve('vue/package.json')))
)

const child = `<script setup lang="ts">
defineProps<{ count: number }>()
const emit = defineEmits<{ change: [value: number] }>()
emit('change', 1)
</script>
<template><span>{{ count.toFixed() }}</span></template>
`
const parent = `<script setup lang="ts">
import Child from './Child.vue'
const count: number = 1
</script>
<template><Child :count="count" />{{ count.toFixed() }}</template>
`

const check = async (expected?: { file: string; code: string }) => {
  const process = Bun.spawn(
    [
      Bun.which('bun') ?? 'bun',
      '--bun',
      tsrs,
      '--runExternalCode',
      '--noEmit',
      '--pretty',
      'false',
      '-p',
      directory
    ],
    { cwd: root, stdout: 'pipe', stderr: 'pipe', timeout: 30_000 }
  )
  const [stdout, stderr, exitCode] = await Promise.all([
    new Response(process.stdout).text(),
    new Response(process.stderr).text(),
    process.exited
  ])
  const output = stdout + stderr
  if (expected) {
    assert.notEqual(exitCode, 0, `Type checker accepted invalid ${expected.file}`)
    assert.ok(
      output
        .split('\n')
        .some((line) => line.includes(expected.file) && line.includes(expected.code)),
      `Expected ${expected.code} mapped to ${expected.file}:\n${output}`
    )
  } else {
    assert.equal(exitCode, 0, output)
  }
}

try {
  await symlink(dependencies, path.join(directory, 'node_modules'), 'dir')
  for (const project of ['app', 'vitest']) {
    await Bun.write(
      path.join(directory, 'tsconfig.json'),
      JSON.stringify({
        extends: path.join(root, `tsconfig.${project}.json`),
        compilerOptions: { composite: false },
        files: ['Parent.vue', 'Child.vue', 'consumer.ts'],
        include: [],
        exclude: []
      })
    )
    await Bun.write(path.join(directory, 'Child.vue'), child)
    await Bun.write(path.join(directory, 'Parent.vue'), parent)
    await Bun.write(
      path.join(directory, 'consumer.ts'),
      `import Child from './Child.vue'
const props: InstanceType<typeof Child>['$props'] = { count: 1 }
export { props }
`
    )
    await check()

    await Bun.write(
      path.join(directory, 'Parent.vue'),
      parent.replace('count: number = 1', "count: number = 'invalid'")
    )
    await check({ file: 'Parent.vue', code: 'TS2322' })
    await Bun.write(
      path.join(directory, 'Parent.vue'),
      parent.replace('count.toFixed()', 'count.missing()')
    )
    await check({ file: 'Parent.vue', code: 'TS2339' })
    await Bun.write(
      path.join(directory, 'Parent.vue'),
      parent.replace(':count="count"', 'count="invalid"')
    )
    await check({ file: 'Parent.vue', code: 'TS2322' })
    await Bun.write(path.join(directory, 'Parent.vue'), parent)

    await Bun.write(
      path.join(directory, 'Child.vue'),
      child.replace("emit('change', 1)", "emit('change', 'invalid')")
    )
    await check({ file: 'Child.vue', code: 'TS2345' })
    await Bun.write(path.join(directory, 'Child.vue'), child)
    await Bun.write(
      path.join(directory, 'consumer.ts'),
      `import Child from './Child.vue'
const props: InstanceType<typeof Child>['$props'] = { count: 'invalid' }
export { props }
`
    )
    await check({ file: 'consumer.ts', code: 'TS2322' })
  }

  await Bun.write(
    path.join(directory, 'tsconfig.json'),
    JSON.stringify({
      extends: path.join(root, 'tsconfig.bun.json'),
      compilerOptions: { composite: false },
      files: ['server.ts'],
      include: [],
      exclude: []
    })
  )
  await Bun.write(path.join(directory, 'server.ts'), 'export const server: typeof Bun = Bun\n')
  await check()
  await Bun.write(path.join(directory, 'server.ts'), "export const port: number = 'invalid'\n")
  await check({ file: 'server.ts', code: 'TS2322' })
  console.log(
    'Type-check smoke passed: Vue scripts, templates, props, emits, TS consumers, and Bun.'
  )
} finally {
  await rm(directory, { recursive: true, force: true })
}
