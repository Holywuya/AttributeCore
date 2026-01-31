var priority = 33;
var combatPower = 1.0;
var attributeName = "electro_damage";
var attributeType = "Attack";
var pattern = "雷元素伤害";
var patternSuffix = "";

function runAttack(attr, attacker, entity, handle) {
    var electroDamage = attr.getRandomValue(attacker, handle);
    if (electroDamage <= 0) return true;
    
    var currentDamage = handle.getDamage();
    handle.setDamage(currentDamage + electroDamage);
    
    return true;
}
