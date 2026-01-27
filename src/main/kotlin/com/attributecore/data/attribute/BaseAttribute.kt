package com.attributecore.data.attribute

import com.attributecore.data.AttributeType
import com.attributecore.data.DamageData
import org.bukkit.entity.LivingEntity

/**
 * 属性基类，所有属性的抽象父类
 */
abstract class BaseAttribute(
    /** 属性唯一标识符 */
    val key: String,
    /** 属性识别名称列表（用于Lore匹配） */
    val names: List<String>,
    /** 属性类型 */
    val type: AttributeType,
    /** 优先级（影响计算顺序） */
    val priority: Int = 0,
    val tags: List<String> = emptyList(),
    val element: String? = null,
    val combatPower: Double = 1.0
) {

    /** 获取显示名称 */
    abstract fun getDisplayName(): String

    /**
     * 当属性作用于攻击时调用
     * @param damageData 伤害数据上下文
     * @param value 属性值
     * @param extraValue 额外值通常为0
     */
    open fun onAttack(damageData: DamageData, value: Double, extraValue: Double) {}

    /**
     * 当属性作用于防御时调用
     * @param damageData 伤害数据上下文
     * @param value 属性值
     * @param extraValue 额外值通常为0
     */
    open fun onDefend(damageData: DamageData, value: Double, extraValue: Double) {}

    /**
     * 实体更新时调用（如生命恢复等）
     * @param entity 实体
     * @param value 属性值
     */
    open fun onUpdate(entity: LivingEntity, value: Double) {}
}
