// 蒸发反应 (Vaporize Reaction)
// 触发条件: 火元素攻击附着水元素光环的目标
// 效果: 造成 2 倍伤害

var phases = ["REACTION"];

function canTrigger(context) {
    // 火元素触发水元素光环
    return context.triggerElement === "FIRE" && context.auraElement === "WATER";
}

function execute(context) {
    if (!canTrigger(context)) {
        return;
    }

    // 应用 2 倍伤害倍率
    context.damageMultiplier = 2.0;

    // 给攻击者发送提示消息
    if (context.attacker && context.attacker.getType() === "PLAYER") {
        context.attacker.sendMessage("§c§l[蒸发] §e触发! 造成 §c2倍 §e伤害!");
    }

    // 给受害者发送提示消息
    if (context.victim && context.victim.getType() === "PLAYER") {
        context.victim.sendMessage("§c§l[蒸发] §7受到蒸发反应伤害!");
    }

    // 播放粒子效果
    var location = context.victim.getLocation();
    var world = location.getWorld();
    if (world) {
        // 蒸汽效果 - 白色粒子
        for (var i = 0; i < 20; i++) {
            var offsetX = (Math.random() - 0.5) * 2;
            var offsetY = Math.random() * 2;
            var offsetZ = (Math.random() - 0.5) * 2;
            world.spawnParticle("CLOUD", 
                location.getX() + offsetX, 
                location.getY() + offsetY, 
                location.getZ() + offsetZ, 
                1, 0, 0, 0, 0);
        }
        // 火焰粒子
        world.spawnParticle("FLAME", location.getX(), location.getY() + 1, location.getZ(), 10, 0.5, 0.5, 0.5, 0.02);
    }

    // 播放音效
    if (world) {
        world.playSound(location, "ENTITY_GENERIC_BURN", 1.0, 1.5);
        world.playSound(location, "BLOCK_FIRE_EXTINGUISH", 0.8, 1.2);
    }
}
