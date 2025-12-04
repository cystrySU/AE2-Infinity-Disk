================================
AE2 Infinity Disk Addon Specification
================================
Goals
-----
- 面向 AE2 1.19.2 稳定 API，提供单个无限容量、无限种类的存储磁盘。
- 与 AE2 存储网格完全兼容，遵守其能量、权限与过滤规则。
- 提供可配置的制作配方、能耗与服务器管理选项。

功能范围
--------
1. Infinite Capacity & Variety Disk
   - 单个虚拟池，支持无限数量与无限物品种类，持久化为 NBT/磁盘文件。
   - 允许服务器配置软上限（例如逻辑分片或配额）以避免溢出，同时提供审计日志。
   - 可选能量消耗倍率与访问权限策略。

系统组件
--------
- storage.InfiniteDiskStorage：负责数据模型、序列化、AE2 StorageChannel 接口实现。
- storage.InfiniteDiskManager：管理虚拟池、软上限与审计记录。
- networking.PacketSyncDiskState：在服务器与客户端间同步磁盘统计信息。
- client.gui.InfinityDiskScreen / menu.InfinityDiskMenu：磁盘管理界面与交互逻辑。
- config.InfinityDiskConfig：通用/服务器配置（能量倍率、配方开关、软上限等）。

开发阶段与交付
---------------
1. 规范与架构
   - 更新 README 与 `ExampleMod.java` 注释，锁定 AE2-only 策略与无限磁盘数据流。
2. 数据模型 & 注册
   - 在 `storage/` 包创建骨架类，注册磁盘物品、存储驱动与能力，确保 DeferredRegister 就绪。
3. 网络同步
   - 编写 PacketSyncDiskState、序列化测试，记录 Forge 事件触点与客户端缓存策略。
4. GUI/UX
   - 设计资源文件、控制器、AE2 终端集成点，并记录无障碍准则。
5. 平衡与 QA
   - 配方、能耗、配置、模拟世界测试，CI 集成 `gradlew test` 与静态检查。

AI 工具分工建议
----------------
- 文档与规范：编写/维护 README、mods.toml 描述。
- 代码生成：在指定包中编写骨架/实现，附带必要注释。
- 测试：编写单元/集成测试，验证存储池序列化与 AE2 交互。
- 资源：生成 UI 材质、语言文件与数据包。

检查点
------
- 每阶段结束进行 Code Review，使用 Checkstyle/Spotless 确保格式统一。
- 关键节点执行 `gradlew build` 与客户端沙盒测试。

开放决策
--------
- 软上限策略（全局配额 vs 多分片）及其告警方式。
- 是否需要额外的数据备份机制（如外部数据库）可在后续讨论。
