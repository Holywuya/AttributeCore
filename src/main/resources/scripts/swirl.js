// 扩散反应 (Swirl Reaction)
// 触发条件: 风元素攻击附着任意元素光环的目标
// 效果: 造成 1.3 倍伤害 + 传播元素光环到周围敌人

var phases = ["REACTION"];
var Particle = Java.type("org.bukkit.Particle");
var Sound = Java.type("org.bukkit.Sound");

function canTrigger(context) {
    return context.triggerElement === "WIND" && 
           context.auraElement !== null && 
           context.auraElement !== "WIND" &&
           context.auraElement !== "PHYSICAL";
}

function execute(context) {
    if (!canTrigger(context)) {
        return;
    }

    context.damageMultiplier = 1.3;

    var elementName = getElementName(context.auraElement);
    if (context.attacker && context.attacker.getType().toString() === "PLAYER") {
        context.attacker.sendMessage("§a§l[扩散] §e触发! 造成 §c1.3倍 §e伤害 + 传播" + elementName + "!");
    }

    if (context.victim && context.victim.getType().toString() === "PLAYER") {
        context.victim.sendMessage("§a§l[扩散] §7受到扩散反应伤害!");
    }

    try {
        var location = context.victim.getLocation();
        var world = location.getWorld();

        var ElementalAura = Java.type("com.attributecore.data.ElementalAura");
        var nearbyEntities = context.victim.getNearbyEntities(4.0, 4.0, 4.0);
        var spreadCount = 0;
        var auraElement = context.auraElement;

        for (var i = 0; i < nearbyEntities.size(); i++) {
            var entity = nearbyEntities.get(i);
            var type = entity.getType().toString();
            if (type !== "ARMOR_STAND" && entity !== context.attacker) {
                ElementalAura.INSTANCE.applyAura(entity, auraElement, 100);
                spreadCount++;
            }
        }

        if (context.attacker && context.attacker.getType().toString() === "PLAYER" && spreadCount > 0) {
            context.attacker.sendMessage("§a   → 传播到 §e" + spreadCount + " §a个目标");
        }

        if (world) {
            world.spawnParticle(Particle.HAPPY_VILLAGER, location.getX(), location.getY() + 1, location.getZ(), 40, 2.0, 1.5, 2.0, 0.1);
            world.spawnParticle(getElementParticle(context.auraElement), location.getX(), location.getY() + 1, location.getZ(), 20, 2, 1, 2, 0.05);
            world.playSound(location, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.7, 1.5);
        }
    } catch (e) {
    }
}

function getElementName(element) {
    switch (element) {
        case "FIRE": return "§c火元素";
        case "WATER": return "§9水元素";
        case "ICE": return "§b冰元素";
        case "ELECTRO": return "§e雷元素";
        default: return "元素";
    }
}

function getElementParticle(element) {
    switch (element) {
        case "FIRE": return Particle.FLAME;
        case "WATER": return Particle.DRIPPING_WATER;
        case "ICE": return Particle.SNOWFLAKE;
        case "ELECTRO": return Particle.ELECTRIC_SPARK;
        default: return Particle.HAPPY_VILLAGER;
    }
}
