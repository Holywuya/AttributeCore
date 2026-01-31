// 蒸发反应 (Vaporize Reaction)
// 触发条件: 火元素攻击附着水元素光环的目标
// 效果: 造成 2 倍伤害

var phases = ["REACTION"];
var Particle = Java.type("org.bukkit.Particle");
var Sound = Java.type("org.bukkit.Sound");

function canTrigger(context) {
    return context.triggerElement === "FIRE" && context.auraElement === "WATER";
}

function execute(context) {
    if (!canTrigger(context)) {
        return;
    }

    context.damageMultiplier = 2.0;

    if (context.attacker && context.attacker.getType().toString() === "PLAYER") {
        context.attacker.sendMessage("§c§l[蒸发] §e触发! 造成 §c2倍 §e伤害!");
    }

    if (context.victim && context.victim.getType().toString() === "PLAYER") {
        context.victim.sendMessage("§c§l[蒸发] §7受到蒸发反应伤害!");
    }

    try {
        var location = context.victim.getLocation();
        var world = location.getWorld();
        if (world) {
            world.spawnParticle(Particle.CLOUD, location.getX(), location.getY() + 1, location.getZ(), 20, 1.0, 1.0, 1.0, 0.05);
            world.spawnParticle(Particle.FLAME, location.getX(), location.getY() + 1, location.getZ(), 10, 0.5, 0.5, 0.5, 0.02);
            world.playSound(location, Sound.BLOCK_FIRE_EXTINGUISH, 1.0, 1.5);
        }
    } catch (e) {
        // 粒子效果失败不影响伤害计算
    }
}
