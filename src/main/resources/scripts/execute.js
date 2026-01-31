var api = Java.type('com.attributecore.script.ScriptAPI');

function onPreDamage(ctx) {
    var attacker = ctx.attacker;
    var victim = ctx.victim;
    var executeThreshold = api.getAttribute(attacker, "execute_threshold");
    
    if (executeThreshold <= 0) return;
    
    var victimHealthPercent = (victim.getHealth() / victim.getMaxHealth()) * 100;
    
    if (victimHealthPercent <= executeThreshold) {
        ctx.damageMultiplier = 3.0;
        api.tell(attacker, "&4&l斩杀! &c对低血量目标造成 &e3x &c伤害");
        api.playSound(attacker, "ENTITY_PLAYER_ATTACK_CRIT", 1.0, 0.5);
    }
}
