var priority = 30;
var combatPower = 1.0;
var attributeName = "fire_damage";
var attributeType = "Attack";
var pattern = "火元素伤害";
var patternSuffix = "";

function runAttack(attr, attacker, entity, handle) {
    var fireDamage = attr.getRandomValue(attacker, handle);
    if (fireDamage <= 0) return true;
    
    var currentDamage = handle.getDamage();
    handle.setDamage(currentDamage + fireDamage);
    
    return true;
}
