/**
 * 示例属性：护甲穿透
 * 类型：攻击型
 * 
 * 功能：固定护甲穿透，直接减少目标护甲值
 */

function getSettings() {
    return {
        key: "armor_penetration",
        names: ["护甲穿透", "穿甲", "Armor Penetration"],
        displayName: "&7护甲穿透",
        type: "ATTACK",
        priority: 8,
        combatPower: 1.2
    };
}

function runAttack(attr, attacker, entity, handle) {
    handle.addFixedPenetration(handle.getValue());
}
