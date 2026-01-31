var api = Java.type('com.attributecore.script.ScriptAPI');

function onPostDamage(ctx) {
    var attacker = ctx.attacker;
    var victim = ctx.victim;
    var thorns = api.getAttribute(victim, "thorns");
    
    if (thorns <= 0) return;
    
    var damage = ctx.damageBucket.total();
    var reflectAmount = damage * (thorns / 100.0);
    
    if (reflectAmount > 0) {
        api.delay(1, function() {
            api.damage(victim, attacker, reflectAmount);
        });
        api.tell(attacker, "&4荆棘! &c受到 " + reflectAmount.toFixed(1) + " 反伤");
        api.playSound(attacker, "ENCHANT_THORNS_HIT", 1.0, 1.0);
    }
}
