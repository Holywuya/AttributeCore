package com.attributecore.data

import com.attributecore.event.CoreConfig // 引用配置
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageByEntityEvent

class DamageData(
    val attacker: LivingEntity,
    val defender: LivingEntity,
    val event: EntityDamageByEntityEvent
) {
    val originalDamage = event.damage
    private var damageBonus = 0.0
    private var damageMultiplier = 1.0

    // ✅ 暴击相关
    var isCrit = false
    private var critMultiplierBonus = 0.0 // 额外的暴击伤害百分比

    private var totalDefenseScore = 0.0
    private var directReductionPercent = 0.0
    private var fixedPenetration = 0.0
    private var percentPenetration = 0.0

    private val tags = mutableSetOf<String>()

    fun addTag(tag: String) = tags.add(tag.uppercase())
    fun hasTag(tag: String) = tags.contains(tag.uppercase())

    fun addDamage(amount: Double) { damageBonus += amount }
    fun setDamageMultiplier(multiplier: Double) { damageMultiplier *= multiplier }

    // ✅ 增加暴击倍率 (例如 value = 50 代表 +50% 暴击伤害)
    fun addCritDamage(value: Double) {
        critMultiplierBonus += (value / 100.0)
    }

    fun addDefenseScore(amount: Double) { totalDefenseScore += amount }
    fun addDirectReductionPercent(percent: Double) { directReductionPercent += percent }
    fun addFixedPenetration(amount: Double) { fixedPenetration += amount }
    fun addPercentPenetration(percent: Double) { percentPenetration += percent }

    fun getFinalDamage(): Double {
        val rawTotalDamage = originalDamage + damageBonus

        // 1. 处理有效防御
        val afterPercent = totalDefenseScore * (1.0 - (percentPenetration / 100.0).coerceIn(0.0, 1.0))
        val effectiveDefense = (afterPercent - fixedPenetration).coerceAtLeast(0.0)

        // 2. ✅ 使用配置文件中的 Armor K 值
        val k = CoreConfig.armorK
        val armorMultiplier = k / (effectiveDefense + k)

        // 3. 抗性倍率
        val resistMultiplier = (1.0 - (directReductionPercent / 100.0)).coerceAtLeast(0.0)

        // 4. ✅ 计算暴击倍率 (基础 200% + 额外加成)
        val finalCritMult = if (isCrit) (2.0 + critMultiplierBonus) else 1.0

        return (rawTotalDamage * armorMultiplier * resistMultiplier * damageMultiplier * finalCritMult).coerceAtLeast(0.0)
    }
}