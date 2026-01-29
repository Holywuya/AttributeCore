# JavaScript 脚本开发指南

AttributeCore 使用 JavaScript 脚本定义属性行为，采用 **AttributePlus 兼容风格**。

## 脚本结构

每个脚本文件需要包含两个函数：

```javascript
// 1. 配置函数 - 定义属性元数据
function getSettings() {
    return {
        key: "attack",                    // 属性唯一标识
        names: ["物理攻击", "攻击力"],      // Lore 匹配名称
        displayName: "&c物理攻击",          // 显示名称（支持颜色代码）
        type: "ATTACK",                   // 属性类型
        priority: 10,                     // 执行优先级（数字越小越先执行）
        combatPower: 1.0,                 // 战斗力权重
        element: "FIRE"                   // 可选：元素类型
    };
}

// 2. 行为函数 - 定义属性逻辑
function runAttack(attr, attacker, entity, handle) {
    attacker.addDamage(handle.getValue());
}
```

## 属性类型

| 类型 | 说明 | 触发函数 |
|------|------|----------|
| `ATTACK` | 攻击型属性 | `runAttack(attr, attacker, entity, handle)` |
| `DEFEND` | 防御型属性 | `runDefend(attr, attacker, entity, handle)` |
| `PASSIVE` | 被动型属性 | `runUpdate(attr, entity, value, handle)` |
| `UPDATE` | 更新型属性 | `runUpdate(attr, entity, value, handle)` |

## API 参考 (AttributePlus 风格)

### attacker / entity 对象

脚本中的 `attacker` 和 `entity` 是 `ScriptEntity` 包装器，提供以下方法：

#### 伤害操作
```javascript
attacker.addDamage(100);              // 增加物理伤害
attacker.takeDamage(50);              // 减少伤害
attacker.getDamage();                 // 获取当前伤害
attacker.setDamage(200);              // 设置伤害
attacker.setMultiplier(1.5);          // 设置伤害倍率

attacker.addElementalDamage("FIRE", 50);  // 增加元素伤害
attacker.addBucketDamage("MAGIC", 30);    // 增加指定类型伤害
```

#### 暴击系统
```javascript
attacker.rollCrit(50);                // 以 50% 几率触发暴击
attacker.setCritTier(2);              // 设置暴击层级
attacker.addCritDamage(50);           // 增加暴击伤害百分比
attacker.addCritResistance(20);       // 增加暴击抗性
```

#### 防御系统
```javascript
entity.addDefenseScore(100);          // 增加护甲值
entity.addPhysicalDefense(50);        // 增加物理防御
entity.addMagicalDefense(50);         // 增加魔法防御
attacker.addFixedPenetration(30);     // 固定穿透
attacker.addPercentPenetration(10);   // 百分比穿透
```

#### 伤害减免
```javascript
entity.addUniversalReduction(10);     // 增加 10% 通用伤害减免
entity.addUniversalFlatReduction(50); // 增加 50 点固定减免
entity.addBucketResistance("FIRE", 20); // 增加火焰抗性
```

#### 标签系统
```javascript
attacker.addTag("FIRE");              // 添加标签
attacker.hasTag("FIRE");              // 检查标签
attacker.removeTag("FIRE");           // 移除标签
```

#### 护盾与生命
```javascript
entity.heal(20);                      // 治疗
entity.addShield(100);                // 增加护盾
entity.getShield();                   // 获取护盾值
```

#### 属性读取
```javascript
attacker.getRandomValue("attack");    // 获取随机属性值
attacker.getAttrMin("attack");        // 获取属性最小值
attacker.getAttrMax("attack");        // 获取属性最大值
attacker.getCP();                     // 获取战斗力
```

#### 视觉反馈
```javascript
attacker.tell("&c你造成了暴击！");     // 发送消息
attacker.actionbar("&e暴击！");        // 发送 ActionBar
attacker.sound("ENTITY_PLAYER_LEVELUP"); // 播放音效
```

#### 原生属性
```javascript
attacker.getName();                   // 获取名称
attacker.getHealth();                 // 获取当前生命
attacker.getMaxHealth();              // 获取最大生命
attacker.getBukkit();                 // 获取原生 Bukkit 实体
```

### handle 对象

`handle` 提供属性值获取：

```javascript
handle.getValue();                    // 获取当前属性值（已 Roll 点）
handle.getFinalDamage();              // 获取最终伤害（结算后）
handle.getDamageBuckets();            // 获取所有伤害桶
```

## 示例脚本

### 物理攻击
```javascript
function getSettings() {
    return {
        key: "attack",
        names: ["物理攻击", "攻击力"],
        displayName: "&c物理攻击",
        type: "ATTACK",
        priority: 10,
        combatPower: 1.0
    };
}

function runAttack(attr, attacker, entity, handle) {
    attacker.addDamage(handle.getValue());
}
```

### 暴击率
```javascript
function getSettings() {
    return {
        key: "crit_chance",
        names: ["暴击率", "暴击"],
        displayName: "&e暴击率",
        type: "ATTACK",
        priority: 5,
        combatPower: 1.5
    };
}

function runAttack(attr, attacker, entity, handle) {
    attacker.rollCrit(handle.getValue());
}
```

### 护甲值
```javascript
function getSettings() {
    return {
        key: "defense",
        names: ["护甲", "防御力"],
        displayName: "&9护甲",
        type: "DEFEND",
        priority: 10,
        combatPower: 1.0
    };
}

function runDefend(attr, attacker, entity, handle) {
    entity.addDefenseScore(handle.getValue());
}
```

### 吸血
```javascript
function getSettings() {
    return {
        key: "lifesteal",
        names: ["吸血", "生命窃取"],
        displayName: "&d吸血",
        type: "ATTACK",
        priority: 100,
        combatPower: 2.5
    };
}

function runAttack(attr, attacker, entity, handle) {
    var finalDamage = attacker.getDamage();
    var healAmount = finalDamage * (handle.getValue() / 100.0);
    attacker.heal(healAmount);
}
```

### 条件暴击（斩杀）
```javascript
function getSettings() {
    return {
        key: "execute_crit",
        names: ["斩杀暴击"],
        displayName: "&4斩杀暴击",
        type: "ATTACK",
        priority: 4,
        combatPower: 3.0
    };
}

function runAttack(attr, attacker, entity, handle) {
    var healthPercent = entity.getHealth() / entity.getMaxHealth();
    var critChance = handle.getValue();
    
    if (healthPercent < 0.3) {
        critChance *= 2;
        attacker.actionbar("&c[斩杀] 暴击率翻倍!");
    }
    
    attacker.rollCrit(critChance);
}
```

## 脚本加载

脚本文件放置在 `plugins/AttributeCore/scripts/` 目录下，以 `.js` 为后缀。

重载命令：`/ac reload`

## 调试

在 `config.yml` 中启用调试模式：

```yaml
debug: true
```

控制台将输出属性注册和伤害计算的详细日志。
