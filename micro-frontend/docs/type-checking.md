# Frontend type checking

All frontend type checks use `tsrs`, the Oxc-incubated Rust port of TypeScript 7.1.
The compiler and Vize mapper are pinned in `micro-frontend/package.json` and the root `bun.lock`.
There is no separate TypeScript compiler or `vue-tsc` development dependency.

## Projects and commands

| Project                | Inputs                                                               | Checker          |
| ---------------------- | -------------------------------------------------------------------- | ---------------- |
| `tsconfig.app.json`    | App TypeScript, Vue scripts/templates, generated global declarations | `tsrs` with Vize |
| `tsconfig.vitest.json` | App sources, tests, server helpers, generated global declarations    | `tsrs` with Vize |
| `tsconfig.bun.json`    | Vite config, Bun server, build and smoke scripts                     | `tsrs`           |

From the repository root:

```bash
bun install --frozen-lockfile
bun run --filter megalith-frontend lint
bun run --filter megalith-frontend test:lint
bun run --filter megalith-frontend type-check
bun run --filter megalith-frontend test:type-check
bun run frontend:check
bun run frontend:build
```

The existing `frontend:check`, `quality:ci`, and build commands use this toolchain locally,
in CI, and in the Docker build. `verify` remains the frontend's full-check script.
Vite still compiles the client and SSR bundles; Bun still packages the standalone executable.
Neither the checker nor the mapper is needed in the production image.

## Vue content mapper

The app and Vitest configs register `vize` for `.vue` files through `contentMappers`.
`--runExternalCode` permits the compiler to start this mapper. Vize supplies virtual TypeScript
and source mappings so diagnostics point at the authored Vue file. Keep the generated
`auto-imports.d.ts` and `components.d.ts` in each project's inputs for auto-imported bindings.
Do not restore a wildcard `declare module '*.vue'` fallback: component imports must retain
their actual props and emits rather than a generic component type.

Run the compiler through `bun --bun tsrs`, including for manual checks:

```bash
cd micro-frontend
bun --bun tsrs --runExternalCode --noEmit -p tsconfig.app.json --composite false
```

`--bun` also makes the mapper's Node entrypoint run under Bun, so the build image does not
need a Node installation. Keep `--noEmit` and `--composite false` on these checks to avoid
emitting JavaScript, declarations, or build metadata into the source tree.

## Type-aware linting

`lint` runs the lockfile-installed Oxlint with warnings denied, both locally and in CI.
The workflow does not download a separate Action-selected Oxlint version.
The `OXLINT_TSGOLINT_PATH` environment variable selects `scripts/tsrs-lint.ts`, which launches
the workspace `tsrs-lint` backend under Bun and enables external content mappers. This opt-in
is required even when linting TypeScript that belongs to a project with Vue content mappers.
No separate Go-based `oxlint-tsgolint` package is needed.

The shared Oxlint config enables `typescript/no-floating-promises`,
`typescript/no-misused-promises`, and `typescript/await-thenable` as errors.
The `test:lint` smoke test checks valid and invalid TypeScript under the app, Vitest, and Bun
project settings, and asserts each rule is actually enforced. It runs in `quality:ci`.

Oxlint currently does not send `.vue` scripts to its type-aware backend. Vue files retain
their existing syntax lint rules and the separate `tsrs`/Vize script and template type checks;
these do not enforce the three type-aware lint rules inside Vue files. Do not remove the
separate type-check step or claim Vue type-aware lint coverage based on a successful lint run.
Use `bun run lint` rather than invoking Oxlint directly so the Rust backend is always selected.
Editor integrations need the same `OXLINT_TSGOLINT_PATH` setting (an absolute path is safest).

## Upgrading and editor support

`tsrs` and Vize are experimental and can change diagnostics between releases. Upgrade their
explicit versions through the Bun workspace and review the resulting root lockfile. Use
the compiler's default compatibility mode; do not enable `--fast` without comparing diagnostics.

`test:type-check` checks valid fixtures and deliberately invalid Vue scripts, templates, props,
emits, TypeScript component consumers, and Bun sources. It extends the actual project configs,
asserts diagnostic codes and original filenames, and removes its temporary files afterward.
CI runs it alongside type checks and the existing tests. After upgrades, run the full frontend
check and build as well; do not suppress mapper failures or weaken strictness to obtain a pass.

CLI migration does not automatically replace an IDE's language service. For an editor with
content-mapper support, use the workspace `tsrs --lsp` and enable external mapper execution
according to the editor integration. JetBrains users should keep Vue support enabled; do not
point its legacy TypeScript SDK field at `tsrs` and assume it implements the old JS compiler API.

References: [tsrs usage](https://github.com/oxc-project/tsrs/blob/main/docs/USAGE.md),
[tsrs editor setup](https://github.com/oxc-project/tsrs/blob/main/docs/LSP.md),
[Vize content mapper](https://vizejs.dev/guide/content-mapper/index.html).
