package com.attributecore.data.attribute

import com.attributecore.data.AttributeType
import com.attributecore.data.DamageData
import org.bukkit.entity.LivingEntity

/**
 * 属性基类，所有属性的抽象父类
 * 
 * 属性系统支持：
 * - 多优先级分层计算
 * - 标签系统（用于条件应用和元素反应）
 * - 元素系统（火、水、冰等）
 * - 战斗力权重
 */
abstract class BaseAttribute(
    /** 属性唯一标识符 */
    val key: String,
    /** 属性识别名称列表（用于Lore匹配） */
    val names: List<String>,
    /** 属性类型 (ATTACK/DEFEND/PASSIVE) */
    val type: AttributeType,
    /** 优先级（数值越小越先计算） */
    val priority: Int = 0,
    /** 属性标签列表（用于条件判断） */
    val tags: List<String> = emptyList(),
    /** 元素类型（FIRE/WATER/ICE/LIGHT/DARK 等） */
    val element: String? = null,
    /** 战斗力权重（1.0 = 标准权重） */
    val combatPower: Double = 1.0
) {

    /** 获取显示名称（带颜色代码） */
    abstract fun getDisplayName(): String

    /**
     * 当属性作用于攻击时调用
     * 攻击者使用此方法向伤害数据注入属性效果
     * 
     * @param damageData 伤害计算上下文
     * @param value 属性最小值
     * @param extraValue 属性最大值
     */
    open fun onAttack(damageData: DamageData, value: Double, extraValue: Double) {}

    /**
     * 当属性作用于防御时调用
     * 防御者使用此方法向伤害数据注入属性效果
     * 
     * @param damageData 伤害计算上下文
     * @param value 属性最小值
     * @param extraValue 属性最大值
     */
    open fun onDefend(damageData: DamageData, value: Double, extraValue: Double) {}

    /**
     * 实体更新时调用（心跳更新）
     * 用于持续性效果、生命恢复等
     * 
     * @param entity 实体
     * @param value 属性值
     */
    open fun onUpdate(entity: LivingEntity, value: Double) {}

    /**
     * 检查属性是否可应用于目标
     * 用于条件应用，默认总是可应用
     * 
     * @param attacker 攻击者
     * @param defender 防御者
     */
    open fun canApply(attacker: LivingEntity, defender: LivingEntity): Boolean = true
}

