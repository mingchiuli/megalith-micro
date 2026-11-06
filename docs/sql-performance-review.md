# SQL / JPA 审查记录

本轮扫描了全项目生产代码中的 SQL、JPA 仓库、持久化调用链、实体索引和事务边界。
关系库访问集中在 `micro-user`、`micro-blog`、`common-outbox`；其余服务使用 HTTP、
Elasticsearch 或 Redis。结论来自代码检查和隔离 MariaDB 集成测试，未对生产数据库做压测或变更。

## 已处理的问题

| 优先级 | 位置与问题 | 处理结果 |
| --- | --- | --- |
| 高 | 博客删除准备逐篇读取发布版本、敏感片段或草稿 | 改成批量读取，混合状态每批最多 4 次 SELECT；空输入不访问数据库 |
| 高 | 博客导出逐篇读取身份和全部版本 | 每个现有 20 篇分页最多 3 次 SELECT；先过滤身份权限，再读允许导出的版本，保持搜索顺序 |
| 高 | 博客草稿、版本及片段的派生删除先加载实体再逐条删除 | 改成 JPQL 批量 DELETE，保留博客版本校验和同事务 outbox；新版本直接插入片段 |
| 高 | 用户、角色、菜单和权限对已有 ID 使用 `save`，经 `merge` 隐式读取 | 新增仍使用 JPA 持久化；更新使用 JPQL，显式维护更新时间，目标消失时整单回滚 |
| 高 | 用户更新可能回写准备数据时的旧登录时间 | 资料更新不修改 `lastLogin`，保留原创建时间 |
| 高 | 博客恢复把已删除的自增 ID 当作 detached entity 合并 | 用 HQL INSERT 保留原 ID，再关联发布版本；重复恢复冲突并整体回滚 |
| 中 | 用户、角色分页只按创建时间排序 | 增加 ID 排序，复用现有索引 |
| 中 | 密码锁定使用原生 MariaDB UPDATE | 改为实体 HQL，仍由数据库计算截止时间，保留微秒精度及并发条件 |
| 中 | 草稿、版本的普通索引与唯一索引重复 | 实体只声明唯一索引；提供检查唯一索引后才删除普通索引的部署脚本 |
| 低 | 简单 ID 投影手写查询、版本映射重复扫描片段 | 改为派生 record 投影，补 AOT hints；按版本 ID 一次分组片段 |

发布内容与删除事件现在保留已经读取的敏感片段，防止删除后恢复时丢失片段数据。

## 保留的实现与限制

- 批量 DELETE、原子计数、乐观版本条件更新、复杂 outbox 查询保留 JPQL；恢复指定 ID 使用 HQL INSERT。
  本轮修改后生产持久化代码不再使用 `nativeQuery`，也未引入 JDBC 查询或 Criteria 框架。
- outbox 的轮询、聚合顺序和管理索引已有 MariaDB EXPLAIN 验证，保留原实现。
- 现有 ID 游标扫描与 `List + Pageable` 保留，不改成需要额外 COUNT 的 Page 查询。
- 自增主键保持不变；不宣称 `saveAll` 是单条 INSERT，也不通过单独开启 JDBC batch 参数承诺批量插入收益。
- 完整导出仍遵循现有格式：博客按 20 篇分批，但每篇导出完整版本历史；用户等 SQL 导出仍读取完整集合。
  本轮不改外部接口或导出格式。
- SQL 导出文本、测试造数和部署 DDL 是有明确用途的 SQL，继续保留。

## 部署索引清理

先部署新的实体映射，再对博客数据库执行 [索引清理脚本](sql/drop-redundant-blog-indexes.sql)。
脚本可重复执行；如果没有等价的唯一索引，就跳过删除。Hibernate `hbm2ddl update` 不会代替这一步。
DDL 独立提交；脚本只在隔离测试数据库执行过，未在现有业务数据库执行。

## 验证

```bash
./gradlew :micro-user:check :micro-blog:check :common-outbox:check \
  :micro-user:integrationTest :micro-blog:integrationTest
SPRING_JPA_PROPERTIES_HIBERNATE_HBM2DDL_AUTO=none \
SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.MariaDBDialect \
  ./gradlew :micro-user:compileAotJava :micro-blog:compileAotJava
```

MariaDB 集成测试共 18 项，覆盖实际 SELECT 数量、无 SELECT 的更新和删除、
缺失发布版本回退、版本冲突及 outbox 失败回滚、密码并发锁定与过期解锁、原 ID 恢复与重复恢复。
索引脚本测试覆盖重复执行及缺少唯一索引时跳过删除，并用每表 20,000 行数据验证唯一索引查询计划。
单测还覆盖导出搜索顺序和权限范围、record 投影的 AOT hints。ArchUnit、Spring 测试 AOT、
主应用 AOT 生成和编译均通过；本轮未构建 Native Image。
