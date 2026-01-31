var priority = 32;
var combatPower = 1.0;
var attributeName = "ice_damage";
var attributeType = "Attack";
var pattern = "冰元素伤害";
var patternSuffix = "";

function runAttack(attr, attacker, entity, handle) {
    var iceDamage = attr.getRandomValue(attacker, handle);
    if (iceDamage <= 0) return true;
    
    var currentDamage = handle.getDamage();
    handle.setDamage(currentDamage + iceDamage);
    
    return true;
}
