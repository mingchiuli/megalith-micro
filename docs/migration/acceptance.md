# Base、auth、exhibit 迁移验收

2026-11-29，候选版本在四种浏览器配置下完成 152 项应用回归，全部通过，零跳过、零重试。旧版运行同一清单，141 项通过，11 项失败或超时。外部服务验收仍有待办。

对照版本为 `c1d9bb93ed5e0a24c682ba30683a002970a0af93`。候选版本运行一个 GraalVM Native Image、两个 Rust release 服务和 Bun 独立前端。两版使用单独的 MariaDB 数据库、Redis、RabbitMQ、Meilisearch 和 HTTPS nginx。测试账号和文章均在隔离环境中创建。

## 迁移范围

| 旧模块 | 候选实现 | 核对内容 |
| --- | --- | --- |
| `micro-user`、`micro-blog` | `micro-base` | 保留两套 Java 包、路由、数据表、事务和 outbox 生产者标识。base 注册的出站 HTTP 客户端为 auth、search 和 OSS。 |
| `api-user`、`api-blog`、`api-auth` | `api-base`、`crates/api`、base 的 auth 出站客户端 | Java 接口和记录、Rust 服务间请求与响应、内部路径和可信身份头。 |
| `micro-auth` | `micro-auth-rs` | 显式接管原 Spring Security 过滤器提供的 `POST /login`。网关每个请求仍做一次路由鉴权；验证了旧版与新版双向识别会话并刷新访问 Cookie。 |
| `micro-exhibit` | `micro-exhibit-rs` | 匿名与作者视图、UTF-16 脱敏区间、一次性阅读码、阅读计数、推荐、文章变更后的缓存失效。 |
| Java `cache` | 项目内 `crates/cache-rs` | auth 的四个方法与 exhibit 的一个方法使用 `#[cache(CONTRACT)]` 属性宏。宏按方法参数的声明顺序生成有类型的缓存键。已用 Java 的 `@Cache` 代理和 Rust 属性宏实测 L2 互读、键追踪、失效及回填。 |

原始清单包含 660 个文件、2,698 个已声明方法。另有 576 项记录将原 auth、exhibit、cache 生产方法和构造函数定位到 Rust 符号、框架接线或已知差异。清单用于代码审查，行为证据来自下面的测试。[文件清单](baseline-c1d9bb93.tsv) · [Rust 方法对照](rust-methods.tsv)

## 构建与集成检查

| 检查对象 | 已验证内容 |
| --- | --- |
| GraalVM base | Gradle 构建、ArchUnit、AOT、Native Image 编译和原生进程启动；带父子关系的菜单读取、协作、outbox。 |
| MariaDB | 并发角色改名与缓存驱逐、菜单父子引用与角色权限外键、同一快照统计待投递与暂停事件、提交成功后才更新锁定指标。隔离副本执行了正向、回滚和再次正向迁移。 |
| Rust workspace | rustfmt、Clippy（警告视为错误）、工作区测试。分别使用真实 Redis、RabbitMQ、Meilisearch 验证分布式缓存、消费确认及关停、搜索投影和协作存储。 |
| 前端 | 类型、格式、lint、Vitest、独立 Bun 可执行文件的 SSR、资源、压缩、遥测和关停检查。 |
| Linux arm64 镜像 | 启动两个发布镜像，在运行容器内执行健康探针。经映射端口访问 auth 登录并检验两个 Set-Cookie 响应头，访问 exhibit 公开统计。HttpOnly 属性由浏览器用例检查。 |

base 的 219 项单元测试、32 项集成测试，Java cache 的 6 项集成测试，以及前端 34 个文件中的 160 项 Vitest 全部通过。Rust 工作区常规测试通过 73 项，另外 7 个需显式启用的中间件用例已分别在相应环境运行。缓存基准测试和 Rust 消费者关停测试也在专用 Redis、RabbitMQ 上完成。

| 浏览器运行使用的产物 | SHA-256 |
| --- | --- |
| macOS base Native Image | `3129faf156db6e02450d217bb963834cf8223f1df858ef1252f0acf29bbc74ff` |
| macOS auth release | `6a3ac270b243c0d3e9953bc0d44f75833534599e85914cdfd1bab2c349c1a499` |
| macOS exhibit release | `0e11698a0486e320f8ccad53bfeb3ec88efd114c139cc56a8a9a6ba5d10fa302` |
| Bun 独立前端 | `14dd0913a578ce398209df57e14324179d61833f1e79363ee1c753d76cb83b84` |

单独启动验证的 Linux arm64 镜像 ID 为 auth `sha256:967be39ecbf76960043cbe2c1260a032e0f39f3e0a287d461f91d1dc1f070960`、exhibit `sha256:30887f9cd7ab932cc04207d954a35b536a7cdfb8fea5df5fbc0c437dc44cc3b2`。

## 浏览器环境

Playwright 1.64.1 驱动 Chromium 156、Firefox 157 和 WebKit 27.2。Firefox 使用 Linux 浏览器容器，桌面 WebKit 和 iPhone 13 窄屏配置使用 macOS WebKit，窄屏配置模拟设备。请求经过 nginx 和真实网关，SSR 使用服务端入口，协作使用 WebSocket。候选环境的 auth、exhibit、sync 各运行两个副本。

| 浏览器配置 | 每版场景数 | 候选通过 | 旧版通过 | 旧版失败或超时 |
| --- | ---: | ---: | ---: | ---: |
| Chromium | 38 | 38 | 36 | 2 |
| Firefox | 38 | 38 | 34 | 4 |
| 桌面 WebKit | 38 | 38 | 36 | 2 |
| 窄屏 WebKit | 38 | 38 | 35 | 3 |

结果按浏览器和用例组分批采集。在开发中修复失败项后重新执行了相关用例组。最终 CSV 按浏览器、文件、场景核对了完整清单，每项各有一次执行记录，Playwright 自动重试设置为 0。CSV 仅记录用例、状态、次数和时长。

桌面 WebKit 在两轮运行中各出现一次超时。一次后台文章搜索超过 15 秒，另一次登录请求在约 10 秒后被客户端中止，入口 nginx 记录 HTTP 499。两次发生在不同请求，原因尚未确认。本地构建目录 `micro-frontend/build/migration/cache-attribute/` 中的 `webkit-before-recheck.json` 和 `webkit-login-timeout.json` 分别保留两次记录。后续运行启用了隔离 nginx 的上游计时，沿用相同超时。

旧版各项失败或超时可按浏览器和用例名称在对照 CSV 中定位。候选修复了注册表单引用冲突和弹层遮挡确认控件的问题。

用例覆盖登录与登出、现有会话跨版本刷新、邀请注册、后台菜单与授权变更、用户角色菜单权限管理、筛选和 SQL 导出、双编辑器协作、文章发布与上下线、版本比较恢复、匿名阅读、一次性阅读码，以及失败后的页面重试。阅读码由作者在 UI 签发，访客在浏览器中向真实网关提交。现有公开搜索只返回公开文章，推荐卡片也都标记为公开，因而这两个入口均无法打开受保护文章的阅读码弹窗。逐项操作和证据类型可按控件编号查阅。[候选逐项结果](browser-evidence.csv) · [旧版逐项结果](baseline-browser-evidence.csv) · [控件清单](ui-controls.tsv)

控件清单有 221 项。173 项有直接操作断言，22 项由工作流断言覆盖，8 项有部分分支证据，4 项仅验证到接口，10 项等待真实服务，3 项真实服务验证到部分步骤，1 项记录旧版问题。控件测试还检查了旧 IndexedDB 草稿的三项只读内容、取协作票据失败后的重连、正文中重复词的脱敏位置、11 个历史版本翻页、图片加载成功与失败、自动补全选择、推荐游标翻页、筛选清空、直接跳页、自定义服务端口和打印预览。Chromium 另外生成了 PDF。

## 尚待验收

- 邮件和短信验证码成功登录需要可接收验证码的专用账号和收件程序。OSS 上传、读取、删除需要专用测试桶；Giscus 和 Disqus 需要测试站点、文章和账号，才能验证真实评论及清理。[真实服务用例及配置](cutover.md#live-integration-fixtures)
- 本机 Ollama 已生成文章标题、摘要和封面提示词。图像生成端点对已配置的图像模型返回 HTTP 400，说明当前运行时不支持图像生成。封面生成、预览和后续上传仍待在支持该模型的端点验证。
- 旧后台的通用注册链接按钮向服务端传空用户名，原注册契约要求邀请绑定用户名，并在消费时校验。绑定用户名的邀请已通过浏览器正反例，空用户名入口需要产品决定。
- 复制的导航库中有一条旧式 `/public/blog/recommendations/page/*` 路由。测试副本已登记实际使用的 `/public/blog/recommendations`；发布前需在目标环境核对这条路由及授权。
- 控件清单标明了只验证到工作流、接口或单个分支的项目。系统打印对话框、第三方服务和已知旧版问题不能由应用回归通过数推断。

切换步骤、回滚脚本、数据库前置检查和重新生成证据的命令见[切换手册](cutover.md)。本次验证只写入隔离测试环境。
