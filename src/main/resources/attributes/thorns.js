var priority = 15;
var combatPower = 0.6;
var attributeName = "thorns";
var attributeType = "Defence";
var pattern = "荆棘";
var patternSuffix = "%";

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
