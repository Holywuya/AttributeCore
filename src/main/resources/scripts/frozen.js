// 冻结反应 (Frozen Reaction)
// 触发条件: 冰元素攻击附着水元素光环的目标，或水元素攻击附着冰元素光环的目标
// 效果: 造成 1.2 倍伤害 + 冻结目标 3 秒 (减速 80%)

var phases = ["REACTION"];
var Particle = Java.type("org.bukkit.Particle");
var Sound = Java.type("org.bukkit.Sound");
var PotionEffectType = Java.type("org.bukkit.potion.PotionEffectType");
var PotionEffect = Java.type("org.bukkit.potion.PotionEffect");

function canTrigger(context) {
    var t = context.triggerElement;
    var a = context.auraElement;
    return (t === "ICE" && a === "WATER") || (t === "WATER" && a === "ICE");
}

function execute(context) {
    if (!canTrigger(context)) {
        return;
    }

    context.damageMultiplier = 1.2;

    if (context.attacker && context.attacker.getType().toString() === "PLAYER") {
        context.attacker.sendMessage("§b§l[冻结] §e触发! 造成 §c1.2倍 §e伤害 + §b冻结效果!");
    }

    if (context.victim && context.victim.getType().toString() === "PLAYER") {
        context.victim.sendMessage("§b§l[冻结] §7你被冻结了! (3秒)");
    }

    try {
        var slowEffect = new PotionEffect(PotionEffectType.SLOWNESS, 60, 3, false, true);
        context.victim.addPotionEffect(slowEffect);

        var location = context.victim.getLocation();
        var world = location.getWorld();
        if (world) {
            world.spawnParticle(Particle.SNOWFLAKE, location.getX(), location.getY() + 1, location.getZ(), 25, 0.75, 1.0, 0.75, 0.05);
            world.playSound(location, Sound.BLOCK_GLASS_BREAK, 1.0, 0.5);
        }
    } catch (e) {
    }
}
