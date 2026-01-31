/**
 * 处决属性 (Execute)
 * 当目标生命值低于阈值时造成额外伤害
 * 
 * Lore 格式: §4处决 §f30%
 * 效果: 当目标生命值低于 处决% 时，造成双倍伤害
 */

// ==================== 属性配置 ====================
var priority = 20;
var combatPower = 0.5;
var attributeName = "execute";
var attributeType = "Attack";
var placeholder = "execute";
var pattern = "处决";
var patternSuffix = "%";

// ==================== 生命周期函数 ====================
function onLoad(attr) {
    return attr;
}

// ==================== 触发函数 ====================
function runAttack(attr, attacker, entity, handle) {
    var threshold = attr.getRandomValue(attacker, handle);
    if (threshold <= 0) return true;
    
    var healthPercent = (entity.getHealth() / entity.getMaxHealth()) * 100;
    
    if (healthPercent <= threshold) {
        var currentDamage = handle.getDamage();
        handle.setDamage(currentDamage * 2.0);
        
        handle.sendActionBar(attacker, "§4处决!");
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
    if (identifier === "execute") {
        return attributeData.get("execute");
    }
    return null;
}

function getPlaceholders() {
    return ["execute"];
}
