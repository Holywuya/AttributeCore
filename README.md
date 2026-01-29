# AttributeCore

**Version**: 1.4.0.0  
**Minecraft**: Paper 1.21.1  
**TabooLib**: 6.2.4

一个功能完善的 Minecraft 服务器属性系统插件，基于 TabooLib 6 构建，支持 JavaScript 脚本自定义属性、多类型伤害计算、护盾系统、战斗力评估等。

---

## 特性

- ✅ **JavaScript 脚本属性系统** - 支持动态加载 JS 脚本定义属性行为
- ✅ **多类型伤害桶架构** - 物理/元素伤害独立计算，支持抗性、减伤、穿透
- ✅ **护盾系统** - 独立护盾值管理，自动恢复，伤害抵扣优先级
- ✅ **暴击分层系统** - 支持多重暴击、暴击抗性、暴击韧性
- ✅ **战斗力计算** - 自动计算玩家综合战斗力评分
- ✅ **PlaceholderAPI 集成** - 提供丰富的占位符用于显示
- ✅ **完整外部 API** - 供其他插件调用（BUFF系统、技能系统等）
- ✅ **高性能设计** - 属性缓存、异步计算、优先级管理

---

## 快速开始

### 安装

1. 下载最新版本的 `AttributeCore-1.4.0.0.jar`
2. 将 JAR 文件放入服务器的 `plugins/` 目录
3. 重启服务器
4. 配置文件将自动生成在 `plugins/AttributeCore/`

### 配置文件

```yaml
# config.yml - 7大模块配置
combat:          # 伤害计算参数
elements:        # 元素系统配置
combat_power:    # 战斗力权重
shield:          # 护盾系统
attributes:      # 属性应用规则
logging:         # 调试日志
advanced:        # 高级参数
```

详细配置说明请查看 [配置文件说明](docs/CONFIG.md)

---

## 开发者文档

### API 使用指南

AttributeCore 提供完整的外部 API，支持其他插件调用。

**快速示例 (Java)**:
```java
import com.attributecore.api.AttributeCoreAPI;

// 给玩家添加临时属性 (BUFF)
AttributeCoreAPI.setEntityAPIData("MyPlugin", player.getUniqueId(), "attack_damage", 50.0);
AttributeCoreAPI.updateEntity(player);

// 获取玩家总攻击力
double attack = AttributeCoreAPI.getAttributeTotal(player, "attack_damage");

// 获取战斗力
double combatPower = AttributeCoreAPI.getCombatPower(player);
```

**详细文档**:
- 📘 [API 使用指南](docs/API_USAGE.md) - 完整 API 参考和集成示例
- 📗 [JavaScript 脚本开发](docs/SCRIPT_GUIDE.md) - 如何编写自定义属性脚本
- 📙 [配置文件说明](docs/CONFIG.md) - 配置参数详解

### API 模块

| 模块 | 功能 |
|------|------|
| 基础属性管理 | 读写实体属性、管理来源隔离 |
| 属性查询统计 | 批量查询、获取非零属性 |
| 护盾系统 | 护盾增减、查询百分比 |
| 战斗力计算 | 综合评分算法 |
| 伤害系统 (高级) | 自定义技能伤害、设置标签 |

---

## JavaScript 脚本示例

项目内置 11 个示例脚本，涵盖常见属性类型：

```
src/main/resources/scripts/
├── example_attack_damage.js          # 物理攻击力
├── example_crit_chance.js            # 暴击率
├── example_crit_damage.js            # 暴击伤害
├── example_armor_penetration.js      # 护甲穿透
├── example_defense.js                # 护甲值
├── example_fire_damage.js            # 火焰元素伤害
├── example_damage_reduction.js       # 伤害减免
├── example_health_regen.js           # 生命恢复（被动）
├── example_lifesteal.js              # 吸血
├── example_advanced_conditional.js   # 高级：条件暴击
└── example_advanced_chain_reaction.js # 高级：连锁闪电
```

**脚本模板 (attack_damage.js)**:
```javascript
function getSettings() {
    return {
        key: "attack_damage",
        name: "§c攻击力",
        type: "ATTACK",
        combatPower: 1.5,
        canNegative: false
    };
}

function runAttack(attr, attacker, victim, handle) {
    let damage = handle.getValue();
    attacker.addDamage(damage);  // AttributePlus 风格 API
}
```

查看 [脚本开发指南](docs/SCRIPT_GUIDE.md) 了解更多。

---

## PlaceholderAPI 占位符

| 占位符 | 说明 | 示例 |
|--------|------|------|
| `%ac_cp%` | 战斗力 | 1234.56 |
| `%ac_cp_int%` | 战斗力（整数） | 1234 |
| `%ac_health%` | 当前生命值 | 18.5 |
| `%ac_health_max%` | 最大生命值 | 20.0 |
| `%ac_health_percent%` | 生命百分比 | 92.5 |
| `%ac_level%` | 玩家等级 | 5 |
| `%ac_shield_percent%` | 护盾百分比 | 75.0 |
| `%ac_<属性键>%` | 任意属性值 | `%ac_attack_damage%` |

所有占位符支持 `_int` 后缀取整，例如 `%ac_health_int%`。

---

## 构建项目

### 构建发行版本

发行版本用于正常使用，不含 TabooLib 本体。

```bash
./gradlew build
```

生成文件：`build/libs/AttributeCore-1.4.0.0.jar`

### 构建开发版本

开发版本包含 TabooLib 本体，用于开发者调试（不可运行）。

```bash
./gradlew taboolibBuildApi -PDeleteCode
```

> 参数 `-PDeleteCode` 表示移除所有逻辑代码以减少体积。

---

## 项目结构

```
AttributeCore/
├── src/main/kotlin/com/attributecore/
│   ├── api/                    # 对外 API
│   │   ├── AttributeCoreAPI.kt # 主 API 入口
│   │   ├── JavaScriptAPI.kt    # JS 脚本 API
│   │   └── ...
│   ├── data/                   # 数据模型
│   │   ├── AttributeData.kt    # 属性数据容器
│   │   ├── DamageData.kt       # 伤害计算上下文
│   │   └── attribute/          # 属性定义
│   ├── manager/                # 核心管理器
│   │   ├── AttributeManager.kt # 属性管理
│   │   ├── ScriptManager.kt    # 脚本加载
│   │   ├── ShieldManager.kt    # 护盾系统
│   │   ├── DamageListener.kt   # 伤害监听
│   │   └── ...
│   └── ...
├── src/main/resources/
│   ├── config.yml              # 主配置文件
│   └── scripts/                # 示例脚本
│       └── example_*.js
└── docs/                       # 文档目录
    ├── API_USAGE.md            # API 使用指南
    ├── SCRIPT_GUIDE.md         # 脚本开发指南
    └── CONFIG.md               # 配置说明
```

---

## 技术栈

- **Language**: Kotlin 2.2.0
- **Framework**: TabooLib 6.2.4-e6c8347
- **Platform**: Paper 1.21.1
- **Build Tool**: Gradle 8.14.3
- **Script Engine**: Nashorn (JavaScript)

---

## 贡献

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建你的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交你的修改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

---

## 许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

---

## 更新日志

### v1.4.0.0 (2026-01-29)
- ✅ 重构：脚本 API 改为 AttributePlus 风格（`attacker.addDamage()` 而非 `handle.addDamage()`）
- ✅ 重写：ScriptEntity.kt 包装实体，暴露完整伤害/防御/暴击/护盾方法
- ✅ 简化：ScriptHandle.kt 仅保留数据查询功能
- ✅ 增强：AttributeExtensions.kt 添加 30+ 扩展方法
- ✅ 修复：ScriptManager 字段映射兼容 key/id 和 displayName/display
- ✅ 更新：11 个示例脚本全部适配新 API
- ✅ 新增：docs/SCRIPT_GUIDE.md 完整脚本开发指南

### v1.3.0.0 (2026-01-29)
- ✅ 新增：完整的外部 API（属性管理、护盾、战斗力、伤害系统）
- ✅ 新增：11 个 JavaScript 示例脚本
- ✅ 增强：PlaceholderAPI 支持（生命值、等级、护盾占位符）
- ✅ 优化：DamageData 多类型伤害桶架构
- ✅ 优化：AttributeData 缓存机制
- ✅ 重构：config.yml 七大模块配置
- ✅ 文档：API 使用指南、脚本开发指南

### v1.2.0.0 (2026-01-27)
- ✅ 核心系统重构
- ✅ JavaScript 加载系统优化

---

## 联系方式

- **Issues**: [GitHub Issues](https://github.com/YourUsername/AttributeCore/issues)
- **讨论**: [GitHub Discussions](https://github.com/YourUsername/AttributeCore/discussions)

---

**Made with ❤️ using TabooLib**