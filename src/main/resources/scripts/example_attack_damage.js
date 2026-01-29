/**
 * 示例属性：物理攻击力
 * 类型：攻击型
 * 
 * 功能：增加基础物理伤害
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
    // 直接增加物理伤害
    handle.addDamage(handle.getValue());
}
