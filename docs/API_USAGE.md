# AttributeCore API 使用指南

本文档介绍如何在外部插件中调用 AttributeCore 提供的 API。

**文档版本**: v1.6.3.0  
**最后更新**: 2026-01-31

---

## 快速开始

### Gradle 依赖

```kotlin
dependencies {
    compileOnly("com.attributecore:AttributeCore:1.6.3.0")
}
```

### Maven 依赖

```xml
<dependency>
    <groupId>com.attributecore</groupId>
    <artifactId>AttributeCore</artifactId>
    <version>1.6.3.0</version>
    <scope>provided</scope>
</dependency>
```

---

## API 模块概览

AttributeCore 提供 4 个核心 API 模块：

| 模块 | 类名 | 职责 |
|------|------|------|
| **主 API** | `AttributeCoreAPI` | 实体属性读写、物品解析、伤害计算、战斗力 |
| **属性注册 API** | `AttributeAPI` | 属性列表查询、元数据获取、JS 属性管理 |
| **伤害系统 API** | `DamageAPI` | 伤害桶创建、元素伤害操作、抗性计算 |
| **元素系统 API** | `ElementAPI` | 元素光环管理、元素反应触发 |

---

## 1. AttributeCoreAPI - 主 API

### 1.1 实体属性查询

```java
// Java
import com.attributecore.api.AttributeCoreAPI;

// 获取实体完整属性数据（基础 + API 来源）
AttributeData data = AttributeCoreAPI.getEntityData(player);

// 获取单个属性
double attack = AttributeCoreAPI.getAttribute(player, "attack_damage");
double critPercent = AttributeCoreAPI.getAttributePercent(player, "crit_chance");
double finalDefense = AttributeCoreAPI.getAttributeFinal(player, "defense");

// 批量获取多个属性
Map<String, Double> stats = AttributeCoreAPI.getAttributesBatch(player, Arrays.asList(
    "attack_damage", "crit_chance", "crit_damage", "defense"
));

// 获取所有非零属性
Map<String, Double> nonZero = AttributeCoreAPI.getAllNonZeroAttributes(player);
```

```kotlin
// Kotlin
val data = AttributeCoreAPI.getEntityData(player)
val attack = AttributeCoreAPI.getAttribute(player, "attack_damage")
val stats = AttributeCoreAPI.getAttributesBatch(player, listOf("attack_damage", "defense"))
```

### 1.2 插件属性管理（BUFF 系统）

```java
// Java - 添加临时 BUFF
public void applyBuff(Player player, String attr, double value, int seconds) {
    UUID uuid = player.getUniqueId();
    
    // 设置属性（覆盖）
    AttributeCoreAPI.setPluginAttribute("MyPlugin", uuid, attr, value);
    
    // 或叠加属性
    AttributeCoreAPI.addPluginAttribute("MyPlugin", uuid, attr, value);
    
    // 刷新实体属性
    AttributeCoreAPI.updateEntityData(player);
    
    // 定时移除
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
        AttributeCoreAPI.removePluginAttribute("MyPlugin", uuid);
        AttributeCoreAPI.updateEntityData(player);
    }, seconds * 20L);
}

// 插件卸载时清理
@Override
public void onDisable() {
    AttributeCoreAPI.clearPluginAllData("MyPlugin");
}
```

```kotlin
// Kotlin
fun applyBuff(player: Player, attr: String, value: Double, ticks: Long) {
    AttributeCoreAPI.addPluginAttribute("MyPlugin", player.uniqueId, attr, value)
    AttributeCoreAPI.updateEntityData(player)
    
    submit(delay = ticks) {
        AttributeCoreAPI.removePluginAttribute("MyPlugin", player.uniqueId)
        AttributeCoreAPI.updateEntityData(player)
    }
}
```

### 1.3 物品属性解析

```java
// Java
ItemStack item = player.getInventory().getItemInMainHand();
AttributeData itemData = AttributeCoreAPI.loadItemData(item);

// 批量解析多个物品
List<ItemStack> equipment = Arrays.asList(
    player.getInventory().getHelmet(),
    player.getInventory().getChestplate(),
    player.getInventory().getLeggings(),
    player.getInventory().getBoots()
);
AttributeData totalData = AttributeCoreAPI.loadItemData(equipment);

// 从 Lore 解析
AttributeData loreData = AttributeCoreAPI.parseAttributesFromLore("§c攻击力 §f100");
```

### 1.4 伤害与战斗力

```java
// Java
// 构建伤害桶
DamageBucket bucket = AttributeCoreAPI.buildDamageBucket(attacker);

// 获取抗性
Map<Element, Double> resistances = AttributeCoreAPI.getResistances(victim);
double fireResist = AttributeCoreAPI.getResistance(victim, "FIRE");

// 计算最终伤害
double finalDamage = AttributeCoreAPI.calculateFinalDamage(attacker, victim, baseDamage);

// 战斗力
double combatPower = AttributeCoreAPI.getCombatPower(player);
```

---

## 2. DamageAPI - 伤害系统 API

### 2.1 创建伤害桶

```java
// Java
import com.attributecore.api.DamageAPI;
import com.attributecore.data.DamageBucket;
import com.attributecore.data.Element;

// 物理伤害
DamageBucket physical = DamageAPI.createPhysicalBucket(100.0);

// 元素伤害
DamageBucket fire = DamageAPI.createElementalBucket(Element.FIRE, 50.0);
DamageBucket ice = DamageAPI.createElementalBucket("ICE", 50.0);

// 混合伤害
Map<String, Double> damages = new HashMap<>();
damages.put("PHYSICAL", 100.0);
damages.put("FIRE", 50.0);
damages.put("ELECTRO", 30.0);
DamageBucket mixed = DamageAPI.createMixedBucket(damages);
```

### 2.2 伤害操作

```java
// Java
DamageBucket bucket = DamageAPI.createPhysicalBucket(100.0);

// 添加伤害
DamageAPI.addDamage(bucket, Element.FIRE, 50.0);
DamageAPI.addDamage(bucket, "ICE", 30.0);

// 设置伤害
DamageAPI.setDamage(bucket, Element.ELECTRO, 80.0);

// 获取伤害
double fireDmg = DamageAPI.getDamage(bucket, Element.FIRE);
double total = DamageAPI.getTotalDamage(bucket);
double elemental = DamageAPI.getElementalDamage(bucket);

// 乘算
DamageAPI.multiplyDamage(bucket, Element.FIRE, 1.5);  // 火伤害 * 1.5
DamageAPI.multiplyAllDamage(bucket, 2.0);             // 全部 * 2.0
```

### 2.3 抗性计算

```java
// Java
DamageBucket bucket = DamageAPI.createMixedBucket(damages);

// 应用抗性（返回新桶，不修改原桶）
DamageBucket reduced = DamageAPI.applyResistances(bucket, victim);

// 应用抗性（原地修改）
DamageAPI.applyResistancesInPlace(bucket, victim);

// 手动计算减伤
double resistance = 50.0;
double reduction = DamageAPI.calculateDamageReduction(resistance);  // 0.333...
double effective = DamageAPI.calculateEffectiveDamage(100.0, resistance);  // 66.67
```

### 2.4 桶操作

```java
// Java
// 合并多个桶
DamageBucket merged = DamageAPI.mergeBuckets(bucket1, bucket2, bucket3);

// 克隆
DamageBucket cloned = DamageAPI.cloneBucket(bucket);

// 检查元素
boolean hasFire = DamageAPI.hasElement(bucket, Element.FIRE);
boolean hasElemental = DamageAPI.hasElementalDamage(bucket);
Set<Element> elements = DamageAPI.getElements(bucket);

// 遍历
DamageAPI.forEachDamage(bucket, entry -> {
    Element element = entry.getKey();
    double damage = entry.getValue();
    System.out.println(element + ": " + damage);
});
```

---

## 3. AttributeAPI - 属性注册查询 API

### 3.1 属性列表

```java
// Java
import com.attributecore.api.AttributeAPI;

// 获取所有属性
List<SubAttribute> all = AttributeAPI.getAll();
List<String> names = AttributeAPI.getAllNames();

// 按名称获取
SubAttribute attr = AttributeAPI.getByName("attack_damage");

// 按类型获取
List<SubAttribute> attackAttrs = AttributeAPI.getAttackAttributes();
List<SubAttribute> defenseAttrs = AttributeAPI.getDefenceAttributes();
List<SubAttribute> byType = AttributeAPI.getByType(AttributeType.Attack);
```

### 3.2 JS 属性查询

```java
// Java
// 获取所有 JS 属性
List<JsAttribute> jsAttrs = AttributeAPI.getJsAttributes();
JsAttribute lifesteal = AttributeAPI.getJsAttribute("life_steal");

// 按元素获取
List<JsAttribute> fireAttrs = AttributeAPI.getJsAttributesByElement(Element.FIRE);
List<JsAttribute> iceAttrs = AttributeAPI.getJsAttributesByElement("ICE");

// 按类型获取
List<JsAttribute> jsAttack = AttributeAPI.getJsAttributesByType(AttributeType.Attack);
```

### 3.3 属性元数据

```java
// Java
// NBT 名称（中文）
String nbtName = AttributeAPI.getNbtName("attack_damage");  // "攻击力"

// Placeholder 后缀
String placeholder = AttributeAPI.getPlaceholder("crit_chance");  // "crit_chance"

// 优先级、战斗力权重
int priority = AttributeAPI.getPriority("attack_damage");
double cpWeight = AttributeAPI.getCombatPowerWeight("attack_damage");

// 属性类型
List<AttributeType> types = AttributeAPI.getTypes("life_steal");

// 元素（仅 JS 属性）
Element element = AttributeAPI.getElement("fire_damage");  // Element.FIRE

// 检查存在
boolean exists = AttributeAPI.exists("attack_damage");
```

### 3.4 映射表

```java
// Java
// NBT 名称 -> 属性名称
Map<String, String> nbtMapping = AttributeAPI.getNbtNameMapping();
String attrName = AttributeAPI.getAttributeNameFromNbt("雷元素伤害");

// 属性名称 -> Placeholder
Map<String, String> placeholders = AttributeAPI.getAllPlaceholders();

// 重新加载 JS 属性
AttributeAPI.reload();
```

---

## 4. ElementAPI - 元素系统 API

### 4.1 元素光环管理

```java
// Java
import com.attributecore.api.ElementAPI;
import com.attributecore.data.Element;

// 应用光环
ElementAPI.applyAura(entity, Element.FIRE, 1.0);
ElementAPI.applyAura(entity, "WATER", 2.0);

// 获取光环
AuraInstance aura = ElementAPI.getAura(entity);
List<AuraInstance> auras = ElementAPI.getAuras(entity);

// 检查光环
boolean hasFire = ElementAPI.hasAura(entity, Element.FIRE);
boolean hasIce = ElementAPI.hasAura(entity, "ICE");

// 消耗光环
boolean consumed = ElementAPI.consumeAura(entity, Element.FIRE, 1.0);

// 清除光环
ElementAPI.clearAura(entity, Element.WATER);  // 清除水元素
ElementAPI.clearAura(entity, (Element) null);  // 清除所有
```

### 4.2 元素查询

```java
// Java
// 获取元素
Element fire = ElementAPI.getElement("FIRE");

// 所有元素
List<Element> all = ElementAPI.getElements();

// 可反应元素（不含 PHYSICAL）
List<Element> reactive = ElementAPI.getReactiveElements();

// 统计
int auraCount = ElementAPI.getActiveAuraCount();
int entityCount = ElementAPI.getAffectedEntityCount();
```

### 4.3 触发元素反应

```java
// Java
boolean triggered = ElementAPI.triggerReaction(
    attacker,
    victim,
    Element.FIRE,
    (existingElement, triggerElement) -> {
        // 反应触发回调
        attacker.sendMessage("触发了 " + existingElement + " + " + triggerElement + " 反应!");
    }
);
```

---

## 完整示例：技能伤害系统

```java
import com.attributecore.api.*;
import com.attributecore.data.*;

public class SkillSystem {
    
    // 火球术 - 火元素技能伤害
    public void castFireball(Player caster, LivingEntity target) {
        // 1. 创建伤害桶
        double baseDamage = AttributeCoreAPI.getAttribute(caster, "attack_damage");
        double fireDamage = AttributeCoreAPI.getAttribute(caster, "fire_damage");
        
        DamageBucket bucket = DamageAPI.createMixedBucket(Map.of(
            "PHYSICAL", baseDamage * 0.5,
            "FIRE", fireDamage * 2.0
        ));
        
        // 2. 应用抗性
        DamageBucket reduced = DamageAPI.applyResistances(bucket, target);
        
        // 3. 造成伤害
        double finalDamage = DamageAPI.getTotalDamage(reduced);
        target.damage(finalDamage, caster);
        
        // 4. 应用火元素光环
        ElementAPI.applyAura(target, Element.FIRE, 1.0);
        
        // 5. 触发元素反应（如果目标已有其他元素）
        AuraInstance existingAura = ElementAPI.getAura(target);
        if (existingAura != null && existingAura.getElement() != Element.FIRE) {
            ElementAPI.triggerReaction(caster, target, Element.FIRE, (aura, trigger) -> {
                caster.sendMessage("§6元素反应触发!");
            });
        }
    }
    
    // BUFF 技能 - 增加攻击力
    public void castAttackBuff(Player caster, int durationSeconds) {
        double buffAmount = AttributeCoreAPI.getAttribute(caster, "attack_damage") * 0.5;
        
        AttributeCoreAPI.addPluginAttribute("SkillSystem", caster.getUniqueId(), "attack_damage", buffAmount);
        AttributeCoreAPI.updateEntityData(caster);
        
        caster.sendMessage("§a攻击力增加 " + (int) buffAmount + " 点，持续 " + durationSeconds + " 秒!");
        
        // 定时移除
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            AttributeCoreAPI.addPluginAttribute("SkillSystem", caster.getUniqueId(), "attack_damage", -buffAmount);
            AttributeCoreAPI.updateEntityData(caster);
            caster.sendMessage("§7攻击力增益已结束");
        }, durationSeconds * 20L);
    }
}
```

---

## 常见问题 FAQ

### Q1: 修改属性后何时生效？
**A:** 调用 `setPluginAttribute` 或 `addPluginAttribute` 后，必须手动调用 `updateEntityData(entity)` 才会刷新属性。

### Q2: 如何避免属性冲突？
**A:** 使用唯一的 `pluginId` 标识符（建议用插件名），AttributeCore 会自动隔离不同来源的数据。

### Q3: 如何在插件卸载时清理数据？
**A:** 在 `onDisable()` 中调用：
```java
AttributeCoreAPI.clearPluginAllData("YourPluginName");
```

### Q4: 抗性公式是什么？
**A:** `伤害减免 = 抗性 / (抗性 + 100)`  
例如：50 抗性 → `50 / 150 = 33.3%` 减伤

### Q5: 元素光环持续多久？
**A:** 默认 5 秒，可通过 `gauge` 参数调整。

---

## API 速查表

| 方法 | 功能 | 返回值 |
|------|------|--------|
| `AttributeCoreAPI.getAttribute(entity, key)` | 获取属性固定值 | `Double` |
| `AttributeCoreAPI.getAttributeFinal(entity, key)` | 获取属性最终值 | `Double` |
| `AttributeCoreAPI.setPluginAttribute(id, uuid, key, value)` | 设置插件属性 | `void` |
| `AttributeCoreAPI.addPluginAttribute(id, uuid, key, value)` | 叠加插件属性 | `void` |
| `AttributeCoreAPI.removePluginAttribute(id, uuid)` | 移除插件属性 | `AttributeData?` |
| `AttributeCoreAPI.updateEntityData(entity)` | 刷新实体属性 | `void` |
| `AttributeCoreAPI.getCombatPower(entity)` | 获取战斗力 | `Double` |
| `DamageAPI.createPhysicalBucket(damage)` | 创建物理伤害桶 | `DamageBucket` |
| `DamageAPI.createElementalBucket(element, damage)` | 创建元素伤害桶 | `DamageBucket` |
| `DamageAPI.applyResistances(bucket, victim)` | 应用抗性 | `DamageBucket` |
| `AttributeAPI.getAll()` | 获取所有属性 | `List<SubAttribute>` |
| `AttributeAPI.getJsAttributes()` | 获取 JS 属性 | `List<JsAttribute>` |
| `ElementAPI.applyAura(entity, element, gauge)` | 应用元素光环 | `void` |
| `ElementAPI.getAura(entity)` | 获取元素光环 | `AuraInstance?` |
| `ElementAPI.triggerReaction(...)` | 触发元素反应 | `Boolean` |

---

## 相关文档

- [开发者 API 详解](DEVELOPER_API.md) - 完整 API 签名和进阶用法
- [脚本开发指南](SCRIPT_GUIDE.md) - JavaScript 自定义属性开发
