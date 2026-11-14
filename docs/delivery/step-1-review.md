# 第一步：代码整理与配置检查

日期：2026-11-14。来源：本次上传的 sky_take_out.zip；未以旧对话中的实现说明代替源码核验。

## 已完成的修改

| 项目 | 实际修改 |
|---|---|
| 交付目录 | 保留 sky-server、sky-admin、SQL 和历史 JTL；排除 IDE、target、node_modules、旧 dist、上传图片、JMeter 安装程序、其他无关项目及原 .git 历史 |
| 依赖 | 移除 jjwt:0.9.1；保留 jjwt-api/impl/jackson:0.12.6。当前 POM 的 Validation 只有一处，没有重复删除 |
| 配置 | MySQL 密码、JWT 密钥无默认明文值；Redis、端口与实例列表可通过环境变量配置；增加配置参考模板 |
| 单实例默认值 | 预期实例默认为 server-A；部署双实例时必须显式配置 server-A,server-B |
| 调试代码 | 删除未鉴权 /test/employee、空的 service.CacheInvalidationPublisher、整类注释的旧异常处理器及未使用前端模板 |
| 信息输出 | Employee 的 password 标记 JsonIgnore，避免 /admin/employee/list 序列化密码哈希；登录校验仍可访问实体密码 |
| 源码清理 | 去重 import；移除已确认不用的 import 和历史缓存实验注释；保留 cache 包中的正式 Publisher |
| 测试更新 | 替换过期的无参构造及旧字段注入，按 Lua 版本校验与随机 TTL 更新测试；补充旧版本写入冲突、本地失效中途回填和密码序列化检查 |

## 检查结果与限制

- Outbox 事件插入位于带事务的菜品业务调用中；提交后触发即时失效和 3 秒延迟失效。
- Poller 先执行 Redis 版本递增/删除，再清理本地并写 ACK；ACK 使用联合键与重复键无操作更新。
- 清理任务按照预期实例集合统计 ACK；级联删除需要实际数据库外键配合；补充的 IDEA 缓存确认了 ACK 联合主键，但未记录外键，已附单独迁移脚本。
- Redis 故障下本地命中仍会直接返回；Poller 的本地失效在 Redis 成功之后。当前实现**不能保证故障期间跨实例下一次请求即读到最新数据**。本次未改变核心缓存流程，作为已知限制记录。
- springdoc 2.8.8 / Knife4j 4.5.0 与 Spring Boot 4.0.8 的运行期兼容性尚未验证；交付整理不擅自升级版本。
- 本环境 Java 21 可用，但没有 Maven，Maven Central 下载连接超时，无法执行 Maven 编译或后端单元测试。
- 上传的前端 node_modules 来自 Windows，无法直接复用；已按原 lockfile 在当前环境重新安装，24 项测试通过，生产构建通过。

本步骤状态：源码整理已完成；构建及运行验收待补。源码检查不等于测试通过。
