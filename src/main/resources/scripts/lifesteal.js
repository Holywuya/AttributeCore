var api = Java.type('com.attributecore.script.ScriptAPI');

function onPostDamage(ctx) {
    var attacker = ctx.attacker;
    var lifeSteal = api.getAttribute(attacker, "life_steal");
    
    if (lifeSteal <= 0) return;
    
    var damage = ctx.damageBucket.total();
    var healAmount = damage * (lifeSteal / 100.0);
    
    if (healAmount > 0) {
        api.heal(attacker, healAmount);
        api.actionbar(attacker, "&c❤ &7吸血 &a+" + healAmount.toFixed(1) + " &7生命值");
    }
}
