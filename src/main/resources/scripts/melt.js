// 融化反应 (Melt Reaction)
// 触发条件: 火元素攻击附着冰元素光环的目标
// 效果: 造成 2 倍伤害

var phases = ["REACTION"];
var Particle = Java.type("org.bukkit.Particle");
var Sound = Java.type("org.bukkit.Sound");

function canTrigger(context) {
    return context.triggerElement === "FIRE" && context.auraElement === "ICE";
}

function execute(context) {
    if (!canTrigger(context)) {
        return;
    }

    context.damageMultiplier = 2.0;

    if (context.attacker && context.attacker.getType().toString() === "PLAYER") {
        context.attacker.sendMessage("§b§l[融化] §e触发! 造成 §c2倍 §e伤害!");
    }

    if (context.victim && context.victim.getType().toString() === "PLAYER") {
        context.victim.sendMessage("§b§l[融化] §7受到融化反应伤害!");
    }

    try {
        var location = context.victim.getLocation();
        var world = location.getWorld();
        if (world) {
            world.spawnParticle(Particle.DRIPPING_WATER, location.getX(), location.getY() + 1, location.getZ(), 15, 1.0, 1.0, 1.0, 0.05);
            world.spawnParticle(Particle.FLAME, location.getX(), location.getY() + 1, location.getZ(), 10, 0.5, 0.5, 0.5, 0.02);
            world.playSound(location, Sound.BLOCK_FIRE_EXTINGUISH, 1.0, 0.8);
        }
    } catch (e) {
        // 粒子效果失败不影响伤害计算
    }
}
