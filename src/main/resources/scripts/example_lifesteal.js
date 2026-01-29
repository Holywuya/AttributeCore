/**
 * 示例属性：吸血
 * 类型：攻击型
 * 
 * 功能：造成伤害时恢复自身生命值
 */

function getSettings() {
    return {
        key: "lifesteal",
        names: ["吸血", "生命窃取", "Lifesteal"],
        displayName: "&d吸血",
        type: "ATTACK",
        priority: 100,
        combatPower: 2.5
    };
}

function runAttack(attr, attacker, entity, handle) {
    var damageData = handle.getDamageData();
    var finalDamage = damageData.getFinalDamage();
    
    var healAmount = finalDamage * (handle.getValue() / 100.0);
    
    var player = attacker.getBukkitEntity();
    var currentHealth = player.getHealth();
    var maxHealth = player.getAttribute(
        org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH
    ).getValue();
    
    var newHealth = Math.min(currentHealth + healAmount, maxHealth);
    player.setHealth(newHealth);
}
