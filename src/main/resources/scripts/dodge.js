var api = Java.type('com.attributecore.script.ScriptAPI');

function onPreDamage(ctx) {
    var victim = ctx.victim;
    var dodgeChance = api.getAttribute(victim, "dodge_chance");
    
    if (dodgeChance <= 0) return;
    
    if (api.chance(dodgeChance)) {
        ctx.cancelled = true;
        api.tell(victim, "&7&l闪避! &f你躲开了攻击");
        api.playSound(victim, "ENTITY_ENDERMAN_TELEPORT", 0.5, 1.5);
    }
}
