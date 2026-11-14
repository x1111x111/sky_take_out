# GitHub 发布准备

当前交付目录没有远程仓库，也没有把原上传包的 .git 历史带入。已连接 GitHub 仅发现另外两个项目，未向它们写入文件。本文所有 push 命令均为待执行说明，不表示发布完成。

建议仓库名：sky-take-out。建议描述：Spring Boot 外卖系统，含 Vue 双端、多级缓存与 Outbox 补偿实践。先使用私有仓库完成构建和启动验收，再根据展示需求决定公开。

## 已备妥内容

- 完整业务源码、前端、配置参考、13 表缓存还原 SQL 与 ACK 外键迁移、README 与交付文档。
- .gitignore 排除真实 .env、本地 application-local.yml、日志、uploads、target、dist 与 node_modules。
- CI 示例运行前端测试/构建和独立后端单元测试；当前未在 GitHub 执行。
- 历史 JTL、重算脚本与不夸大性能的测试说明。
- 未选择 LICENSE，需确认原始代码与素材授权后再决定；不要直接给未确认权属的全部内容追加开源许可。

## 本地建库并推送

先在 GitHub 创建空仓库，不自动添加 README/.gitignore/LICENSE。不要覆盖用户其他项目仓库。

在解压后的项目根目录执行：

```powershell
git init -b main
git add .
git diff --cached --stat
git diff --cached --check
git commit -m "chore: prepare sky take out project delivery"
git remote add origin <你新建仓库的真实HTTPS地址>
git push -u origin main
```

推送前完成：`python scripts/check_delivery.py`、后端构建、启动验收与 SQL 新库演练。检查待提交差异，不要把本地密码、JWT 密钥或当前业务数据加入提交。上传包中的旧 Git 历史可能记录过旧凭据；本交付不携带它，未对用户电脑原仓库做历史改写。

如果直接使用原来的 Git 仓库，必须自行检查历史及已跟踪配置；添加 .gitignore 不会移除已经被跟踪的文件。不要执行强制推送替换已有远程历史。

## 建议首个版本说明

标题：苍穹外卖工程化交付候选版。

内容：整理 Vue 双端与 Spring Boot 后端，文档化菜品多级缓存、Lua 版本校验、Outbox/实例 ACK 补偿和部署流程。前端 24 项测试与生产构建已通过；后端构建、空库运行、真实服务联调与综合压测尚未完成。该版本作为交付候选，不标注生产就绪或虚构性能提升比例。

发布结果应记录仓库 URL、分支、真实提交 SHA 及 CI 链接。当前均为空，不能填写示例为真实发布记录。
