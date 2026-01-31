/**
 * 荆棘属性 (Thorns)
 * 受到攻击时反弹部分伤害给攻击者
 * 
 * Lore 格式: §5荆棘 §f20%
 * 效果: 受到攻击时将 伤害值 * 荆棘% 的伤害反弹给攻击者
 */

// ==================== 属性配置 ====================
var priority = 15;
var combatPower = 0.6;
var attributeName = "thorns";
var attributeType = "Defence";
var placeholder = "thorns";
var pattern = "荆棘";
var patternSuffix = "%";

// ==================== 生命周期函数 ====================
function onLoad(attr) {
    return attr;
}

// ==================== 触发函数 ====================
function runAttack(attr, attacker, entity, handle) {
    return true;
}

function runDefense(attr, entity, killer, handle) {
    if (killer == null) return true;
    
    var thornsPercent = attr.getRandomValue(entity, handle);
    if (thornsPercent <= 0) return true;
    
    var damage = handle.getDamage();
    var reflectDamage = damage * (thornsPercent / 100.0);
    
    if (reflectDamage > 0) {
        killer.damage(reflectDamage);
    }
    
    return true;
}

function runKiller(attr, killer, entity, handle) {
    return true;
}

function run(attr, entity, handle) {
    return true;
}

function runCustom(attr, caster, target, params, source, handle) {
    return true;
}

// ==================== 占位符 ====================
function getPlaceholder(attr, attributeData, player, identifier) {
    if (identifier === "thorns") {
        return attributeData.get("thorns");
    }
    return null;
}

function getPlaceholders() {
    return ["thorns"];
}
