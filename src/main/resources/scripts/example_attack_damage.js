/**
 * 物理攻击力
 * 类型:ATTACK
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
    java.lang.System.out.println("[JS-DIRECT] runAttack START");
    
    var value = handle.getValue();
    java.lang.System.out.println("[JS-DIRECT] getValue() returned: " + value);
    
    java.lang.System.out.println("[JS-DIRECT] Calling addDamage(" + value + ")");
    attacker.addDamage(value);
    java.lang.System.out.println("[JS-DIRECT] addDamage() completed");
}
