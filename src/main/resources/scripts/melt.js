// 融化反应 (Melt Reaction)
// 触发条件: 火元素攻击附着冰元素光环的目标
// 效果: 造成 2 倍伤害

function canTrigger(context) {
    // 火元素触发冰元素光环
    return context.triggerElement === "FIRE" && context.auraElement === "ICE";
}

function execute(context) {
    if (!canTrigger(context)) {
        return;
    }

    // 应用 2 倍伤害倍率
    context.damageMultiplier = 2.0;

    // 给攻击者发送提示消息
    if (context.attacker && context.attacker.getType() === "PLAYER") {
        context.attacker.sendMessage("§b§l[融化] §e触发! 造成 §c2倍 §e伤害!");
    }

    // 给受害者发送提示消息
    if (context.victim && context.victim.getType() === "PLAYER") {
        context.victim.sendMessage("§b§l[融化] §7受到融化反应伤害!");
    }

    // 播放粒子效果
    var location = context.victim.getLocation();
    var world = location.getWorld();
    if (world) {
        // 水滴效果 - 蓝色粒子
        for (var i = 0; i < 15; i++) {
            var offsetX = (Math.random() - 0.5) * 2;
            var offsetY = Math.random() * 2;
            var offsetZ = (Math.random() - 0.5) * 2;
            world.spawnParticle("WATER_DROP", 
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
        world.playSound(location, "BLOCK_FIRE_EXTINGUISH", 1.0, 0.8);
        world.playSound(location, "BLOCK_GLASS_BREAK", 0.5, 1.2);
    }
}
