package com.attributecore.data

enum class AttributeType {
    /** 攻击型属性 (如 伤害，暴击) */
    ATTACK,
    /** 防御型属性 (如 防御，韧性) */
    DEFENSE,
    /** 更新属性 (动态影响玩家状态，如 速度，生命恢复)[11] */
    UPDATE,
    /** 其他自定义属性 */
    OTHER
}
