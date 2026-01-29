/**
 * 物理攻击力
 * 类型：ATTACK
 */

function getSettings() {
    return {
        key: "attack",
        names: ["物理攻击", "攻击力", "Attack"],
        displayName: "&c物理攻击",
        type: "ATTACK",
        priority: 10,
        combatPower: 1.0
    };
}

function runAttack(attr, attacker, entity, handle) {
    var value = handle.getValue();
    
    try {
        api.tell(attacker.getBukkit(), "[JS] runAttack: key=" + attr.key + ", value=" + value);
        api.tell(attacker.getBukkit(), "[JS] About to call addDamage with value: " + value);
        attacker.addDamage(value);
        api.tell(attacker.getBukkit(), "[JS] addDamage SUCCESS");
    } catch (e) {
        api.tell(attacker.getBukkit(), "[JS] ERROR: " + e.message);
    }
}
