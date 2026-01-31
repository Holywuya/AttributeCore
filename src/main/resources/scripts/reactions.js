var api = Java.type('com.attributecore.script.ScriptAPI');
var Element = Java.type('com.attributecore.data.Element');

function onReaction(ctx) {
    var aura = ctx.auraElement;
    var trigger = ctx.triggerElement;
    var attacker = ctx.attacker;
    var victim = ctx.victim;
    
    if (aura == Element.FIRE && trigger == Element.WATER) {
        ctx.damageMultiplier = 2.0;
        api.tell(attacker, "&b&l蒸发! &7造成 &e2x &7伤害");
        api.playSound(attacker, "ENTITY_GENERIC_EXTINGUISH_FIRE", 1.0, 1.2);
        return;
    }
    
    if (aura == Element.WATER && trigger == Element.FIRE) {
        ctx.damageMultiplier = 1.5;
        api.tell(attacker, "&6&l蒸发! &7造成 &e1.5x &7伤害");
        api.playSound(attacker, "ENTITY_GENERIC_EXTINGUISH_FIRE", 1.0, 0.8);
        return;
    }
    
    if (aura == Element.WATER && trigger == Element.ELECTRO) {
        ctx.damageMultiplier = 1.5;
        var nearby = api.getNearbyEntities(victim, 3.0);
        for (var i = 0; i < nearby.length; i++) {
            var entity = nearby[i];
            if (entity != victim && entity != attacker) {
                api.damage(attacker, entity, ctx.damageBucket.total() * 0.3);
            }
        }
        api.tell(attacker, "&9&l感电! &7对周围敌人造成连锁伤害");
        api.playSound(attacker, "ENTITY_LIGHTNING_BOLT_THUNDER", 0.5, 1.5);
        return;
    }
    
    if (aura == Element.ICE && trigger == Element.FIRE) {
        ctx.damageMultiplier = 2.0;
        api.tell(attacker, "&c&l融化! &7造成 &e2x &7伤害");
        api.playSound(attacker, "BLOCK_FIRE_EXTINGUISH", 1.0, 1.0);
        return;
    }
    
    if (aura == Element.FIRE && trigger == Element.ICE) {
        ctx.damageMultiplier = 1.5;
        api.tell(attacker, "&c&l融化! &7造成 &e1.5x &7伤害");
        return;
    }
    
    if (aura == Element.ICE && trigger == Element.ELECTRO) {
        ctx.damageMultiplier = 1.8;
        api.tell(attacker, "&d&l超导! &7造成 &e1.8x &7伤害并降低防御");
        return;
    }
    
    if ((aura == Element.FIRE || aura == Element.ELECTRO) && trigger == Element.WIND) {
        ctx.damageMultiplier = 1.5;
        var nearby = api.getNearbyEntities(victim, 4.0);
        for (var i = 0; i < nearby.length; i++) {
            var entity = nearby[i];
            if (entity != victim && entity != attacker) {
                api.applyAura(entity, aura.configKey, 0.5);
            }
        }
        api.tell(attacker, "&a&l扩散! &7元素扩散到周围敌人");
        return;
    }
    
    ctx.damageMultiplier = 1.2;
    api.log("Unknown reaction: " + aura.name() + " + " + trigger.name());
}
