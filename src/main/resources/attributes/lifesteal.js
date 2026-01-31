var priority = 10;
var combatPower = 0.8;
var attributeName = "life_steal";
var attributeType = "Attack";
var pattern = "吸血";
var patternSuffix = "%";

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
