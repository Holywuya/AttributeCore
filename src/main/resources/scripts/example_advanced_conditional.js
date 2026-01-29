/**
 * 高级示例：条件暴击
 * 类型：攻击型
 * 
 * 功能：当目标生命值低于30%时，暴击率翻倍
 */

function getSettings() {
    return {
        key: "execute_crit",
        names: ["斩杀暴击", "Execute Crit"],
        displayName: "&4&l斩杀暴击",
        type: "ATTACK",
        priority: 4,
        combatPower: 3.0
    };
}

function runAttack(attr, attacker, entity, handle) {
    var target = entity.getBukkitEntity();
    var healthPercent = target.getHealth() / target.getAttribute(
        org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH
    ).getValue();
    
    var critChance = handle.getValue();
    if (healthPercent < 0.3) {
        critChance *= 2;
        attacker.sendActionBar("&c&l[斩杀] &e暴击率翻倍!");
    }
    
    handle.rollCrit(critChance);
}
