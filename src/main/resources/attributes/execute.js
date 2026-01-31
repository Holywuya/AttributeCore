var priority = 20;
var combatPower = 0.5;
var attributeName = "execute";
var attributeType = "Attack";
var pattern = "处决";
var patternSuffix = "%";

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
