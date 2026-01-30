# AttributeCore

**Version**: 1.0.0.0  
**Minecraft**: Paper 1.20+  
**TabooLib**: 6.2.4  
**架构**: 基于 SX-Attribute 3.x 完全重构

---

## 📖 项目简介

AttributeCore 是一个轻量级 Minecraft 属性系统插件，使用 **Kotlin + TabooLib 6.x** 重写，完全参考 **SX-Attribute 3.x** 的架构设计。

### ✨ 核心特性

- ✅ **SX-Attribute 兼容架构** - 完全对标 SX-Attribute 3.x 设计理念
- ✅ **Lore 属性读取** - 支持彩色代码格式（如 `§c攻击力 §f100`）
- ✅ **NBT 属性读取** - 使用 TabooLib ItemTag API 读取 NBT 数据
- ✅ **可扩展属性系统** - 基于 `SubAttribute` 抽象类轻松扩展新属性
- ✅ **战斗力计算** - 自动计算实体综合战斗力
- ✅ **优先级管理** - 支持属性优先级排序
- ✅ **高性能设计** - ConcurrentHashMap 缓存，异步事件处理

---

## 🎯 已实现功能

### **核心属性（v1.0.0）**

| 属性名称 | 类型 | 说明 | Lore 格式 |
|---------|------|------|----------|
| **攻击力** | Attack | 增加物理伤害 | `§c攻击力 §f100` |
| **防御力** | Defence | 减少受到的伤害 | `§9防御力 §f50` |
| **暴击率** | Attack | 触发暴击的概率（%） | `§6暴击率 §f25%` |
| **暴击伤害** | Other | 暴击时的伤害倍率（%） | `§6暴击伤害 §f150%` |

### **系统功能**

- ✅ **属性管理器** (`AttributeManager`) - 实体属性缓存和加载
- ✅ **属性读取器** (`ItemAttributeReader`) - Lore + NBT 双读取
- ✅ **伤害监听器** (`DamageListener`) - 攻击/防御属性处理
- ✅ **装备监听器** (`EquipmentListener`) - 装备变更自动更新
- ✅ **战斗力计算** - 加权计算实体综合战斗力

---

## 🚀 快速开始

### **安装**

1. 下载 `AttributeCore-1.0.0.0.jar`
2. 放入服务器 `plugins/` 目录
3. 重启服务器
4. 配置文件自动生成在 `plugins/AttributeCore/`

### **配置文件**

```yaml
# config.yml - 主配置文件
attribute-priority:        # 属性优先级列表
  - "attack_damage"
  - "defense"
  - "crit_chance"
  - "crit_damage"

combat-power-weights:      # 战斗力权重
  attack_damage: 1.5
  defense: 1.2
  crit_chance: 0.8
  crit_damage: 0.5

damage:
  enabled: true
  min-damage: 0.1
  defense-formula:
    base-value: 100        # 防御公式：减伤 = 防御/(防御+100)
```

---

## 📦 属性格式示例

### **Lore 属性格式**

```yaml
# 物品 Lore 示例
lore:
  - "§c攻击力 §f100"
  - "§9防御力 §f50"
  - "§6暴击率 §f25%"
  - "§6暴击伤害 §f150%"
```

### **NBT 属性格式**

```json
{
  "AttributeCore": {
    "attack_damage": 100,
    "defense": 50,
    "crit_chance": 25,
    "crit_damage": 150
  }
}
```

---

## 🛠️ 开发者指南

### **创建自定义属性**

```kotlin
package com.attributecore.attribute

import com.attributecore.data.*
import com.attributecore.event.*
import org.bukkit.entity.Player

class LifeSteal : SubAttribute("life_steal", AttributeType.Attack) {
    init {
        priority = 10
        combatPowerWeight = 0.6
        register(this)
    }

    private val pattern = createPattern("吸血", "%")

    override fun loadAttribute(attributeData: AttributeData, lore: String) {
        matchValue(lore, pattern)?.let {
            attributeData.add(name, it)
        }
    }

    override fun eventMethod(attributeData: AttributeData, eventData: EventData) {
        if (eventData is DamageEventData) {
            val lifeSteal = attributeData[name]
            if (lifeSteal > 0) {
                val heal = eventData.damage * (lifeSteal / 100.0)
                if (eventData.attacker is Player) {
                    val player = eventData.attacker as Player
                    player.health = (player.health + heal).coerceAtMost(player.maxHealth)
                }
            }
        }
    }

    override fun getPlaceholder(attributeData: AttributeData, player: Player, identifier: String): Any? {
        return when (identifier) {
            "life_steal" -> attributeData[name]
            else -> null
        }
    }

    override fun getPlaceholders(): List<String> = listOf("life_steal")
}
```

### **注册属性**

在 `AttributeCore.kt` 的 `onEnable` 中添加：

```kotlin
LifeSteal()
```

---

## 📂 项目结构

```
AttributeCore/
├── src/main/kotlin/com/attributecore/
│   ├── AttributeCore.kt          # 主插件类
│   ├── data/
│   │   ├── AttributeData.kt      # 属性数据容器（Map 存储）
│   │   ├── AttributeType.kt      # 属性类型枚举
│   │   └── SubAttribute.kt       # 属性抽象基类
│   ├── manager/
│   │   ├── AttributeManager.kt   # 属性管理器
│   │   └── ItemAttributeReader.kt# 物品属性读取器
│   ├── listener/
│   │   ├── DamageListener.kt     # 伤害事件监听
│   │   └── EquipmentListener.kt  # 装备变更监听
│   ├── attribute/                # 具体属性实现
│   │   ├── AttackDamage.kt
│   │   ├── Defense.kt
│   │   ├── CritChance.kt
│   │   └── CritDamage.kt
│   └── event/
│       └── EventData.kt          # 事件数据类
└── src/main/resources/
    └── config.yml                # 主配置文件
```

---

## 🔧 技术栈

- **Language**: Kotlin 2.2.0
- **Framework**: TabooLib 6.2.4-e6c8347
- **Platform**: Paper 1.20+
- **Build Tool**: Gradle 8.14.3
- **Architecture**: SX-Attribute 3.x 架构

---

## 📋 版本对比

| 特性 | SX-Attribute (Java) | AttributeCore (Kotlin) |
|------|---------------------|------------------------|
| 属性存储 | `double[][]` 二维数组 | `Map<String, Double>` |
| 配置管理 | 原生 `YamlConfiguration` | TabooLib `Config` |
| NBT 读取 | 第三方 NBT 库 | TabooLib `ItemTag` |
| 事件系统 | Bukkit Event | TabooLib `@SubscribeEvent` |
| 生命周期 | `onEnable/onDisable` | `@Awake(LifeCycle)` |
| 异步操作 | `Bukkit.getScheduler()` | TabooLib `submit(async)` |
| 语言 | Java | Kotlin + DSL |

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交修改 (`git commit -m 'Add AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

---

## 📄 许可证

本项目采用 MIT 许可证。

---

## 📝 更新日志

### v1.0.0.0 (2026-01-30)
- 🎉 **全面重构** - 完全基于 SX-Attribute 3.x 架构重写
- ✅ 实现核心属性系统：攻击力、防御力、暴击率、暴击伤害
- ✅ 支持 Lore 属性读取（彩色代码格式）
- ✅ 支持 NBT 属性读取（TabooLib ItemTag）
- ✅ 属性优先级管理系统
- ✅ 战斗力计算系统
- ✅ 高性能缓存机制
- 🔧 使用 Kotlin 2.2.0 + TabooLib 6.2.4
- 📦 版本号重置为 1.0.0.0

---

## 🙏 致谢

- **SX-Attribute** - 架构设计参考
- **TabooLib** - 强大的 Minecraft 插件框架

---

## 📮 联系方式

- **Issues**: [GitHub Issues](https://github.com/YourUsername/AttributeCore/issues)
- **Wiki**: [项目文档](https://github.com/YourUsername/AttributeCore/wiki)

---

**Made with ❤️ using Kotlin + TabooLib**