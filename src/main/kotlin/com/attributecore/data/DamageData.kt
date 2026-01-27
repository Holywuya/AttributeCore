package com.attributecore.data

import com.attributecore.event.CoreConfig
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageByEntityEvent
import java.util.concurrent.ThreadLocalRandom

/**
 * 伤害计算上下文 (多类型伤害桶架构)
 */
class DamageData(
    val attacker: LivingEntity,
    val defender: LivingEntity,
    val event: EntityDamageByEntityEvent
) {
    /** 原始伤害（Bukkit事件的初始伤害），默认归入物理桶 */
    val originalDamage = event.damage

    /** 伤害倍率（全局） */
    private var damageMultiplier = 1.0

    /** 伤害标签系统 */
    val tags = mutableSetOf<String>()

    // ================= [ 多类型伤害桶 ] =================

    /** 伤害桶：类型 -> 伤害数值 (默认包含事件原始物理伤害) */
    private val damageBuckets = mutableMapOf<String, Double>("PHYSICAL" to originalDamage)

    /** 抗性桶：类型 -> 抗性百分比 (0-100) */
    private val resistanceBuckets = mutableMapOf<String, Double>()

    // ================= [ 暴击系统 ] =================
    var critTier = 0
    private var critMultiplierBonus = 0.0
    private var critResistance = 0.0
    private var critResilience = 0.0

    // ================= [ 物理防御与穿甲系统 ] =================
    private var totalDefenseScore = 0.0
    private var fixedPenetration = 0.0
    private var percentPenetration = 0.0

    // ================= [ 方法 ] =================

    fun setDamageMultiplier(multiplier: Double) { damageMultiplier *= multiplier }
    fun addTag(tag: String) = tags.add(tag.uppercase())
    fun hasTag(tag: String) = tags.contains(tag.uppercase())

    /** 向指定类型的桶增加伤害 (如 PHYSICAL, FIRE, WATER) */
    fun addBucketDamage(type: String, amount: Double) {
        val t = type.uppercase()
        damageBuckets[t] = damageBuckets.getOrDefault(t, 0.0) + amount
    }

    /** 增加指定类型的抗性 (百分比) */
    fun addBucketResistance(type: String, percent: Double) {
        val t = type.uppercase()
        resistanceBuckets[t] = resistanceBuckets.getOrDefault(t, 0.0) + percent
    }

    // 快捷方法：默认加物理伤害
    fun addDamage(amount: Double) = addBucketDamage("PHYSICAL", amount)
    fun addDirectReductionPercent(percent: Double) = addBucketResistance("PHYSICAL", percent)

    // 暴击与穿甲
    fun addCritDamage(value: Double) { critMultiplierBonus += (value / 100.0) }
    fun addCritResistance(value: Double) { critResistance += value }
    fun addCritResilience(value: Double) { critResilience += (value / 100.0) }
    fun addDefenseScore(amount: Double) { totalDefenseScore += amount }
    fun addFixedPenetration(amount: Double) { fixedPenetration += amount }
    fun addPercentPenetration(percent: Double) { percentPenetration += percent }

    fun rollCrit(totalChance: Double) {
        val actualChance = (totalChance - critResistance).coerceAtLeast(0.0)
        val baseTier = (actualChance / 100).toInt()
        val probability = actualChance % 100
        val random = ThreadLocalRandom.current().nextDouble(100.0)
        this.critTier = if (random < probability) baseTier + 1 else baseTier
    }

    /**
     * 核心计算：多桶独立结算
     */
    fun getFinalDamage(): Double {
        // 1. 计算物理护甲的减伤倍率 (K公式)
        val afterPercentArmor = totalDefenseScore * (1.0 - (percentPenetration / 100.0).coerceIn(0.0, 1.0))
        val effectiveDefense = (afterPercentArmor - fixedPenetration).coerceAtLeast(0.0)
        val k = CoreConfig.armorK
        val armorMultiplier = k / (effectiveDefense + k)

        var totalBucketDamage = 0.0

        // 2. 遍历所有伤害桶独立结算
        damageBuckets.forEach { (type, rawDamage) ->
            var bucketDamage = rawDamage

            // 物理伤害应用护甲减免
            if (type == "PHYSICAL") {
                bucketDamage *= armorMultiplier
            }

            // 应用该类型独立抗性
            val res = resistanceBuckets.getOrDefault(type, 0.0)
            bucketDamage *= (1.0 - res / 100.0).coerceAtLeast(0.0)

            totalBucketDamage += bucketDamage
        }

        // 3. 计算暴击倍率
        val baseCritMult = CoreConfig.defaultCritMultiplier
        val totalCritMultAttr = (baseCritMult + critMultiplierBonus - critResilience).coerceAtLeast(1.1)
        val finalCritMultiplier = if (critTier > 0) 1.0 + critTier * (totalCritMultAttr - 1.0) else 1.0

        // 4. 最终汇总
        return (totalBucketDamage * finalCritMultiplier * damageMultiplier).coerceAtLeast(0.0)
    }
}