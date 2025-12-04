# AE Infinity Disk 项目总览（开发者向）

## 1. 项目简介与特性

**AE Infinity Disk** 是一个面向 Minecraft 1.19.2、基于 Forge 的 Applied Energistics 2（AE2）附属模组，为 AE2 提供一枚「无限容量 / 无限种类」的 ME 存储磁盘（`infinity_disk`）。

主要特性：
- 向 AE2 注册新的存储 Cell 类型，实现逻辑上的「无限容量、无限种类」物品存储。
- 通过统一的管理器与存储池（`InfiniteDiskManager` / `InfiniteDiskStorage`）集中管理所有无限磁盘的数据。
- 提供基础的客户端 GUI 与菜单（`InfinityDiskMenu` / `InfinityDiskScreen`），用于查看或管理磁盘状态（具体功能依当前实现）。
- 使用 Forge/AE2 官方 API：
  - Forge 运行环境与事件总线（注册物品、菜单、命令、网络等）。
  - AE2 的 `StorageCells` API 注册自定义 Cell 处理器（`InfiniteDiskCellHandler`）。
- 可通过配置（`Config`）对部分行为进行服务器端调整（如能量消耗倍率、软上限等——以代码注释为准）。

本仓库主要面向有一定 Forge & AE2 开发经验的模组作者，用于参考或二次开发。

---

## 2. 运行环境与依赖版本

### Minecraft / Forge
- **Minecraft 版本**：`1.19.2`
- **Forge 版本**：`43.5.0`
- **ForgeGradle 插件**：`net.minecraftforge.gradle`，版本范围 `[6.0, 6.2)`
- **Java 版本**：使用 Gradle Toolchain，目标 `Java 17`

相关配置来自：
- `gradle.properties`
  - `minecraft_version=1.19.2`
  - `forge_version=43.5.0`
  - `minecraft_version_range=[1.19.2,1.20)`
  - `forge_version_range=[43,)`
  - `loader_version_range=[43,)`
- `build.gradle`
  - `java.toolchain.languageVersion = JavaLanguageVersion.of(17)`
  - `minecraft "net.minecraftforge:forge:${minecraft_version}-${forge_version}"`

### AE2 依赖
- 运行时与 API 通过 ModMaven 获取：
  - `compileOnly fg.deobf("appeng:appliedenergistics2-forge:12.9.5:api")`
  - `runtimeOnly fg.deobf("appeng:appliedenergistics2-forge:12.9.5")`
- 需要在运行时安装对应版本的 AE2 Forge 模组。

### 其他
- 构建系统：Gradle（使用项目提供的 `gradlew` / `gradlew.bat`）
- IDE 推荐：IntelliJ IDEA（或 Eclipse），支持 Gradle 项目导入与 Java 17。

---

## 3. 源码与资源结构说明

### 3.1 Java 源码结构（`src/main/java`）

根包：`com.CystrySu.infinitydisk`

主要子包与类：

- `Infinitydisk`
  - 模组主类，`@Mod(Infinitydisk.MODID)`。
  - 负责：
    - 注册物品 `INFINITY_DISK`（`DeferredRegister<Item>`）。
    - 注册菜单类型 `InfinityDiskMenu.MENUS`。
    - 在 `commonSetup` 中注册 AE2 Cell 处理器和网络通道。
    - 注册 Forge 事件（如命令、服务器启动等）。
    - 注册配置 `Config.COMMON_SPEC`。

- `Config`
  - 定义服务器/公共配置（`ModConfig.Type.COMMON`）。
  - 通常包含：能量倍率、是否启用某些配方或功能、软上限等（具体字段见源码）。

- `item`
  - `ItemInfiniteDisk`
    - 无限存储磁盘物品实现，绑定到注册名 `infinity_disk`。
    - 负责物品的基本行为、工具提示等（具体逻辑见类实现）。

- `storage`
  - `InfiniteDiskCellHandler`
    - 实现 AE2 的 `ICellHandler` 相关接口。
    - 在 `Infinitydisk.commonSetup()` 中通过 `StorageCells.addCellHandler(InfiniteDiskCellHandler.INSTANCE)` 向 AE2 注册。
    - 决定什么时候某个物品（`ItemInfiniteDisk`）被识别为有效的 AE2 存储 Cell，并返回对应的存储实现。
  - `InfiniteDiskCellInventory`
    - 实现 AE2 的 `StorageCell`/`IMEInventory` 相关接口。
    - 表示某一枚无限磁盘在 AE2 存储网络中的视图（Inventory 层），和物品堆栈、NBT 等绑定。
  - `InfiniteDiskStorage`
    - 核心的后端存储逻辑，通常管理真正的物品池（如 Map 或其它结构）。
    - 负责增删查物品、计算统计信息、序列化/反序列化等。
  - `InfiniteDiskManager`
    - 全局管理器，维护所有无限磁盘 ID 对应的存储池。
    - 可能负责：
      - 磁盘数据的生命周期管理（加载/卸载）。
      - 软上限、审计日志或性能控制。
      - 与世界保存机制（Level / Capability / SavedData）集成。

- `menu`
  - `InfinityDiskMenu`
    - Forge 菜单容器实现，配合 GUI 屏幕使用。
    - 内部含有一个 `DeferredRegister<MenuType<?>> MENUS` 用于注册容器类型。
    - 负责同步服务器端无限磁盘数据给客户端 GUI。

- `client`
  - `ClientSetup`
    - 客户端初始化逻辑（在 MOD 事件总线上，`Dist.CLIENT`）。
    - 负责注册 Screen、渲染层等。
  - `client.gui`
    - `InfinityDiskScreen`
      - 对应 `InfinityDiskMenu` 的客户端界面实现。
      - 负责绘制界面、处理用户交互，并通过网络包与服务器同步操作。

- `networking`
  - `NetworkHandler`
    - 注册 Forge 网络通道与数据包。
    - 在 `Infinitydisk.commonSetup()` 中调用 `NetworkHandler.register()` 完成通道与包的注册。
  - `PacketSyncDiskState`
    - 用于在服务端与客户端之间同步无限磁盘的统计信息/状态（如容量使用、物品种类数等）。
  - `ClientDiskStateCache`
    - 客户端缓存，用于存储从服务器同步过来的磁盘状态，避免频繁请求。

- `command`
  - `InfinityDiskCommands`
    - 在 `RegisterCommandsEvent` 中注册调试/管理命令（例如：重置磁盘、查看统计信息等）。

### 3.2 资源结构（`src/main/resources`）

- `META-INF/mods.toml`
  - Forge 模组元数据定义：
    - `modId`、名称、版本、作者、描述等信息（其中有部分由 Gradle 的 `processResources` 任务注入）。
  - 保证与 `gradle.properties` 中的 `mod_id`、`mod_name` 等一致。

- `assets/ae_infinity_disk/`
  - `lang/`
    - 多语言本地化文件（`*.json`），支持：
      - `en_us.json`、`zh_cn.json`、`zh_tw.json`、`de_de.json` 等。
    - 包含物品名称（如 `item.ae_infinity_disk.infinity_disk`）、GUI 文本、提示等。
  - `models/item/infinity_disk.json`
    - 物品模型定义，指向对应的纹理资源，用于渲染无限磁盘物品。
  - `textures/item/infinity_disk.png`
    - 无限磁盘物品图标纹理。
  - `textures/gui/`
    - GUI 相关纹理资源（例如无限磁盘管理界面背景）。

- `data/ae_infinity_disk/`
  - `recipes/infinity_disk.json`
    - 数据驱动的合成配方文件，定义如何在游戏中合成无限磁盘。

- `pack.mcmeta`
  - 资源包元数据，同样由 Gradle 的 `processResources` 任务注入版本信息等。

---

## 4. 关键类与模块职责概览

> 下列说明基于当前代码结构和常见 AE2 Addon 约定，具体细节以源码为准。

### 4.1 核心入口与配置

- `Infinitydisk`
  - 模组主入口类：
    - 定义 `MODID = "ae_infinity_disk"`，与 `mods.toml` / `gradle.properties` 保持一致。
    - 在构造函数中：
      - 注册物品、菜单类型。
      - 注册通用事件监听（命令注册、服务器启动等）。
      - 注册通用配置 `Config.COMMON_SPEC`。
    - 在 `commonSetup` 中：
      - `NetworkHandler.register()` —— 注册所有网络包。
      - `StorageCells.addCellHandler(InfiniteDiskCellHandler.INSTANCE)` —— 向 AE2 注册自定义存储 Cell 处理器。

- `Config`
  - 提供 `COMMON_SPEC` 用于 Forge 配置系统。
  - 集中管理与无限磁盘行为相关的服务器变量（如能量倍率、配方开关、软上限等）。

### 4.2 无限存储实现

- `ItemInfiniteDisk`
  - 表示可放入 AE2 驱动器/界面的无限存储磁盘物品。
  - 可能包含：
    - 工具提示（显示容量、使用率等）。
    - `stacksTo(1)` 限制：单格最多一枚。

- `InfiniteDiskCellHandler`
  - AE2 Cell 处理器：判断物品是否为本模组的无限磁盘，并提供对应的 `InfiniteDiskCellInventory` 实例。
  - 通过 Singleton `INSTANCE` 注册到 AE2 的 `StorageCells`。

- `InfiniteDiskCellInventory`
  - 将单个无限磁盘映射为一个 AE2 可用的存储单元。
  - 管理与物品 Stack/NBT 的关联，以及对后端存储池的访问。

- `InfiniteDiskStorage`
  - 实际的存储容器，实现类似 `IMEInventory` 的行为。
  - 对外提供增加/移除物品、查询现有物品和统计信息的接口。

- `InfiniteDiskManager`
  - 全局管理器，管理所有磁盘对应的 `InfiniteDiskStorage` 实例。
  - 可能负责：
    - 根据磁盘唯一 ID 创建/获取存储实例。
    - 与世界保存系统整合（确保服务器重启后仍能恢复数据）。
    - 处理一致性与清理逻辑（例如删除不再存在的磁盘数据）。

### 4.3 菜单、GUI 与网络

- `InfinityDiskMenu`
  - Forge 容器菜单，用于服务端逻辑（槽位、玩家物品栏、与 ME 网络交互等）。
  - 通过 `MENUS` `DeferredRegister` 注册对应的菜单类型。

- `InfinityDiskScreen`
  - 对应菜单的客户端界面，绘制背景、按钮、文本等。
  - 利用网络包向服务器发送交互指令（例如过滤条件、分页、操作按钮等）。

- `NetworkHandler`
  - 创建 Forge 网络通道（通常使用 `SimpleChannel`）。
  - 注册 `PacketSyncDiskState` 等数据包，分配 ID、指定编码/解码与处理逻辑。

- `PacketSyncDiskState`
  - 用于同步服务器端计算出的磁盘状态（如总物品数、种类数、占用情况等）到客户端。

- `ClientDiskStateCache`
  - 客户端缓存层，接收 `PacketSyncDiskState` 后更新，并为 GUI 提供即时显示的数据源。

### 4.4 命令与事件

- `InfinityDiskCommands`
  - 在 `RegisterCommandsEvent` 中注册，提供调试/管理命令：
    - 例如：重建索引、清理无效数据、打印当前统计等（具体见实现）。

- 事件处理
  - `Infinitydisk` 中：
    - `onServerStarting(ServerStartingEvent event)`：服务器启动时进行日志或初始化工作。
    - `onRegisterCommands(RegisterCommandsEvent event)`：绑定命令注册到 `InfinityDiskCommands`。
  - 内部静态类 `ClientModEvents`
    - `onClientSetup(FMLClientSetupEvent event)`：客户端初始化阶段进行日志或 Screen 注册（通常与 `ClientSetup` 配合使用）。

---

## 5. 构建与在开发环境中运行

以下步骤假设你已经具备基本的 Java/Gradle/Minecraft Forge 开发环境，并安装了 JDK 17。

### 5.1 导入项目

1. 克隆本仓库到本地。
2. 在 IDE（例如 IntelliJ IDEA）中选择「Open」或「Import Gradle Project」，打开仓库根目录。
3. IDE 应自动识别 `build.gradle` 并完成 Gradle 同步。

### 5.2 使用 Gradle Wrapper 构建

在仓库根目录下（Windows PowerShell）：

```powershell
./gradlew.bat build
```

- 第一次执行会下载 ForgeGradle 和 Minecraft 相关依赖，时间较长。
- 构建成功后，模组 jar 通常位于：
  - `build/libs/ae_infinity_disk-<version>.jar`

### 5.3 在开发环境中启动客户端/服务端

ForgeGradle 已在 `build.gradle` 中定义了若干运行配置：

- 客户端：
  - Gradle 任务：`runClient`
  - 在 IDE 中通常会生成相应的 Run Configuration，可直接运行。

- 服务端：
  - Gradle 任务：`runServer`（使用 `--nogui` 参数）。

示例命令（PowerShell）：

```powershell
./gradlew.bat runClient
# 或
./gradlew.bat runServer
```

### 5.4 开发建议

- 确保本地安装的 JDK 为 17，且与 Gradle Toolchain 一致。
- 若需调试 AE2 交互：
  - 建议在开发环境中同时加载 AE2 和本模组。
  - 可配合 `InfinityDiskCommands` 中的命令查看内部状态或重现问题。

---

## 6. 后续可扩展点建议

基于当前架构，可以考虑以下方向进行扩展或优化：

1. **容量与性能控制**
   - 引入「软上限」或「优雅退化」机制，防止极端大规模物品存储导致 tick 延迟或网络压力。
   - 对 `InfiniteDiskStorage` 的内部结构进行优化（例如分片存储、Lazy 加载、批量同步）。

2. **多维度统计与可视化**
   - 扩展 `PacketSyncDiskState` 与 `ClientDiskStateCache`，提供更丰富的统计信息（例如：按物品类型、模组来源分组）。
   - 在 `InfinityDiskScreen` 中增加图表/排序/过滤功能，帮助玩家管理海量物品。

3. **权限与安全控制**
   - 引入权限系统或集成现有权限模组，限制某些玩家对无限磁盘的访问或管理能力。
   - 在命令与管理界面中增加权限校验，避免误操作或滥用。

4. **兼容更多模组与平台**
   - 针对其他常见存储类模组（如 Storage Drawers、Refined Storage 等）探索兼容或迁移方案（视设计方向而定）。
   - 若有计划，可在内部抽象出跨平台接口层，为未来移植到 NeoForge/Fabric 等平台做准备。

5. **数据可靠性与备份**
   - 为 `InfiniteDiskManager` 提供额外的备份与恢复机制（例如：周期性快照）。
   - 在严重错误时能优雅降级或导出当前磁盘数据，便于问题排查。

6. **测试与验证**
   - 利用 Forge 的 `gameTestServer` 运行配置，为核心存储/同步逻辑编写 GameTest 用例。
   - 在极端压力场景（海量物品、频繁插拔磁盘）下进行基准测试与内存分析。

---

本 `PROJECT_OVERVIEW.md` 作为开发者向总览文档，建议在对项目进行较大重构或版本升级（例如 Minecraft/Forge/AE2 大版本变更）后进行同步更新，以保持信息准确。
