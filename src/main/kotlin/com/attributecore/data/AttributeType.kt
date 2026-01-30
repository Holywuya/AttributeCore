package com.attributecore.data

/**
 * 属性类型枚举
 * 参考 SX-Attribute 的设计
 *
 * @property Attack 攻击类属性（伤害计算时触发）
 * @property Defence 防御类属性（受伤时触发）
 * @property Update 更新类属性（装备变更时更新玩家状态）
 * @property Other 其他类属性（特殊逻辑）
 */
enum class AttributeType {
    /**
     * 攻击类属性
     * 在实体造成伤害时触发
     */
    Attack,

    /**
     * 防御类属性
     * 在实体受到伤害时触发
     */
    Defence,

    /**
     * 更新类属性
     * 在装备变更时更新实体属性（如移动速度、攻击速度）
     */
    Update,

    /**
     * 其他类属性
     * 包含特殊机制属性（如经验加成、JavaScript 自定义属性）
     */
    Other
}
