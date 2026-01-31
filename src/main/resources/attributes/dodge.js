var priority = 5;
var combatPower = 1.2;
var attributeName = "dodge";
var attributeType = "Defence";
var pattern = "闪避";
var patternSuffix = "%";

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
