# AttributeCore API 使用指南

本文档介绍如何在外部插件中调用 AttributeCore 提供的 API。

---

## 快速开始

### Maven 依赖 (将 AttributeCore 打包到你的插件)

```xml
<dependency>
    <groupId>com.attributecore</groupId>
    <artifactId>AttributeCore</artifactId>
    <version>1.3.0.0</version>
    <scope>provided</scope>
</dependency>
```

### Gradle 依赖

```kotlin
dependencies {
    compileOnly("com.attributecore:AttributeCore:1.3.0.0")
}
```

---

## API 模块概览

AttributeCore API 分为 5 大模块：

1. **基础属性管理** - 读写实体属性、管理属性来源
2. **属性查询统计** - 批量查询、获取非零属性
3. **护盾系统** - 护盾增减、查询
4. **战斗力计算** - 获取玩家战斗力评分
5. **伤害系统 (高级)** - 创建自定义伤害、设置伤害标签

---

## 1. 基础属性管理 API

### Java 示例

```java
import com.attributecore.api.AttributeCoreAPI;
import org.bukkit.entity.Player;

public class MySkillPlugin {
    
    // 临时增加玩家攻击力 (BUFF 系统)
    public void applyAttackBuff(Player player, double amount, int durationSeconds) {
        UUID uuid = player.getUniqueId();
        
        // 1. 添加属性 (来源: 你的插件名)
        AttributeCoreAPI.setEntityAPIData("MySkillPlugin", uuid, "attack_damage", amount);
        
        // 2. 刷新玩家属性
        AttributeCoreAPI.updateEntity(player);
        
        // 3. 定时移除 BUFF
        Bukkit.getScheduler().runTaskLater(this, () -> {
            AttributeCoreAPI.removeEntityAPIData("MySkillPlugin", uuid);
            AttributeCoreAPI.updateEntity(player);
        }, durationSeconds * 20L);
    }
    
    // 读取玩家当前总攻击力
    public double getPlayerAttack(Player player) {
        return AttributeCoreAPI.getAttributeTotal(player, "attack_damage");
    }
    
    // 插件卸载时清理所有数据
    @Override
    public void onDisable() {
        AttributeCoreAPI.removePluginAllEntityData("MySkillPlugin");
    }
}
```

### Kotlin 示例

```kotlin
import com.attributecore.api.AttributeCoreAPI

class BuffManager {
    
    fun applyTemporaryAttribute(player: Player, key: String, value: Double, durationTicks: Long) {
        AttributeCoreAPI.setEntityAPIData("MyPlugin", player.uniqueId, key, value)
        AttributeCoreAPI.updateEntity(player)
        
        submit(delay = durationTicks) {
            AttributeCoreAPI.removeEntityAPIData("MyPlugin", player.uniqueId)
            AttributeCoreAPI.updateEntity(player)
        }
    }
    
    fun getPlayerStats(player: Player): Map<String, Double> {
        return AttributeCoreAPI.getAttributesBatch(player, listOf(
            "attack_damage", "crit_chance", "crit_damage", "defense"
        ))
    }
}
```

---

## 2. 属性查询统计 API

### 批量查询多个属性

```java
// Java
Map<String, Double> stats = AttributeCoreAPI.getAttributesBatch(player, Arrays.asList(
    "attack_damage", "crit_chance", "health_max", "defense"
));

double attack = stats.get("attack_damage");
double critChance = stats.get("crit_chance");
```

```kotlin
// Kotlin
val stats = AttributeCoreAPI.getAttributesBatch(player, listOf(
    "attack_damage", "crit_chance", "health_max", "defense"
))
```

### 获取所有非零属性

```java
// Java - 用于属性面板显示
Map<String, DoubleArray> allAttrs = AttributeCoreAPI.getAllNonZeroAttributes(player);
allAttrs.forEach((key, values) -> {
    double base = values[0];
    double bonus = values[1];
    player.sendMessage(key + ": " + base + " + " + bonus);
});
```

---

## 3. 护盾系统 API

### 技能增加护盾

```java
// Java - 释放技能增加护盾
public void castShieldSkill(Player player) {
    double shieldAmount = 500.0;
    AttributeCoreAPI.modifyShield(player, shieldAmount);
    player.sendMessage("§b获得 " + shieldAmount + " 点护盾!");
}

// 检查护盾状态
public void checkShield(Player player) {
    double current = AttributeCoreAPI.getCurrentShield(player);
    double max = AttributeCoreAPI.getMaxShield(player);
    double percent = AttributeCoreAPI.getShieldPercent(player);
    
    player.sendMessage(String.format("护盾: %.0f / %.0f (%.1f%%)", current, max, percent));
}
```

```kotlin
// Kotlin - 技能增加护盾
fun castShieldSkill(player: Player) {
    AttributeCoreAPI.modifyShield(player, 500.0)
    player.sendMessage("§b获得 500 点护盾!")
}
```

---

## 4. 战斗力计算 API

### 获取玩家战斗力

```java
// Java - 战斗力排行榜
public void showCombatPowerRanking() {
    List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
    players.sort((p1, p2) -> {
        double cp1 = AttributeCoreAPI.getCombatPower(p1);
        double cp2 = AttributeCoreAPI.getCombatPower(p2);
        return Double.compare(cp2, cp1); // 降序
    });
    
    for (int i = 0; i < Math.min(10, players.size()); i++) {
        Player p = players.get(i);
        double cp = AttributeCoreAPI.getCombatPower(p);
        Bukkit.broadcastMessage(String.format("#%d %s - %.0f", i + 1, p.getName(), cp));
    }
}
```

```kotlin
// Kotlin - 战斗力查询
fun showPlayerCombatPower(player: Player) {
    val cp = AttributeCoreAPI.getCombatPower(player)
    player.sendMessage("§e当前战斗力: §f${cp.toInt()}")
}
```

---

## 5. 伤害系统 API (高级)

### 创建自定义技能伤害

```java
// Java - 火球术技能 (火焰伤害 + 物理伤害)
public void castFireball(Player caster, LivingEntity target) {
    // 1. 触发虚拟伤害事件 (用于创建 DamageData)
    EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(
        caster, target, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 0.1
    );
    
    // 2. 创建伤害上下文
    DamageData damageData = AttributeCoreAPI.createDamageContext(caster, target, event);
    
    // 3. 添加技能伤害 (物理 + 火焰)
    double physicalDamage = AttributeCoreAPI.getAttributeTotal(caster, "attack_damage") * 0.5;
    double fireDamage = AttributeCoreAPI.getAttributeTotal(caster, "fire_damage") * 2.0;
    
    AttributeCoreAPI.addTypedDamage(damageData, "PHYSICAL", physicalDamage);
    AttributeCoreAPI.addTypedDamage(damageData, "FIRE", fireDamage);
    
    // 4. 添加技能标签 (用于特殊机制判断)
    AttributeCoreAPI.addDamageTag(damageData, "SKILL");
    AttributeCoreAPI.addDamageTag(damageData, "FIRE_MAGIC");
    
    // 5. 设置伤害倍率 (技能伤害 +50%)
    AttributeCoreAPI.setDamageMultiplier(damageData, 1.5);
    
    // 6. 触发事件 (让 AttributeCore 自动计算最终伤害)
    Bukkit.getPluginManager().callEvent(event);
}
```

### 创建必定暴击的技能

```java
// Java - 绝对暴击技能 (忽略暴击抗性)
public void castCriticalStrike(Player caster, LivingEntity target) {
    EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(
        caster, target, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 0.1
    );
    
    DamageData damageData = AttributeCoreAPI.createDamageContext(caster, target, event);
    
    // 添加物理伤害
    double damage = AttributeCoreAPI.getAttributeTotal(caster, "attack_damage") * 3.0;
    AttributeCoreAPI.addTypedDamage(damageData, "PHYSICAL", damage);
    
    // 强制暴击 (设置 critTier = 1 或手动设置暴击倍率)
    damageData.rollCrit(100.0); // 100% 暴击率
    
    // 添加标签
    AttributeCoreAPI.addDamageTag(damageData, "CRITICAL_STRIKE_SKILL");
    
    Bukkit.getPluginManager().callEvent(event);
}
```

### 创建穿甲伤害

```java
// Java - 真实伤害 (无视护甲)
public void castTrueDamage(Player caster, LivingEntity target) {
    EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(
        caster, target, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 0.1
    );
    
    DamageData damageData = AttributeCoreAPI.createDamageContext(caster, target, event);
    
    // 添加伤害
    AttributeCoreAPI.addTypedDamage(damageData, "PHYSICAL", 500.0);
    
    // 设置穿甲标志 (忽略护甲计算)
    AttributeCoreAPI.setDamageFlags(damageData, false, true);
    
    // 添加标签
    AttributeCoreAPI.addDamageTag(damageData, "TRUE_DAMAGE");
    
    Bukkit.getPluginManager().callEvent(event);
}
```

---

## 完整集成示例：BUFF 管理系统

```java
import com.attributecore.api.AttributeCoreAPI;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BuffSystem extends JavaPlugin {
    
    private static final String SOURCE_NAME = "BuffSystem";
    private final ConcurrentHashMap<UUID, Map<String, Double>> activeBuffs = new ConcurrentHashMap<>();
    
    @Override
    public void onEnable() {
        // 初始化
    }
    
    @Override
    public void onDisable() {
        // 清理所有 BUFF 数据
        AttributeCoreAPI.removePluginAllEntityData(SOURCE_NAME);
        activeBuffs.clear();
    }
    
    // 添加 BUFF
    public void addBuff(Player player, String attributeKey, double value, int durationSeconds) {
        UUID uuid = player.getUniqueId();
        
        // 1. 记录 BUFF
        activeBuffs.putIfAbsent(uuid, new ConcurrentHashMap<>());
        Map<String, Double> buffs = activeBuffs.get(uuid);
        buffs.put(attributeKey, buffs.getOrDefault(attributeKey, 0.0) + value);
        
        // 2. 应用到 AttributeCore
        AttributeCoreAPI.setEntityAPIData(SOURCE_NAME, uuid, attributeKey, buffs.get(attributeKey));
        AttributeCoreAPI.updateEntity(player);
        
        // 3. 定时移除
        getServer().getScheduler().runTaskLater(this, () -> {
            removeBuff(player, attributeKey, value);
        }, durationSeconds * 20L);
        
        player.sendMessage("§a获得 BUFF: " + attributeKey + " +" + value + " (持续 " + durationSeconds + "s)");
    }
    
    // 移除 BUFF
    public void removeBuff(Player player, String attributeKey, double value) {
        UUID uuid = player.getUniqueId();
        Map<String, Double> buffs = activeBuffs.get(uuid);
        if (buffs == null) return;
        
        double newValue = buffs.getOrDefault(attributeKey, 0.0) - value;
        if (newValue <= 0) {
            buffs.remove(attributeKey);
        } else {
            buffs.put(attributeKey, newValue);
        }
        
        // 更新到 AttributeCore
        if (newValue > 0) {
            AttributeCoreAPI.setEntityAPIData(SOURCE_NAME, uuid, attributeKey, newValue);
        } else {
            AttributeCoreAPI.removeEntityAPIData(SOURCE_NAME, uuid);
        }
        AttributeCoreAPI.updateEntity(player);
    }
    
    // 清理玩家所有 BUFF
    public void clearAllBuffs(Player player) {
        UUID uuid = player.getUniqueId();
        activeBuffs.remove(uuid);
        AttributeCoreAPI.removeEntityAPIData(SOURCE_NAME, uuid);
        AttributeCoreAPI.updateEntity(player);
    }
}
```

---

## 常见问题 FAQ

### Q1: 修改属性后何时生效？
**A:** 调用 `setEntityAPIData` 后，必须手动调用 `updateEntity(entity)` 才会刷新属性。

### Q2: 如何避免属性冲突？
**A:** 使用唯一的 `source` 标识符（建议用插件名），AttributeCore 会自动隔离不同来源的数据。

### Q3: 如何在插件卸载时清理数据？
**A:** 在 `onDisable()` 中调用：
```java
AttributeCoreAPI.removePluginAllEntityData("YourPluginName");
```

### Q4: 自定义伤害如何触发？
**A:** 创建 `DamageData` 后，必须调用 `Bukkit.getPluginManager().callEvent(event)` 让 AttributeCore 监听器处理。

### Q5: 护盾如何自动恢复？
**A:** AttributeCore 自动处理护盾恢复，速度由 `config.yml` 的 `shield.regen_speed` 配置。

---

## API 参考速查表

| 方法 | 功能 | 返回值 |
|------|------|--------|
| `getEntityData(entity)` | 获取实体属性数据 | `AttributeData` |
| `setEntityAPIData(source, uuid, key, value)` | 设置属性 | void |
| `removeEntityAPIData(source, uuid)` | 移除属性 | void |
| `updateEntity(entity)` | 刷新属性 | void |
| `getAttributeTotal(entity, key)` | 获取总值 | `Double` |
| `getAttributesBatch(entity, keys)` | 批量查询 | `Map<String, Double>` |
| `getCurrentShield(entity)` | 当前护盾 | `Double` |
| `modifyShield(entity, delta)` | 修改护盾 | void |
| `getCombatPower(entity)` | 战斗力 | `Double` |
| `createDamageContext(...)` | 创建伤害上下文 | `DamageData` |
| `addTypedDamage(data, type, amount)` | 添加伤害 | void |

---

## 联系与反馈

- **项目地址**: [GitHub - AttributeCore](https://github.com/YourUsername/AttributeCore)
- **问题反馈**: [Issues](https://github.com/YourUsername/AttributeCore/issues)
- **更新日志**: 查看 `CHANGELOG.md`

---

**文档版本**: v1.3.0.0  
**最后更新**: 2026-01-29
