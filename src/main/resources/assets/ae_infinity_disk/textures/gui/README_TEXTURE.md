# GUI 纹理说明

## 当前状态: 使用 Minecraft 默认纹理 ✅

已配置为使用 Minecraft 的默认容器背景纹理:
```java
ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");
```

这是 Minecraft 原版大箱子的 GUI 背景，简洁通用。

## 可用的 Minecraft 默认 GUI 纹理

| 纹理路径 | 说明 |
|---------|------|
| `minecraft:textures/gui/container/generic_54.png` | 大箱子背景（当前使用） |
| `minecraft:textures/gui/container/dispenser.png` | 发射器背景 |
| `minecraft:textures/gui/container/hopper.png` | 漏斗背景 |
| `minecraft:textures/gui/container/furnace.png` | 熔炉背景 |

## 物品纹理

磁盘物品使用自定义纹理:
- 路径: `ae_infinity_disk:item/infinity_disk`
- 文件: `textures/item/infinity_disk.png`

## 如果想使用自定义 GUI 纹理

1. 创建 256x256 的 PNG 文件
2. 放入 `src/main/resources/assets/ae_infinity_disk/textures/gui/`
3. 修改 `InfinityDiskScreen.java` 中的 TEXTURE 路径
