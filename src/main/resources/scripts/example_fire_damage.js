/**
 * 示例属性：火焰伤害
 * 类型：攻击型 + 元素
 * 
 * 功能：造成火焰元素伤害，可触发元素反应
 */

function getSettings() {
    return {
        key: "fire_damage",
        names: ["火焰伤害", "Fire Damage"],
        displayName: "&c&l火焰伤害",
        type: "ATTACK",
        priority: 15,
        element: "FIRE",
        combatPower: 1.3
    };
}

function runAttack(attr, attacker, entity, handle) {
    attacker.addElementalDamage("FIRE", handle.getValue());
}
