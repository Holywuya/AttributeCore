/**
 * 高级示例：元素链式反应
 * 类型：攻击型 + 元素
 * 
 * 功能：造成雷元素伤害，并对周围敌人传递
 */

function getSettings() {
    return {
        key: "chain_lightning",
        names: ["连锁闪电", "Chain Lightning"],
        displayName: "&e&l⚡ 连锁闪电",
        type: "ATTACK",
        priority: 20,
        element: "THUNDER",
        combatPower: 2.0
    };
}

function runAttack(attr, attacker, entity, handle) {
    attacker.addElementalDamage("THUNDER", handle.getValue());
    
    var target = entity.getBukkit();
    var nearbyEntities = target.getNearbyEntities(5, 3, 5);
    
    var chainCount = 0;
    var maxChains = 3;
    
    for (var i = 0; i < nearbyEntities.size() && chainCount < maxChains; i++) {
        var nearby = nearbyEntities.get(i);
        if (nearby instanceof org.bukkit.entity.LivingEntity && 
            nearby.getUniqueId() != attacker.getBukkit().getUniqueId()) {
            
            nearby.damage(handle.getValue() * 0.5, attacker.getBukkit());
            chainCount++;
        }
    }
    
    if (chainCount > 0) {
        attacker.actionbar("&e⚡ 连锁 " + chainCount + " 个目标!");
    }
}
