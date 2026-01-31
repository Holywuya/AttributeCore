var priority = 34;
var combatPower = 1.0;
var attributeName = "wind_damage";
var attributeType = "Attack";
var pattern = "风元素伤害";
var patternSuffix = "";

function runAttack(attr, attacker, entity, handle) {
    var windDamage = attr.getRandomValue(attacker, handle);
    if (windDamage <= 0) return true;
    
    var currentDamage = handle.getDamage();
    handle.setDamage(currentDamage + windDamage);
    
    return true;
}
