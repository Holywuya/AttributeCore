/**
 * 闪避属性 (Dodge)
 * 受到攻击时有概率完全闪避伤害
 * 
 * Lore 格式: §a闪避 §f15%
 * 效果: 受到攻击时有 闪避% 的概率完全规避伤害
 */

// ==================== 属性配置 ====================
var priority = 5;
var combatPower = 1.2;
var attributeName = "dodge";
var attributeType = "Defence";
var placeholder = "dodge";
var pattern = "闪避";
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
    var dodgeChance = attr.getRandomValue(entity, handle);
    if (dodgeChance <= 0) return true;
    
    if (attr.chance(dodgeChance)) {
        handle.setDamage(0);
        attr.setCancelled(true, handle);
        
        handle.sendActionBar(entity, "§a闪避!");
        return false;
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
    if (identifier === "dodge") {
        return attributeData.get("dodge");
    }
    return null;
}

function getPlaceholders() {
    return ["dodge"];
}
