# 苍穹外卖：业务系统与多级缓存实践

面向 Java 后端学习与实习项目展示的外卖系统。包含 Vue 管理端和用户端，以及 Spring Boot 后端；围绕菜品分类查询实现 Caffeine、Redis、MySQL 多级读取，并通过事务内失效事件和实例 ACK 补偿缓存通知。

本交付基于 2026-11-14 上传的源码整理。前端 24 项测试和生产构建通过；后端 Maven 构建、空库初始化与真实服务联调尚未验收。多实例综合压力测试按项目决定未执行，不能据此宣称强一致、高可用或某个并发容量。

## 功能

| 角色 | 功能 |
|---|---|
| 商家 | 员工、分类、菜品口味、套餐组成、图片上传、营业状态、订单查询与接单/拒单/配送/完成、工作台和报表 |
| 用户 | 模拟登录、菜单与口味选择、购物车、地址新增与列表、下单、订单历史与详情、取消和再来一单 |
| 本地演示 | 模拟支付，默认关闭；不包含真实微信身份认证、真实支付或退款执行 |

地址编辑/删除、真实支付和退款均不在当前接口范围内。角色仅区分 ADMIN 和 USER，未实现细粒度 RBAC。新增员工的默认密码仍为教学代码中的 123456，后续正式使用前需补充安全开户与改密能力。

## 技术栈

版本来自当前 POM 和 package-lock.json，不代表推荐升级版本。

| 部分 | 当前实现 |
|---|---|
| 后端 | Java 17 目标版本、Spring Boot 4.0.8、MyBatis 4.0.1 |
| 数据与缓存 | MySQL、Spring Data Redis、Caffeine |
| 容错与鉴权 | Resilience4j 2.4.0、JJWT 0.12.6、BCrypt |
| 接口文档 | springdoc 2.8.8、Knife4j 4.5.0；与 Boot 4 的运行兼容性待验证 |
| 前端 | Vue 3.5.42、Vite 8.3.0、Axios 1.20.0（客户端实际请求使用 fetch） |
| 本次工具环境 | JDK 21.0.13、Node 24.22.1、Python 3 |

前端 UI 为项目自有组件与 CSS，未使用 Element Plus。完整架构说明见 [系统架构](docs/architecture/system-architecture.md)。

## 缓存实现

- 读取：本地 Caffeine → Redis → MySQL；同分类本地锁与 Redis 租约锁抑制并发重建。
- 正常 Redis TTL 为 480～720 秒；空结果 45～75 秒；本地 TTL 30 秒、最多 200 项。
- Redis Lua 原子校验版本再写缓存；本地 generation 与同步保护拒绝失效前的旧回填。
- 菜品写操作在同一数据库事务记录 Outbox；提交后执行失效、Pub/Sub 通知及 3 秒后的再次失效。
- 每个实例每轮处理最多 100 条未 ACK 事件；Redis 与本地失效完成后才写 ACK。轮询默认间隔 2 秒。
- 超过 7 天且全部预期实例 ACK 的事件才可清理；ACK 联合主键已在结构缓存中确认，级联删除需应用所附外键迁移或核实当前库已有约束。
- Redis 失败时有熔断与单实例最多 5 个并发的 MySQL 降级查询。

这些机制提供异步失效与补偿思路。消息传递有时间窗口；Redis 故障时已有本地缓存仍可能命中，未实现全实例本地缓存绕过。不得宣称“修改后其他实例下一次请求必定最新”。详见 [已知问题](docs/delivery/known-issues.md)。

## 目录

| 路径 | 内容 |
|---|---|
| sky-server | Spring Boot 源码、配置参考、单元与环境依赖测试 |
| sky-admin | Vue 管理端和用户端、前端测试、Nginx 示例 |
| sql/sky_take_out.sql | 从补充的 IDEA 结构缓存还原的 13 表空库脚本；另附 ACK 外键迁移，未在 MySQL 执行验收 |
| scripts | 初始管理员 SQL 生成器、历史 JTL 统计工具、静态交付检查 |
| docs | 架构、数据库、接口、部署、测试和逐步交付记录 |

交付包不包含原始 .git 历史、业务数据、上传图片、依赖目录与构建产物。未附加开源许可证；公开分发前应确认原始教学代码与素材的许可。

## 本地启动

详见 [Windows / Linux 启动说明](docs/deployment/deployment-guide.md)。

1. 准备 JDK 17+（本次环境为 21）、Maven 3.9.x、MySQL 8、Redis/本地兼容服务、Node 22.12+。
2. 仅在新空库导入 `sql/sky_take_out.sql`，再执行 `sql/migrations/001_ack_event_cascade.sql`；现有数据库先核对当前结构和外键。
3. 使用 `scripts/admin_password.py` 生成管理员初始化 SQL；初始密码自行设置。
4. 设置 `DB_USERNAME`、`DB_PASSWORD`、`JWT_SECRET`，在 `sky-server` 执行 `mvn spring-boot:run`。
5. 在 `sky-admin` 执行 `npm ci`、`npm run dev`。
6. 管理端：`http://localhost:5173/#/admin/dashboard`；用户端：`http://localhost:5173/#/user/menu`。

后端默认 8080。`.env.example` 仅为参考，Spring Boot 不会自动加载该文件。双实例必须使用不同 `CACHE_INSTANCE_ID`、端口，并在全部实例配置相同 `CACHE_EXPECTED_INSTANCE_IDS=server-A,server-B`。

## 验证结果

| 项目 | 状态 |
|---|---|
| 前端 npm ci | 已完成，保留原 lockfile |
| 前端 npm test | 24/24 通过 |
| 前端 npm run build | 通过，19 个模块 |
| 后端编译、后端测试、打包 | 未执行成功：当前环境没有 Maven，下载超时 |
| MySQL 建表与端到端业务 | 未执行；已读取 IDEA 结构缓存，无运行中的 MySQL/Redis 测试环境 |
| 多实例、宕机恢复、综合压测 | 未执行 |
| 历史 round2/round3 JTL | 已保留并重新统计；不能充当当前版本测试证明 |

[测试报告](docs/testing/performance-report.md)区分本次验证、历史样本与未测试内容；不会由 HTTP 200 推断业务断言成功。

## 文档导航

- [第一步：代码与配置整理](docs/delivery/step-1-review.md)
- [系统架构与一致性边界](docs/architecture/system-architecture.md)
- [数据库设计与 ER 图](docs/database/database-design.md)
- [数据库附件核对](docs/database/source-review.md)
- [接口清单与示例](docs/api/api-documentation.md)
- [部署与启动](docs/deployment/deployment-guide.md)
- [测试与历史压测数据](docs/testing/performance-report.md)
- [发布准备](docs/deployment/github-release.md)
- [交付状态与后续事项](docs/delivery/delivery-status.md)
