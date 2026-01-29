/**
 * 示例属性：伤害减免
 * 类型：防御型
 * 
 * 功能：百分比减少受到的所有伤害
 */

function getSettings() {
    return {
        key: "damage_reduction",
        names: ["伤害减免", "Damage Reduction"],
        displayName: "&b伤害减免",
        type: "DEFEND",
        priority: 5,
        combatPower: 2.0
    };
}

function runDefend(attr, attacker, entity, handle) {
    handle.addUniversalReduction(handle.getValue());
}
