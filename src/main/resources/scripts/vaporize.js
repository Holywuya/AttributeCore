// 蒸发反应 (Vaporize Reaction)
// 触发条件: 火元素攻击附着水元素光环的目标，或水元素攻击附着火元素光环的目标
// 效果: 造成 2 倍伤害（火→水）或 1.5 倍伤害（水→火）

var phases = ["REACTION"];
var Particle = Java.type("org.bukkit.Particle");
var Sound = Java.type("org.bukkit.Sound");

function canTrigger(context) {
    var t = context.triggerElement;
    var a = context.auraElement;
    // 双向触发：火+水 或 水+火
    return (t === "FIRE" && a === "WATER") || (t === "WATER" && a === "FIRE");
}

function execute(context) {
    if (!canTrigger(context)) {
        return;
    }

    var t = context.triggerElement;
    var a = context.auraElement;
    
    // 火→水: 2.0倍伤害（强蒸发）
    // 水→火: 1.5倍伤害（弱蒸发）
    if (t === "FIRE" && a === "WATER") {
        context.damageMultiplier = 2.0;
        if (context.attacker && context.attacker.getType().toString() === "PLAYER") {
            context.attacker.sendMessage("§c§l[蒸发] §e触发! 造成 §c2倍 §e伤害!");
        }
    } else {
        context.damageMultiplier = 1.5;
        if (context.attacker && context.attacker.getType().toString() === "PLAYER") {
            context.attacker.sendMessage("§9§l[蒸发] §e触发! 造成 §c1.5倍 §e伤害!");
        }
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
