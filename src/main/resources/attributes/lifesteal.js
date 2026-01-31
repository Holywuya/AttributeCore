/**
 * 吸血属性 (Life Steal)
 * 攻击时按百分比回复生命值
 * 
 * Lore 格式: §c吸血 §f10%
 * 效果: 造成伤害时回复 伤害值 * 吸血% 的生命
 */

// ==================== 属性配置 ====================
var priority = 10;
var combatPower = 0.8;
var attributeName = "life_steal";
var attributeType = "Attack";
var placeholder = "life_steal";
var pattern = "吸血";
var patternSuffix = "%";

// ==================== 生命周期函数 ====================
function onLoad(attr) {
    return attr;
}

// ==================== 触发函数 ====================
function runAttack(attr, attacker, entity, handle) {
    var percent = attr.getRandomValue(attacker, handle);
    if (percent <= 0) return true;
    
    var damage = handle.getDamage();
    var healAmount = damage * (percent / 100.0);
    
    if (healAmount > 0) {
        attr.heal(attacker, healAmount, handle);
    }
    
    return true;
}

function runDefense(attr, entity, killer, handle) {
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
    if (identifier === "life_steal") {
        return attributeData.get("life_steal");
    }
    return null;
}

function getPlaceholders() {
    return ["life_steal"];
}
