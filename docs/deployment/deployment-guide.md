# 部署与启动

本指南基于当前配置与代码整理，尚未完成后端构建、MySQL 初始化和端到端演练。适用于本地项目展示。模拟用户登录不适合暴露给不可信用户；真实业务上线需要另行完成认证、依赖兼容性和运行验收。

## 环境

| 组件 | 要求或约定 |
|---|---|
| Java | POM 目标 17；JDK 17+，本次环境为 21 |
| Maven | 3.9.x，需可访问项目依赖仓库；上传包没有完整 mvnw 脚本 |
| MySQL | 8.x，InnoDB / utf8mb4；默认数据库 sky_take_out |
| Redis | 支持 String、SET NX EX/PX、EVAL、Pub/Sub 的本地单服务；Windows 可使用现有兼容服务，但需单独验收 |
| Node | 22.12+；本次 Node 24.22.1 完成 npm ci/test/build |
| Python | 3；生成初始管理员 SQL 时额外需要 bcrypt 包 |

不应跨 Windows/Linux 复制 node_modules；始终从当前 lockfile 执行 npm ci。

## 初始化新数据库

使用本地 MySQL 客户端连接，导入 `sql/sky_take_out.sql` 并执行 `sql/migrations/001_ack_event_cascade.sql`。SQL 会选择 sky_take_out；若要隔离到另一个库，先调整 CREATE DATABASE、USE 与 DB_URL，避免连到已有业务库。

MySQL 客户端中：

```sql
SOURCE F:/sky_take_out/sql/sky_take_out.sql;
SOURCE F:/sky_take_out/sql/migrations/001_ack_event_cascade.sql;
```

基础脚本依据 IDEA 缓存还原，不含外键；第二个脚本为当前清理机制补充 ACK 级联约束，需在新库执行一次。仅用于新空库。旧库先检查已有约束和孤儿 ACK，再按 [数据库文档](../database/database-design.md) 先对比结构。

为应用创建仅访问该库的本地账号并授予该库所需 DML 权限；实际用户名配置为 DB_USERNAME。建表操作可用独立管理员执行。数据库真实密码自行设置。

生成初始管理员 INSERT：

```powershell
python -m pip install bcrypt
python .\scripts\admin_password.py
```

输入至少 12 字符的自定密码，复制输出 SQL 到 MySQL 的 sky_take_out 库执行。脚本不会连接数据库，也不会保存明文密码。不要重复初始化同名 admin。首次没有分类、菜品和套餐，需通过管理端录入。现有员工创建接口仍使用教学默认密码，当前没有改密接口；正式部署需另行修复。

## Windows / IDEA

假设整理后的目录位于 `F:\sky_take_out`。原目录请先备份，解压交付包到单独目录对比，不要直接覆盖当前数据库、图片或本地配置。

在 PowerShell 设置环境变量：

```powershell
$env:DB_USERNAME = 'sky'
$secureDb = Read-Host 'MySQL password' -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $secureDb).Password
$env:JWT_SECRET = '请替换为随机生成且至少32字节的密钥'
$env:REDIS_HOST = '127.0.0.1'
$env:REDIS_PORT = '6379'
$env:CACHE_INSTANCE_ID = 'server-A'
$env:CACHE_EXPECTED_INSTANCE_IDS = 'server-A'
```

可用 Python 生成随机 JWT 密钥：`python -c "import secrets; print(secrets.token_urlsafe(48))"`。生成后只放入本地环境，不要写回 Git 跟踪文件。所有实例必须使用相同 JWT 密钥。

在 IDEA 打开 sky-server/pom.xml，选择 JDK 17 或 21，加载 Maven。Run Configuration 的 Environment variables 填入同样变量，再运行 SkyServerApplication。通过 IDEA 启动时，PowerShell 的变量不会自动传给已运行的 IDEA；必须在运行配置中设置。

命令行构建和启动：

```powershell
cd F:\sky_take_out\sky-server
mvn.cmd -B "-Dtest=PasswordTest,DishServiceImplCacheTest,EmployeeSerializationTest" clean package
java -jar .\target\sky-server-0.0.1-SNAPSHOT.jar
```

此构建只运行列出的无外部服务单元测试。`mvn verify` 还会运行现有的 SpringBootTest / RedisConnectionTest，需有效环境变量、MySQL 和 Redis。测试里会写入 `sky:test:connection` 并设置 5 分钟 TTL，应使用隔离测试服务。

另开终端启动前端：

```powershell
cd F:\sky_take_out\sky-admin
npm.cmd ci
npm.cmd test
npm.cmd run dev
```

后端默认 8080；前端 5173。管理员入口 `http://localhost:5173/#/admin/dashboard`，用户入口 `http://localhost:5173/#/user/menu`。用户登录输入固定本地 code，同一 code 会代表同一账号。

需要本地完整订单演示时，在后端设置 `MOCK_PAYMENT_ENABLED=true` 并重启。支付与退款不是真实资金操作。

## Linux

使用相同变量名，通过服务配置或终端 export 设置。先设置所有必需值，再在 sky-server 运行：

```bash
mvn -B -Dtest=PasswordTest,DishServiceImplCacheTest,EmployeeSerializationTest clean package
java -jar target/sky-server-0.0.1-SNAPSHOT.jar
```

在 sky-admin 运行 `npm ci && npm test && npm run build`。Nginx 示例位于 sky-admin/deploy/nginx.conf：`/api/` 转发时去掉前缀，`/uploads/` 保留；按实际机器调整根目录、端口、域名及 TLS。当前示例仅本地 HTTP。

应用的工作目录必须固定且可写，图片保存在其中的 uploads。备份数据库与 uploads，不能仅备份 JAR。交付包不包含用户原来的上传图片。

## 双实例配置

| 项目 | A | B |
|---|---|---|
| SERVER_PORT | 8080 | 8081 |
| CACHE_INSTANCE_ID | server-A | server-B |
| CACHE_EXPECTED_INSTANCE_IDS | server-A,server-B | server-A,server-B |
| MySQL / Redis / JWT | 共享相同配置 | 共享相同配置 |

ID 不能重复；临时离线后恢复需保留稳定 ID。不能为清理积压而删除临时离线实例。该部署配置已写入指南，但双实例运行和故障测试未执行。图片目录需共享存储或固定路由，当前方案未解决。

## 启动后逐项检查

1. 控制台确认 Spring Boot 启动，检查 Outbox/ACK 相关 SQL 与订阅是否报错。
2. 管理员登录，新增分类、菜品；用户模拟登录后查询分类和菜单。
3. 店铺默认打烊；管理员先切换为营业，再演练加购、地址、下单、模拟支付、接单、派送、完成。
4. 检查 NOT_LOGIN/TOKEN_INVALID/NO_PERMISSION 及 code=0 错误是否被客户端正确处理。
5. 检查事件与 ACK 数据库结构，确认是否有缺表、缺列或外键差异。

上述仍是待执行清单，完成后才能更新交付状态。

## 已知启动阻碍

- 本次无法验证 Spring Boot 4.0.8 与 springdoc 2.8.8 / Knife4j 4.5.0 的组合；保留完整启动堆栈以定位。
- 缺 DB_PASSWORD/JWT_SECRET 会导致配置无法解析；这是避免默认凭据运行的预期行为。
- JWT 密钥过短会在签发 Token 时失败。
- Redis 订阅容器可能影响冷启动时的连接行为，故障降级不能代替冷启动验收。
- Spring Boot 不自动加载 .env；模板文件不等于环境变量已设置。
