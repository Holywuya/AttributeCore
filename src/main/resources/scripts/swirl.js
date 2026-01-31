// 扩散反应 (Swirl Reaction)
// 触发条件: 风元素攻击附着任意元素光环的目标
// 效果: 造成 1.3 倍伤害 + 传播元素光环到周围敌人

var phases = ["REACTION"];

function canTrigger(context) {
    // 风元素触发任意其他元素光环
    return context.triggerElement === "WIND" && 
           context.auraElement !== null && 
           context.auraElement !== "WIND";
}

function execute(context) {
    if (!canTrigger(context)) {
        return;
    }

    // 应用 1.3 倍伤害倍率
    context.damageMultiplier = 1.3;

    // 给攻击者发送提示消息
    if (context.attacker && context.attacker.getType() === "PLAYER") {
        var elementName = getElementName(context.auraElement);
        context.attacker.sendMessage("§a§l[扩散] §e触发! 造成 §c1.3倍 §e伤害 + 传播" + elementName + "!");
    }

    // 给受害者发送提示消息
    if (context.victim && context.victim.getType() === "PLAYER") {
        context.victim.sendMessage("§a§l[扩散] §7受到扩散反应伤害!");
    }

    var location = context.victim.getLocation();
    var world = location.getWorld();

    // 传播元素光环到周围 4 格内的实体
    var ElementalAura = Java.type("com.attributecore.data.ElementalAura");
    var nearbyEntities = context.victim.getNearbyEntities(4.0, 4.0, 4.0);
    var spreadCount = 0;

    for (var i = 0; i < nearbyEntities.size(); i++) {
        var entity = nearbyEntities.get(i);
        // 只传播到敌对生物或其他玩家
        if (entity.getType() !== "ARMOR_STAND" && entity !== context.attacker) {
            ElementalAura.INSTANCE.applyAura(entity, context.auraElement, 100); // 5 秒光环
            spreadCount++;
        }
    }

    if (context.attacker && context.attacker.getType() === "PLAYER" && spreadCount > 0) {
        context.attacker.sendMessage("§a   → 传播到 §e" + spreadCount + " §a个目标");
    }

    // 播放粒子效果
    if (world) {
        // 风旋涡效果
        for (var i = 0; i < 40; i++) {
            var angle = (i / 40.0) * Math.PI * 4; // 两圈螺旋
            var radius = 2.0 * (i / 40.0);
            var offsetX = Math.cos(angle) * radius;
            var offsetZ = Math.sin(angle) * radius;
            var offsetY = (i / 40.0) * 2.0;
            
            world.spawnParticle("VILLAGER_HAPPY", 
                location.getX() + offsetX, 
                location.getY() + offsetY, 
                location.getZ() + offsetZ, 
                1, 0, 0, 0, 0);
        }
        
        // 元素粒子 (根据被扩散的元素类型)
        var elementParticle = getElementParticle(context.auraElement);
        world.spawnParticle(elementParticle, location.getX(), location.getY() + 1, location.getZ(), 20, 2, 1, 2, 0.05);
    }

    // 播放音效
    if (world) {
        world.playSound(location, "ENTITY_ENDER_DRAGON_FLAP", 0.7, 1.5);
        world.playSound(location, "BLOCK_WOOL_BREAK", 1.0, 0.8);
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
        case "FIRE": return "FLAME";
        case "WATER": return "WATER_DROP";
        case "ICE": return "SNOW_SHOVEL";
        case "ELECTRO": return "CRIT_MAGIC";
        default: return "VILLAGER_HAPPY";
    }
}
