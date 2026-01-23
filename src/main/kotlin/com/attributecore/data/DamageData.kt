package com.attributecore.data

import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageByEntityEvent
import java.util.concurrent.ConcurrentHashMap

/**
 * 伤害计算上下文对象
 * 贯穿整个伤害计算流程，用于传递数据和累加伤害
 */
data class DamageData(
    val attacker: LivingEntity,
    val defender: LivingEntity,
    val attackData: AttributeData, // 攻击者的属性面板
    val defenceData: AttributeData, // 防御者的属性面板
    val event: EntityDamageByEntityEvent // 原始 Bukkit 事件
) {
    // 存储不同类型的伤害 (Key: 来源/类型, Value: 伤害值)
    // 例如: {"Base": 100.0, "Fire": 20.0, "Skill": 500.0}
    private val damages = ConcurrentHashMap<String, Double>()

    // 添加伤害
    fun addDamage(source: String, value: Double) {
        damages.merge(source, value) { old, new -> old + new }
    }

    // 减少伤害 (例如格挡、护盾)
    fun takeDamage(source: String, value: Double) {
        damages.computeIfPresent(source) { _, v -> (v - value).coerceAtLeast(0.0) }
    }

    // 获取总伤害 (汇总所有大于0的伤害)
    fun getFinalDamage(): Double {
        return damages.values.filter { it > 0 }.sum()
    }

    // 获取特定类型的伤害
    fun getDamage(source: String): Double {
        return damages.getOrDefault(source, 0.0)
    }

    // 是否暴击 (用于后续飘字判断，可作为扩展)
    var isCrit: Boolean = false
}