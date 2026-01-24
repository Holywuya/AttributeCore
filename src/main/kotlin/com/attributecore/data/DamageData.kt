package com.attributecore.data

import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageByEntityEvent

/**
 * 伤害计算上下文，封装战斗相关的所有数据
 */
class DamageData(
    val attacker: LivingEntity,
    val defender: LivingEntity,
    val attackData: AttributeData?,
    val defenceData: AttributeData?,
    val event: EntityDamageByEntityEvent
) {
    /** 原始伤害 */
    val originalDamage = event.damage
    /** 伤害修正值 */
    private var damageBonus = 0.0
    /** 伤害倍率 */
    private var damageMultiplier = 1.0

    /**
     * 增加伤害
     */
    fun addDamage(amount: Double) {
        damageBonus += amount
    }

    /**
     * 设置伤害倍率
     */
    fun setDamageMultiplier(multiplier: Double) {
        damageMultiplier = multiplier
    }

    // 在 DamageData 类中添加以下字段和方法

    private var damageReduction = 0.0

    /**
     * 减少伤害
     */
    fun reduceDamage(amount: Double) {
        damageReduction += amount
    }

    /**
     * 获取最终伤害（更新后）
     */
    fun getFinalDamage(): Double {
        val damageWithBonus = originalDamage + damageBonus
        val damageAfterReduction = damageWithBonus - damageReduction
        return (damageAfterReduction * damageMultiplier).coerceAtLeast(0.0)
    }

}
