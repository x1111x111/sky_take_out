# 数据库附件核对

收到的附件为 `sky_ds.zip`。它包含 IDEA 数据源配置、历史连接信息和结构缓存，不是 `.sql` 数据库备份，没有业务行数据。交付只提取 sky_take_out 的表结构，不复制连接账户、其他库名称或整个 .idea 目录。

缓存 XML 在压缩包中的时间标记为 2026-11-11 16:44，时区未指定；不证明当前数据库在 11 月 14 日仍完全相同。

## 已读取的证据

| 项目 | 结果 |
|---|---|
| 表 | 13 张，均属于 sky_take_out |
| 字段 | 117 个 |
| 索引 | 17 个（含 13 个主键） |
| 员工用户名 | 存在 username 唯一索引 |
| 用户 openid | 缓存未记录唯一索引，字段允许 NULL，VARCHAR(45) |
| ACK | 联合主键 event_id + instance_id；processed_at 默认 CURRENT_TIMESTAMP(3) |
| ACK 额外索引 | idx_instance_processed(instance_id, processed_at) |
| 事件额外索引 | idx_category_id_id(category_id,id)、idx_created_at(created_at) |
| 外键 | 缓存没有记录，不能确认当前库有 ON DELETE CASCADE |
| 业务数据 | 没有提供，不推断账号、密码、分类或商品数据 |

## 对交付的修订

原先仅从 Java 实体推导的建表脚本已被缓存对应的字段长度、NULL、默认值和索引定义替换；没有把推测性查询索引写成当前已有索引。原缓存中的 orders.cancel_Reason 大小写保留，Java/Mapper 使用 cancel_reason，MySQL 列名不区分大小写；后续迁移可按实际 SHOW CREATE TABLE 决定是否统一。

当前清理方法只 DELETE 事件表。若真实库缺少级联外键，就会留下 ACK 孤儿。为新库另附 `sql/migrations/001_ack_event_cascade.sql`，明确区分“缓存观察到的结构”和“当前代码需要的迁移”。未在用户数据库执行该迁移；存量库应先检查现有外键和孤儿记录。

```sql
SHOW CREATE TABLE cache_invalidation_event;
SHOW CREATE TABLE cache_invalidation_ack;
SELECT COUNT(*) AS orphan_ack_count
FROM cache_invalidation_ack a
LEFT JOIN cache_invalidation_event e ON e.id=a.event_id
WHERE e.id IS NULL;
```

缺外键且存在孤儿时，ALTER 会失败，应先审阅数据与备份再处理，不能自动删除。

## 后续所需的权威结构

当前缓存足以完善候选文档；实际运行验收仍应使用当前数据库导出的仅结构 SQL，或各表 SHOW CREATE TABLE。Windows 可执行：

```powershell
mysqldump -u root -p --no-data --databases sky_take_out --result-file=F:\sky_take_out_schema.sql
```

用户名替换为有权限的本地账号；-p 会提示输入密码，不把密码写入命令。这里只导出结构，未要求导出真实业务数据。当前没有访问或扫描 F 盘，也没有连接到用户数据库。
