# AttributeCore 开发者 API 详解

本文档提供 AttributeCore 所有 API 的完整签名、参数说明和进阶用法。

**文档版本**: v1.6.3.0  
**最后更新**: 2026-01-31

---

## 目录

1. [AttributeCoreAPI - 主 API](#1-attributecoreapi---主-api)
2. [DamageAPI - 伤害系统 API](#2-damageapi---伤害系统-api)
3. [AttributeAPI - 属性注册查询 API](#3-attributeapi---属性注册查询-api)
4. [ElementAPI - 元素系统 API](#4-elementapi---元素系统-api)
5. [数据结构](#5-数据结构)

---

## 1. AttributeCoreAPI - 主 API

**包路径**: `com.attributecore.api.AttributeCoreAPI`

### 1.1 实体属性查询

| 方法签名 | 描述 |
|----------|------|
| `getEntityData(entity: LivingEntity): AttributeData` | 获取实体完整属性数据（基础 + API 来源） |
| `getBaseEntityData(entity: LivingEntity): AttributeData` | 获取实体基础属性（不含 API 来源） |
| `getAttribute(entity: LivingEntity, key: String): Double` | 获取属性固定值 |
| `getAttributePercent(entity: LivingEntity, key: String): Double` | 获取属性百分比加成 |
| `getAttributeFinal(entity: LivingEntity, key: String): Double` | 获取属性最终值 = 固定值 * (1 + 百分比/100) |
| `getAttributesBatch(entity: LivingEntity, keys: List<String>): Map<String, Double>` | 批量获取多个属性的最终值 |
| `getAllNonZeroAttributes(entity: LivingEntity): Map<String, Double>` | 获取所有非零属性 |
| `getAllAttributesFinal(entity: LivingEntity): Map<String, Double>` | 获取所有属性的最终值 |

### 1.2 插件属性管理

| 方法签名 | 描述 |
|----------|------|
| `getPluginAttributeData(uuid: UUID): AttributeData` | 获取某实体所有 API 来源的属性数据 |
| `setPluginAttribute(pluginId: String, uuid: UUID, data: AttributeData)` | 设置插件为实体提供的属性数据 |
| `setPluginAttribute(pluginId: String, uuid: UUID, key: String, value: Double)` | 设置单个属性值 |
| `setPluginAttributePercent(pluginId: String, uuid: UUID, key: String, value: Double)` | 设置单个属性百分比值 |
| `addPluginAttribute(pluginId: String, uuid: UUID, key: String, value: Double)` | 叠加属性值 |
| `addPluginAttributePercent(pluginId: String, uuid: UUID, key: String, value: Double)` | 叠加属性百分比值 |
| `removePluginAttribute(pluginId: String, uuid: UUID): AttributeData?` | 移除插件为实体提供的属性数据 |
| `hasPluginAttribute(pluginId: String, uuid: UUID): Boolean` | 检查插件是否为实体设置了属性 |
| `clearEntityPluginData(uuid: UUID)` | 清空实体的所有 API 来源属性 |
| `clearPluginAllData(pluginId: String)` | 清空插件为所有实体设置的属性 |

### 1.3 实体属性刷新

| 方法签名 | 描述 |
|----------|------|
| `updateEntityData(entity: LivingEntity)` | 刷新实体的属性缓存 |
| `clearEntityCache(entity: LivingEntity)` | 清除实体的属性缓存 |
| `clearEntityCache(uuid: UUID)` | 清除实体的属性缓存（通过 UUID） |

### 1.4 物品属性解析

| 方法签名 | 描述 |
|----------|------|
| `loadItemData(item: ItemStack?): AttributeData` | 读取单个物品的属性数据 |
| `loadItemData(items: List<ItemStack?>): AttributeData` | 批量读取多个物品的属性数据（合并） |
| `parseAttributesFromLore(lore: String): AttributeData` | 从单行 Lore 解析属性 |
| `parseAttributesFromLore(lores: List<String>): AttributeData` | 从 Lore 列表解析属性 |

### 1.5 伤害系统

| 方法签名 | 描述 |
|----------|------|
| `buildDamageBucket(entity: LivingEntity): DamageBucket` | 构建实体的伤害桶（基于攻击类属性） |
| `getResistances(entity: LivingEntity): Map<Element, Double>` | 获取实体的所有元素抗性 |
| `getResistance(entity: LivingEntity, element: Element): Double` | 获取某个元素的抗性 |
| `getResistance(entity: LivingEntity, elementName: String): Double` | 获取某个元素的抗性（通过名称） |
| `calculateFinalDamage(attacker: LivingEntity, victim: LivingEntity, baseDamage: Double): Double` | 计算从攻击者到受害者的最终伤害 |
| `calculateFinalDamage(bucket: DamageBucket, victim: LivingEntity): Double` | 计算伤害桶应用抗性后的最终伤害 |

### 1.6 战斗力与属性查询

| 方法签名 | 描述 |
|----------|------|
| `getCombatPower(entity: LivingEntity): Double` | 获取实体的战斗力 |
| `getRegisteredAttributeNames(): List<String>` | 获取所有已注册属性的名称列表 |
| `getJsAttributeNames(): List<String>` | 获取所有 JS 属性的名称列表 |
| `isAttributeRegistered(name: String): Boolean` | 检查属性是否已注册 |
| `getAttributeNbtName(name: String): String?` | 获取属性的 NBT 名称（中文 pattern） |
| `getAttributeCombatPowerWeight(name: String): Double` | 获取属性的战斗力权重 |

### 1.7 回调

| 方法签名 | 描述 |
|----------|------|
| `onAttributeChange(entity: LivingEntity, callback: Consumer<AttributeData>)` | 触发属性变更回调 |

---

## 2. DamageAPI - 伤害系统 API

**包路径**: `com.attributecore.api.DamageAPI`

### 2.1 伤害桶创建

| 方法签名 | 描述 |
|----------|------|
| `createPhysicalBucket(damage: Double): DamageBucket` | 创建物理伤害桶 |
| `createElementalBucket(element: Element, damage: Double): DamageBucket` | 创建元素伤害桶 |
| `createElementalBucket(elementName: String, damage: Double): DamageBucket` | 创建元素伤害桶（通过名称） |
| `createMixedBucket(damages: Map<String, Double>): DamageBucket` | 创建混合伤害桶 |

### 2.2 伤害操作

| 方法签名 | 描述 |
|----------|------|
| `addDamage(bucket: DamageBucket, element: Element, value: Double)` | 添加伤害到指定元素 |
| `addDamage(bucket: DamageBucket, elementName: String, value: Double)` | 添加伤害（通过名称） |
| `setDamage(bucket: DamageBucket, element: Element, value: Double)` | 设置指定元素的伤害 |
| `setDamage(bucket: DamageBucket, elementName: String, value: Double)` | 设置伤害（通过名称） |
| `getDamage(bucket: DamageBucket, element: Element): Double` | 获取指定元素的伤害 |
| `getDamage(bucket: DamageBucket, elementName: String): Double` | 获取伤害（通过名称） |
| `getTotalDamage(bucket: DamageBucket): Double` | 获取总伤害 |
| `getElementalDamage(bucket: DamageBucket): Double` | 获取元素伤害总和（不含物理） |
| `getPhysicalDamage(bucket: DamageBucket): Double` | 获取物理伤害 |
| `multiplyDamage(bucket: DamageBucket, element: Element, multiplier: Double)` | 乘算指定元素的伤害 |
| `multiplyAllDamage(bucket: DamageBucket, multiplier: Double)` | 乘算所有伤害 |

### 2.3 抗性计算

| 方法签名 | 描述 |
|----------|------|
| `applyResistances(bucket: DamageBucket, victim: LivingEntity): DamageBucket` | 应用抗性（返回新桶） |
| `applyResistancesInPlace(bucket: DamageBucket, victim: LivingEntity)` | 应用抗性（原地修改） |
| `applyResistances(bucket: DamageBucket, resistances: Map<Element, Double>): DamageBucket` | 应用自定义抗性 |
| `calculateDamageReduction(resistance: Double, baseValue: Double = 100.0): Double` | 计算伤害减免比例 |
| `calculateEffectiveDamage(damage: Double, resistance: Double, baseValue: Double = 100.0): Double` | 计算有效伤害 |

**抗性公式**: `伤害减免 = 抗性 / (抗性 + baseValue)`

### 2.4 桶操作

| 方法签名 | 描述 |
|----------|------|
| `mergeBuckets(vararg buckets: DamageBucket): DamageBucket` | 合并多个伤害桶 |
| `cloneBucket(bucket: DamageBucket): DamageBucket` | 克隆伤害桶 |
| `hasElement(bucket: DamageBucket, element: Element): Boolean` | 检查是否包含指定元素 |
| `hasElementalDamage(bucket: DamageBucket): Boolean` | 检查是否有元素伤害 |
| `getElements(bucket: DamageBucket): Set<Element>` | 获取所有包含的元素 |
| `forEachDamage(bucket: DamageBucket, consumer: Consumer<Map.Entry<Element, Double>>)` | 遍历所有伤害 |

---

## 3. AttributeAPI - 属性注册查询 API

**包路径**: `com.attributecore.api.AttributeAPI`

### 3.1 属性列表

| 方法签名 | 描述 |
|----------|------|
| `getAll(): List<SubAttribute>` | 获取所有已注册属性 |
| `getAllNames(): List<String>` | 获取所有属性名称 |
| `getByName(name: String): SubAttribute?` | 按名称获取属性 |
| `getByType(type: AttributeType): List<SubAttribute>` | 按类型获取属性 |
| `getByType(typeName: String): List<SubAttribute>` | 按类型名称获取属性 |
| `getAttackAttributes(): List<SubAttribute>` | 获取所有攻击类属性 |
| `getDefenceAttributes(): List<SubAttribute>` | 获取所有防御类属性 |
| `exists(name: String): Boolean` | 检查属性是否存在 |
| `getAttributeCount(): Int` | 获取属性总数 |

### 3.2 JS 属性查询

| 方法签名 | 描述 |
|----------|------|
| `getJsAttributes(): List<JsAttribute>` | 获取所有 JS 属性 |
| `getJsAttribute(name: String): JsAttribute?` | 获取指定 JS 属性 |
| `getJsAttributesByType(type: AttributeType): List<JsAttribute>` | 按类型获取 JS 属性 |
| `getJsAttributesByElement(element: Element): List<JsAttribute>` | 按元素获取 JS 属性 |
| `getJsAttributesByElement(elementName: String): List<JsAttribute>` | 按元素名称获取 JS 属性 |
| `getJsAttributeCount(): Int` | 获取 JS 属性数量 |

### 3.3 属性元数据

| 方法签名 | 描述 |
|----------|------|
| `getNbtName(attributeName: String): String?` | 获取 NBT 名称 |
| `getPlaceholder(attributeName: String): String?` | 获取 Placeholder 后缀 |
| `getPriority(attributeName: String): Int` | 获取优先级（默认 100） |
| `getCombatPowerWeight(attributeName: String): Double` | 获取战斗力权重（默认 1.0） |
| `getTypes(attributeName: String): List<AttributeType>` | 获取属性类型列表 |
| `getElement(attributeName: String): Element?` | 获取属性元素（仅 JS 属性） |

### 3.4 映射表

| 方法签名 | 描述 |
|----------|------|
| `getNbtNameMapping(): Map<String, String>` | 获取 NBT 名称 -> 属性名称映射 |
| `getAttributeNameFromNbt(nbtName: String): String?` | 从 NBT 名称获取属性名称 |
| `getAllPlaceholders(): Map<String, String>` | 获取属性名称 -> Placeholder 映射 |
| `reload()` | 重新加载 JS 属性 |

---

## 4. ElementAPI - 元素系统 API

**包路径**: `com.attributecore.api.ElementAPI`

### 4.1 元素光环管理

| 方法签名 | 描述 |
|----------|------|
| `applyAura(entity: LivingEntity, element: Element, gauge: Double = 1.0)` | 应用元素光环 |
| `applyAura(entity: LivingEntity, elementName: String, gauge: Double = 1.0)` | 应用元素光环（通过名称） |
| `getAura(entity: LivingEntity): AuraInstance?` | 获取实体的元素光环 |
| `getAuras(entity: LivingEntity): List<AuraInstance>` | 获取实体的所有元素光环 |
| `hasAura(entity: LivingEntity, element: Element): Boolean` | 检查是否有指定元素光环 |
| `hasAura(entity: LivingEntity, elementName: String): Boolean` | 检查是否有光环（通过名称） |
| `consumeAura(entity: LivingEntity, element: Element, amount: Double = 1.0): Boolean` | 消耗元素光环 |
| `consumeAura(entity: LivingEntity, elementName: String, amount: Double = 1.0): Boolean` | 消耗光环（通过名称） |
| `clearAura(entity: LivingEntity, element: Element? = null)` | 清除元素光环 |
| `clearAura(entity: LivingEntity, elementName: String?)` | 清除光环（通过名称） |

### 4.2 元素查询

| 方法签名 | 描述 |
|----------|------|
| `getElement(name: String): Element?` | 获取元素枚举 |
| `getElements(): List<Element>` | 获取所有元素 |
| `getReactiveElements(): List<Element>` | 获取可反应元素（不含 PHYSICAL） |
| `getActiveAuraCount(): Int` | 获取活跃光环数量 |
| `getAffectedEntityCount(): Int` | 获取受影响实体数量 |

### 4.3 元素反应

| 方法签名 | 描述 |
|----------|------|
| `triggerReaction(attacker: LivingEntity, victim: LivingEntity, triggerElement: Element, callback: BiConsumer<Element, Element>?): Boolean` | 触发元素反应 |

---

## 5. 数据结构

### 5.1 Element 枚举

**包路径**: `com.attributecore.data.Element`

| 值 | 描述 | 抗性属性名 |
|----|------|-----------|
| `PHYSICAL` | 物理 | `physical_resistance` |
| `FIRE` | 火 | `fire_resistance` |
| `WATER` | 水 | `water_resistance` |
| `ICE` | 冰 | `ice_resistance` |
| `ELECTRO` | 雷 | `electro_resistance` |
| `WIND` | 风 | `wind_resistance` |

### 5.2 AttributeType 枚举

**包路径**: `com.attributecore.data.AttributeType`

| 值 | 描述 | 触发时机 |
|----|------|----------|
| `Attack` | 攻击类 | 攻击时 `runAttack()` |
| `Defence` | 防御类 | 受击时 `runDefense()` |
| `Update` | 更新类 | 装备变更时 `runUpdate()` |
| `Runtime` | 运行时 | 每 tick 或周期 `runRuntime()` |
| `Killer` | 击杀类 | 击杀时 `runKiller()` |
| `Custom` | 自定义 | 手动调用 |
| `Other` | 其他 | 无自动触发 |

### 5.3 DamageBucket

**包路径**: `com.attributecore.data.DamageBucket`

| 方法 | 描述 |
|------|------|
| `get(element: Element): Double` | 获取元素伤害 |
| `set(element: Element, value: Double)` | 设置元素伤害 |
| `add(element: Element, value: Double)` | 添加元素伤害 |
| `multiply(element: Element, multiplier: Double)` | 乘算元素伤害 |
| `multiplyAll(multiplier: Double)` | 乘算所有伤害 |
| `total(): Double` | 获取总伤害 |
| `elementalDamage(): Double` | 获取元素伤害总和 |
| `applyResistances(resistances: Map<Element, Double>)` | 应用抗性 |
| `clone(): DamageBucket` | 克隆 |
| `merge(other: DamageBucket)` | 合并另一个桶 |
| `hasElement(element: Element): Boolean` | 检查元素 |
| `hasElementalDamage(): Boolean` | 是否有元素伤害 |
| `elements(): Set<Element>` | 获取所有元素 |
| `toMap(): Map<Element, Double>` | 转为 Map |

### 5.4 AttributeData

**包路径**: `com.attributecore.data.AttributeData`

| 方法 | 描述 |
|------|------|
| `get(key: String): Double` | 获取固定值 |
| `set(key: String, value: Double)` | 设置固定值 |
| `add(key: String, value: Double)` | 添加固定值 |
| `getPercent(key: String): Double` | 获取百分比 |
| `setPercent(key: String, value: Double)` | 设置百分比 |
| `addPercent(key: String, value: Double)` | 添加百分比 |
| `getFinal(key: String): Double` | 获取最终值 = 固定值 * (1 + 百分比/100) |
| `getAllFinal(): Map<String, Double>` | 获取所有最终值 |
| `getNonZeroAttributes(): Map<String, Double>` | 获取非零属性 |
| `add(other: AttributeData)` | 合并另一个数据 |
| `isValid(): Boolean` | 是否有效（非空） |
| `buildDamageBucket(): DamageBucket` | 构建伤害桶 |
| `getAllResistances(): Map<Element, Double>` | 获取所有抗性 |
| `getResistance(element: Element): Double` | 获取元素抗性 |
| `calculateCombatPower(weights: Map<String, Double>): Double` | 计算战斗力 |

### 5.5 AuraInstance

**包路径**: `com.attributecore.data.AuraInstance`

| 属性/方法 | 描述 |
|-----------|------|
| `element: Element` | 元素类型 |
| `gauge: Double` | 光环量值 |
| `expireTime: Long` | 过期时间戳 |
| `isExpired(): Boolean` | 是否已过期 |
| `consume(amount: Double): Boolean` | 消耗光环量值 |

---

## 使用示例

### 创建 BUFF 系统插件

```java
import com.attributecore.api.*;
import com.attributecore.data.*;

public class BuffPlugin extends JavaPlugin {
    
    private static final String PLUGIN_ID = "BuffPlugin";
    
    @Override
    public void onDisable() {
        // 清理所有 BUFF
        AttributeCoreAPI.clearPluginAllData(PLUGIN_ID);
    }
    
    public void addAttackBuff(Player player, double amount, int seconds) {
        UUID uuid = player.getUniqueId();
        
        // 叠加攻击力
        AttributeCoreAPI.addPluginAttribute(PLUGIN_ID, uuid, "attack_damage", amount);
        AttributeCoreAPI.updateEntityData(player);
        
        player.sendMessage("§a获得 " + amount + " 攻击力 BUFF!");
        
        // 定时移除
        Bukkit.getScheduler().runTaskLater(this, () -> {
            AttributeCoreAPI.addPluginAttribute(PLUGIN_ID, uuid, "attack_damage", -amount);
            AttributeCoreAPI.updateEntityData(player);
            player.sendMessage("§7攻击力 BUFF 已结束");
        }, seconds * 20L);
    }
    
    public void clearAllBuffs(Player player) {
        AttributeCoreAPI.removePluginAttribute(PLUGIN_ID, player.getUniqueId());
        AttributeCoreAPI.updateEntityData(player);
    }
}
```

### 创建技能伤害

```java
public void castElementalSkill(Player caster, LivingEntity target, Element element, double multiplier) {
    // 获取攻击者属性
    double baseDamage = AttributeCoreAPI.getAttribute(caster, "attack_damage");
    String elementDamageKey = element.name().toLowerCase() + "_damage";
    double elementDamage = AttributeCoreAPI.getAttribute(caster, elementDamageKey);
    
    // 创建伤害桶
    DamageBucket bucket = DamageAPI.createElementalBucket(element, (baseDamage + elementDamage) * multiplier);
    
    // 应用抗性
    double finalDamage = AttributeCoreAPI.calculateFinalDamage(bucket, target);
    
    // 造成伤害
    target.damage(finalDamage, caster);
    
    // 应用元素光环
    ElementAPI.applyAura(target, element, 1.0);
    
    // 发送消息
    caster.sendMessage(String.format("§6%s 造成 %.1f 点 %s 伤害!", 
        element.getDisplayName(), finalDamage, element.getDisplayName()));
}
```

---

## 相关文档

- [API 快速入门](API_USAGE.md)
- [脚本开发指南](SCRIPT_GUIDE.md)
