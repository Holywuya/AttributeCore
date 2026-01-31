var priority = 31;
var combatPower = 1.0;
var attributeName = "water_damage";
var attributeType = "Attack";
var pattern = "水元素伤害";
var patternSuffix = "";

function runAttack(attr, attacker, entity, handle) {
    var waterDamage = attr.getRandomValue(attacker, handle);
    if (waterDamage <= 0) return true;
    
    var currentDamage = handle.getDamage();
    handle.setDamage(currentDamage + waterDamage);
    
    return true;
}
