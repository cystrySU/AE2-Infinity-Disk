# AE Infinity Disk

<p align="center">
  <strong>无限容量 · 无限种类 · 无限可能</strong>
</p>

---

## 📖 简介

**AE Infinity Disk** 是一个面向 Minecraft 1.19.2 的 [Applied Energistics 2](https://appliedenergistics.github.io/) 附属模组，为 AE2 提供一枚具有**无限容量**和**无限种类**的 ME 存储磁盘。

## ✨ 特性

- 🎯 **无限容量** - 存储任意数量的物品，没有上限
- 📦 **无限种类** - 存储任意类型的物品，不受种类限制
- ⚡ **高性能** - 经过优化的缓存机制，确保大规模存储时的流畅体验
- 🔧 **可配置** - 灵活的配置选项，包括能量消耗、软上限等
- 🌐 **多语言支持** - 支持中文、英文、日文、韩文等 14 种语言

## 📋 依赖

| 依赖项 | 版本 |
|--------|------|
| Minecraft | 1.19.2 |
| Forge | 43.5.0+ |
| Applied Energistics 2 | 12.9.5+ |

## ⚙️ 配置

配置文件：`config/ae_infinity_disk-common.toml`

| 配置项 | 默认值 | 描述 |
|--------|--------|------|
| `enableDynamicEnergyDrain` | `false` | 启用动态能量消耗（随物品数量增加） |
| `baseIdleDrain` | `1.0` | 基础空闲能量消耗 (AE/t) |
| `maxIdleDrain` | `512000.0` | 最大能量消耗上限 (512k AE/t) |

## 📝 更新日志

### 1.1.2
- 去除调试指令

### 1.1.1
- 默认禁用动态能量消耗
- 新增 512k AE/t 能量消耗上限

### 1.1.0
- 性能优化：缓存机制、脏标记、轻量级 NBT 读取
- GUI 优化：纯色背景、字符串缓存

### 1.0.0
- 初始版本发布
  
## 📜 许可证

本项目采用 [GNU Lesser General Public License v3.0](LICENSE)（LGPL-3.0-only）。
