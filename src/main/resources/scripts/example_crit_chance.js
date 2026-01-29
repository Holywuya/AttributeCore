/**
 * 示例属性：暴击率
 * 类型：攻击型
 * 
 * 功能：增加暴击几率，支持多层暴击
 * 示例：100% 暴击率 = 1层暴击，200% = 2层暴击
 */

function getSettings() {
    return {
        key: "crit_chance",
        names: ["暴击率", "暴击", "Crit Chance"],
        displayName: "&e暴击率",
        type: "ATTACK",
        priority: 5,
        combatPower: 1.5
    };
}

function runAttack(attr, attacker, entity, handle) {
    handle.rollCrit(handle.getValue());
}
