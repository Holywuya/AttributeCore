/**
 * 示例属性：护甲值
 * 类型：防御型
 * 
 * 功能：增加物理护甲，降低物理伤害
 */

function getSettings() {
    return {
        key: "defense",
        names: ["护甲", "防御力", "Defense", "Armor"],
        displayName: "&9护甲",
        type: "DEFEND",
        priority: 10,
        combatPower: 1.0
    };
}

function runDefend(attr, attacker, entity, handle) {
    entity.addDefenseScore(handle.getValue());
}
