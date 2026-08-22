# TS7 迁移清单

> 状态：**等待中**。项目已就绪，但 `vue-tsc` 尚无官方 TS7 支持，等 TS 7.1（编译器 API 回归）落地后再执行。

## 当前状态快照

| 项         | 值                                                |
| ---------- | ------------------------------------------------- |
| typescript | `~6.0.3`（JS 版 `tsc`）                           |
| vue-tsc    | `^3.3.9`                                          |
| 包管理     | Bun 1.4 单仓（无 workspaces）                     |
| 构建       | Vite 8（client + SSR），Bun standalone executable |
| 类型检查   | `vue-tsc`（app/test）、`tsc`（bun/server）        |
| 编辑器     | JetBrains 环境（`IdeaProjects/`）                 |

## 评估结论（2026-08-17）

源码与配置对 TS 7 已完全就绪，**唯一硬阻塞是工具链**：

- ✅ 已在 TS 6.0.3（官方迁移路径要求先过 TS6）
- ✅ 无 `const enum`、namespace、自定义 transformer、旧式 `moduleResolution`（app/server 均为 `Bundler`）
- ✅ tsconfig 全链（含 `@vue/tsconfig` 与 Bun 类型）不含任何 TS7 移除的选项（兼容性扫描零命中）
- ✅ CSS 副作用导入（`entry-client.ts`、`EditorItem.vue`、`BlogView.vue` 等）由 `env.d.ts` 的 `vite/client` 声明覆盖，新默认 `noUncheckedSideEffectImports` 不会误报
- ✅ ESLint 为纯语法检查（`vueTsConfigs.recommended` 不开 type-aware），不受编译器 API 移除影响
- ✅ 客户端/SSR 构建走 Vite/esbuild，与 TS 编译器无关
- ⛔ **阻塞**：`vue-tsc`/Volar 靠运行时补丁 JS 版 `tsc.js` 识别 `.vue`，TS7 原生 Go 二进制无法如此工作。TS 7.0 不暴露可编程 Compiler API（推迟到 7.1），vue-tsc 官方支持要等 7.1
- ⛔ **编辑器**：JetBrains 暂无原生 tsgo 语言服务；VS Code 需装 "TypeScript Native Preview" 扩展

## 届时迁移方案（双装）

1. `typescript` 改为指向 JS API 垫片（供 vue-tsc + 编辑器语言服务）：
   ```bash
   bun add -d typescript@npm:@typescript/typescript6@^6.0.2
   ```
2. 新增原生编译器（用于 `type-check` 的 Bun 服务端检查）：
   ```bash
   bun add -d @typescript/native@npm:typescript@^7.0.2
   ```
3. **前提**：升级 `vue-tsc` 到含垫片识别修复的版本（vuejs/language-tools PR #6123 —— `runTsc` 需能解析 `@typescript/typescript6` 垫片，找到真实的 `tsc.js`）。
   若届时 vue-tsc 已原生支持 TS7，则忽略双装，直接 `typescript@^7.0`。

## 预期要处理的新严格默认值

| 新默认                         | 本项目影响                                                                                  |
| ------------------------------ | ------------------------------------------------------------------------------------------- |
| `strictBuiltinIteratorReturn`  | 迭代器返回类型从 `IteratorResult<T, any>` 变 `IteratorResult<T, undefined>`，可能有个别报错 |
| `noUncheckedSideEffectImports` | 副作用导入需可解析；本项目 CSS 已由 `vite/client` 覆盖，预计无报错                          |
| `isolatedModules` 默认 true    | 本项目已用 `verbatimModuleSyntax: true`，等效覆盖，无影响                                   |
| 移除的 tsconfig 选项           | 扫描确认全链零命中                                                                          |

另外注意 tsgo 已知边界：`skipLibCheck` 不压制第三方 `.d.ts` 的解析级错误（TS1540）、
JSDoc `@typedef`+`@template` 声明生成会丢泛型参数（本项目非 `.js`，风险低）。

## 执行步骤（7.1 落地后）

1. 升级依赖（按上面双装或直装 TS7）
2. `bun run type-check` —— 修 `strictBuiltinIteratorReturn` 等新报错
3. `bun run build` —— 验证客户端、SSR 和 Bun standalone 编译
4. `bun run check` —— 全量 lint + format + type-check + test + build + SSR/OTel smoke
5. 编辑器冒烟：JetBrains 打开项目确认语言服务仍用 TS6 垫片（`typescript.tsdk` 指向 workspace 安装）

## 参考链接

- [microsoft/typescript-go（TS7 原生编译器）](https://github.com/microsoft/typescript-go)
- [vuejs/language-tools PR #6123 —— 双装垫片方案](https://github.com/vuejs/language-tools/pull/6123)
- [TypeScript 7.0 RC Migration Guide](https://www.sitepoint.com/typescript-70-rc-the-go-rewrite-migration-guide/)
- [TS7 迁移指南（dev.to）](https://dev.to/dev_encyclopedia/typescript-7-migration-guide-tsgo-breaking-changes-build-times-ihn)
