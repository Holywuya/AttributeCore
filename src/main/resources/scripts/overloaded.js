// 超载反应 (Overloaded Reaction)
// 触发条件: 雷元素攻击附着火元素光环的目标
// 效果: 造成 1.5 倍伤害 + 范围爆炸伤害

function canTrigger(context) {
    // 雷元素触发火元素光环
    return context.triggerElement === "ELECTRO" && context.auraElement === "FIRE";
}

function execute(context) {
    if (!canTrigger(context)) {
        return;
    }

    // 应用 1.5 倍伤害倍率
    context.damageMultiplier = 1.5;

    // 给攻击者发送提示消息
    if (context.attacker && context.attacker.getType() === "PLAYER") {
        context.attacker.sendMessage("§6§l[超载] §e触发! 造成 §c1.5倍 §e伤害 + §6AoE爆炸!");
    }

    // 给受害者发送提示消息
    if (context.victim && context.victim.getType() === "PLAYER") {
        context.victim.sendMessage("§6§l[超载] §7受到超载反应伤害!");
    }

    var location = context.victim.getLocation();
    var world = location.getWorld();

    // AoE 爆炸效果 - 对周围 3 格内的实体造成额外伤害
    var nearbyEntities = context.victim.getNearbyEntities(3.0, 3.0, 3.0);
    var explosionDamage = context.damageBucket.getBaseDamage() * 0.5; // 50% 基础伤害

    for (var i = 0; i < nearbyEntities.size(); i++) {
        var entity = nearbyEntities.get(i);
        if (entity.getType() !== "PLAYER" && entity.getType() !== "ARMOR_STAND") {
            entity.damage(explosionDamage);
        }
    }

    // 播放粒子效果
    if (world) {
        // 爆炸粒子
        world.spawnParticle("EXPLOSION_LARGE", location.getX(), location.getY() + 1, location.getZ(), 3, 0, 0, 0, 0);
        // 火焰粒子
        for (var i = 0; i < 30; i++) {
            var offsetX = (Math.random() - 0.5) * 3;
            var offsetY = Math.random() * 2;
            var offsetZ = (Math.random() - 0.5) * 3;
            world.spawnParticle("FLAME", 
                location.getX() + offsetX, 
                location.getY() + offsetY, 
                location.getZ() + offsetZ, 
                1, 0, 0, 0, 0.05);
        }
        // 闪电粒子
        world.spawnParticle("CRIT_MAGIC", location.getX(), location.getY() + 1, location.getZ(), 20, 1.5, 1.5, 1.5, 0);
    }

    // 播放音效
    if (world) {
        world.playSound(location, "ENTITY_GENERIC_EXPLODE", 1.0, 1.0);
        world.playSound(location, "ENTITY_LIGHTNING_BOLT_THUNDER", 0.5, 1.5);
    }
}
