/**
 * 示例属性：生命恢复
 * 类型：被动型
 * 
 * 功能：每秒恢复生命值
 */

function getSettings() {
    return {
        key: "health_regen",
        names: ["生命恢复", "回血", "Health Regen"],
        displayName: "&a生命恢复",
        type: "PASSIVE",
        priority: 0,
        combatPower: 0.8
    };
}

function runUpdate(attr, entity, value, handle) {
    var player = entity.getBukkitEntity();
    var currentHealth = player.getHealth();
    var maxHealth = player.getAttribute(
        org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH
    ).getValue();
    
    var newHealth = Math.min(currentHealth + value, maxHealth);
    player.setHealth(newHealth);
}
