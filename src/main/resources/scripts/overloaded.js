// 超载反应 (Overloaded Reaction)
// 触发条件: 雷元素攻击附着火元素光环的目标，或火元素攻击附着雷元素光环的目标
// 效果: 造成 1.5 倍伤害 + 范围爆炸伤害

var phases = ["REACTION"];
var Particle = Java.type("org.bukkit.Particle");
var Sound = Java.type("org.bukkit.Sound");

function canTrigger(context) {
    var t = context.triggerElement;
    var a = context.auraElement;
    return (t === "ELECTRO" && a === "FIRE") || (t === "FIRE" && a === "ELECTRO");
}

function execute(context) {
    if (!canTrigger(context)) {
        return;
    }

    context.damageMultiplier = 1.5;

    if (context.attacker && context.attacker.getType().toString() === "PLAYER") {
        context.attacker.sendMessage("§6§l[超载] §e触发! 造成 §c1.5倍 §e伤害 + §6AoE爆炸!");
    }

    if (context.victim && context.victim.getType().toString() === "PLAYER") {
        context.victim.sendMessage("§6§l[超载] §7受到超载反应伤害!");
    }

    try {
        var location = context.victim.getLocation();
        var world = location.getWorld();

        var nearbyEntities = context.victim.getNearbyEntities(3.0, 3.0, 3.0);
        var explosionDamage = 5.0;

        for (var i = 0; i < nearbyEntities.size(); i++) {
            var entity = nearbyEntities.get(i);
            var type = entity.getType().toString();
            if (type !== "PLAYER" && type !== "ARMOR_STAND") {
                entity.damage(explosionDamage);
            }
        }

        if (world) {
            world.spawnParticle(Particle.EXPLOSION, location.getX(), location.getY() + 1, location.getZ(), 3, 0, 0, 0, 0);
            world.spawnParticle(Particle.FLAME, location.getX(), location.getY() + 1, location.getZ(), 30, 1.5, 1.0, 1.5, 0.05);
            world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0, 1.0);
        }
    } catch (e) {
    }
}
