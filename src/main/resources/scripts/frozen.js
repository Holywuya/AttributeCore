// 冻结反应 (Frozen Reaction)
// 触发条件: 冰元素攻击附着水元素光环的目标
// 效果: 造成 1.2 倍伤害 + 冻结目标 3 秒 (减速 80%)

function canTrigger(context) {
    // 冰元素触发水元素光环
    return context.triggerElement === "ICE" && context.auraElement === "WATER";
}

function execute(context) {
    if (!canTrigger(context)) {
        return;
    }

    // 应用 1.2 倍伤害倍率
    context.damageMultiplier = 1.2;

    // 给攻击者发送提示消息
    if (context.attacker && context.attacker.getType() === "PLAYER") {
        context.attacker.sendMessage("§b§l[冻结] §e触发! 造成 §c1.2倍 §e伤害 + §b冻结效果!");
    }

    // 给受害者发送提示消息
    if (context.victim && context.victim.getType() === "PLAYER") {
        context.victim.sendMessage("§b§l[冻结] §7你被冻结了! (3秒)");
    }

    // 应用缓慢效果 (冻结)
    var PotionEffectType = Java.type("org.bukkit.potion.PotionEffectType");
    var PotionEffect = Java.type("org.bukkit.potion.PotionEffect");
    
    // 缓慢 IV (80% 减速) 持续 60 ticks (3 秒)
    var slowEffect = new PotionEffect(PotionEffectType.SLOW, 60, 3, false, true);
    context.victim.addPotionEffect(slowEffect);

    // 播放粒子效果
    var location = context.victim.getLocation();
    var world = location.getWorld();
    if (world) {
        // 冰晶粒子
        for (var i = 0; i < 25; i++) {
            var offsetX = (Math.random() - 0.5) * 1.5;
            var offsetY = Math.random() * 2;
            var offsetZ = (Math.random() - 0.5) * 1.5;
            world.spawnParticle("SNOW_SHOVEL", 
                location.getX() + offsetX, 
                location.getY() + offsetY, 
                location.getZ() + offsetZ, 
                1, 0, 0, 0, 0);
        }
        // 蓝色粒子
        world.spawnParticle("VILLAGER_HAPPY", location.getX(), location.getY() + 1, location.getZ(), 15, 0.5, 0.5, 0.5, 0);
    }

    // 播放音效
    if (world) {
        world.playSound(location, "BLOCK_GLASS_BREAK", 1.0, 0.5);
        world.playSound(location, "BLOCK_SNOW_BREAK", 1.0, 0.8);
    }
}
