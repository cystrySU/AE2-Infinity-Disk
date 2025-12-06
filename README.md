# AE Infinity Disk
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/cystrySU/AE2-Infinity-Disk)<br>

[中文文档](./doc/readme-zh.md) 

AE Infinity Disk is an add-on mod for [Applied Energistics 2](https://appliedenergistics.github.io/) targeting Minecraft 1.19.2.  
It introduces an ME storage disk with effectively unlimited capacity and item variety, designed for large-scale storage networks.

---

## Overview

AE Infinity Disk provides a single high-end ME storage disk that can hold an unlimited number of items and an unlimited number of distinct item types.  
It is intended for late-game AE2 networks where conventional drive capacities and type limits become a bottleneck.

Key goals of this mod include:

- Removing practical storage limits for AE2 item networks.
- Maintaining good performance even with extremely large inventories.
- Providing configurable behavior to better fit different modpack balance philosophies.

---

## Features

- **Unlimited capacity** – Stores an arbitrary number of items without a hard limit.
- **Unlimited types** – Stores any number of distinct item types; no type cap.
- **Performance-oriented design** – Optimized caching and dirty-marking mechanisms to keep interaction responsive on large networks.
- **Configurable behavior** – Adjustable energy usage and soft limits through the configuration file.
- **Multilingual support** – Localizations for Chinese, English, Japanese, Korean and many other languages.

---

## Requirements

| Dependency               | Version       |
|--------------------------|--------------|
| Minecraft                | 1.19.2       |
| Forge                    | 43.5.0+      |
| Applied Energistics 2    | 12.9.5+      |

Ensure that you are using a compatible version of Forge and AE2. Other Minecraft or Forge versions are not supported.

---

## Installation

1. Install Minecraft 1.19.2 with the appropriate Forge version (43.5.0 or newer).
2. Install Applied Energistics 2 version 12.9.5 or newer.
3. Download the AE Infinity Disk mod JAR file for the correct Minecraft version.
4. Place the JAR file into your `mods` folder.
5. Launch the game and verify that both Applied Energistics 2 and AE Infinity Disk are loaded.

---

## Configuration

The main configuration file is generated at:

`config/ae_infinity_disk-common.toml`

The following options are available:

| Option                       | Default       | Description                                                                 |
|-----------------------------|--------------|-----------------------------------------------------------------------------|
| `enableDynamicEnergyDrain`  | `false`      | Enables dynamic energy usage that increases with the amount of stored items |
| `baseIdleDrain`             | `1.0`        | Base idle energy consumption (AE/t)                                        |
| `maxIdleDrain`              | `512000.0`   | Maximum idle energy consumption (AE/t), soft cap at 512k AE/t              |

Adjust these values to match your modpack’s balance needs. Increasing the drains will make the infinity disk more power-hungry, particularly in large networks.

---

## Changelog

All notable changes to this project are documented in this section.  
Version numbers follow the release tags on the project’s distribution platform.

### 1.1.2

- Removed internal debug commands that were unintentionally left accessible to end users.
- No gameplay or balance changes were introduced in this version.
- This release is intended as a minor maintenance update to clean up development artifacts.

### 1.1.1

- Changed the default value of `enableDynamicEnergyDrain` to `false`, disabling dynamic idle energy consumption out of the box.
- Introduced a soft cap for idle energy drain with `maxIdleDrain` set to `512000.0` AE/t (512k AE/t) to prevent extreme power usage in large networks.
- Updated internal configuration handling logic to ensure that new configuration options are applied correctly without breaking existing setups.

### 1.1.0

- Implemented a caching layer for internal storage lookups, significantly reducing overhead when interacting with very large networks.
- Added dirty-marking mechanisms to avoid unnecessary writes when the stored state has not changed.
- Optimized NBT read/write operations for the infinity disk, reducing the cost of saving and loading large inventories.
- Revised the GUI for the infinity disk: simplified background visuals and reduced unnecessary texture usage for improved clarity and performance.
- Introduced string caching in the GUI to avoid repeated string allocations and rendering work, especially on frequently opened interfaces.

### 1.0.0

- Initial public release of AE Infinity Disk.
- Added the infinity ME storage disk item, providing effectively unlimited capacity and type count for AE2 storage networks.
- Implemented basic configuration options, including idle energy drain and related balancing settings.
- Included initial localization files for multiple languages and baseline integration with Applied Energistics 2.

---

## License

This project is licensed under the [GNU Lesser General Public License v3.0](LICENSE) (LGPL-3.0-only).

You are free to use this mod in modpacks under the terms of the LGPL-3.0-only license.
