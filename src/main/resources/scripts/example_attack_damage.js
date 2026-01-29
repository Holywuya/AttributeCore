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
    print("[AC-JS-DEBUG] runAttack called: key=" + attr.key + ", value=" + value);
    print("[AC-JS-DEBUG] attacker type: " + (typeof attacker) + ", has addDamage: " + (typeof attacker.addDamage));
    attacker.addDamage(value);
    print("[AC-JS-DEBUG] addDamage called successfully");
}
