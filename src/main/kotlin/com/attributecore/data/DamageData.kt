package com.attributecore.data

import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageByEntityEvent

class DamageData(
    val attacker: LivingEntity,
    val defender: LivingEntity,
    val event: EntityDamageByEntityEvent
) {
    val originalDamage = event.damage
    private var damageBonus = 0.0          // 基础伤害加成
    private var damageMultiplier = 1.0     // 最终伤害倍率

    private var totalDefenseScore = 0.0      // 累积防御点数 (护甲值)
    private var directReductionPercent = 0.0 // 直接减伤百分比 (0-100)

    private var fixedPenetration = 0.0       // 固定穿甲
    private var percentPenetration = 0.0     // 百分比穿甲 (0-100)

    private val tags = mutableSetOf<String>()

    fun addTag(tag: String) = tags.add(tag.uppercase())
    fun hasTag(tag: String) = tags.contains(tag.uppercase())

    fun addDamage(amount: Double) { damageBonus += amount }
    fun setDamageMultiplier(multiplier: Double) { damageMultiplier *= multiplier }

    fun addDefenseScore(amount: Double) { totalDefenseScore += amount }
    fun addDirectReductionPercent(percent: Double) { directReductionPercent += percent }

    fun addFixedPenetration(amount: Double) { fixedPenetration += amount }
    fun addPercentPenetration(percent: Double) { percentPenetration += percent }

    /**
     * 获取最终伤害计算结果
     */
    fun getFinalDamage(): Double {
        // 1. 基础总伤害
        val rawTotalDamage = originalDamage + damageBonus

        // 2. 计算有效防御 (先算百分比穿透，再算固定穿透)
        val afterPercent = totalDefenseScore * (1.0 - (percentPenetration / 100.0).coerceIn(0.0, 1.0))
        val effectiveDefense = (afterPercent - fixedPenetration).coerceAtLeast(0.0)

        // 3. 护甲曲线公式: DamageMultiplier = K / (Defense + K)
        val k = 400.0
        val armorMultiplier = k / (effectiveDefense + k)

        // 4. 直接减伤百分比倍率
        val directResistMultiplier = (1.0 - (directReductionPercent / 100.0)).coerceAtLeast(0.0)

        // 5. 汇总
        return (rawTotalDamage * armorMultiplier * directResistMultiplier * damageMultiplier).coerceAtLeast(0.0)
    }
}