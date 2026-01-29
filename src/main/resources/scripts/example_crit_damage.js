/**
 * 示例属性：暴击伤害
 * 类型：攻击型
 * 
 * 功能：增加暴击伤害倍率
 */

function getSettings() {
    return {
        key: "crit_damage",
        names: ["暴击伤害", "暴伤", "Crit Damage"],
        displayName: "&6暴击伤害",
        type: "ATTACK",
        priority: 6,
        combatPower: 1.5
    };
}

function runAttack(attr, attacker, entity, handle) {
    handle.addCritDamage(handle.getValue());
}
