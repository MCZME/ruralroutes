# 乡野商路 (Rural Routes)

一个 Minecraft 模组，提供以村庄交换为基础的新资源获取途径。

## 概述

乡野商路为玩家提供了一套独立的资源获取体系，玩家可以通过与不同村庄进行物资交换来获取所需资源。该系统与原版流程完全兼容，不强制玩家使用，但为探索世界提供了新的动力。

## 核心特性

- **村庄交换系统**：独立于原版村民交易的全新交换机制
- **群系主题**：不同群系的村庄有不同的产出倾向和需求倾向
- **特产系统**：每个村庄随机拥有2-3种特产，驱动探索
- **有限库存**：村庄库存有限，定时刷新，形成玩家流转的自然动力

## 详细文档

请参阅 [docs/概述.md](docs/概述.md) 获取完整设计文档。

## 开发环境

本模组基于 NeoForged 开发。

- Minecraft 版本：1.21.4
- NeoForge 版本：请参阅 `gradle.properties`

### 构建与运行

```bash
# 刷新依赖
./gradlew --refresh-dependencies

# 清理构建
./gradlew clean

# 构建模组
./gradlew build
```

## 资源

- [NeoForged 文档](https://docs.neoforged.net/)
- [NeoForged Discord](https://discord.neoforged.net/)