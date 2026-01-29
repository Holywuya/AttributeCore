package com.attributecore.data

import com.attributecore.event.CoreConfig
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageByEntityEvent
import java.util.concurrent.ThreadLocalRandom

/**
 * 伤害计算上下文 (多类型伤害桶架构)
 * 支持复杂的伤害类型系统、多重护甲抗性、暴击分层、特殊效果等
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

    /** 伤害标签系统（用于条件判断、元素反应、特殊机制） */
    val tags = mutableSetOf<String>()

    // ================= [ 多类型伤害桶系统 ] =================

    /** 伤害桶：类型 -> 伤害数值 (默认包含事件原始物理伤害) */
    private val damageBuckets = mutableMapOf<String, Double>("PHYSICAL" to originalDamage)

    /** 伤害倍率桶：类型 -> 倍率（独立倍增器，应用在最终结算前） */
    private val damageMultiplierBuckets = mutableMapOf<String, Double>()

    /** 抗性桶：类型 -> 抗性百分比 (0-100) */
    private val resistanceBuckets = mutableMapOf<String, Double>()

    /** 伤害减免桶：类型 -> 固定减免值（应用在百分比抗性之后） */
    private val flatReductionBuckets = mutableMapOf<String, Double>()

    // ================= [ 暴击系统 ] =================
    var critTier = 0
    private var critMultiplierBonus = 0.0
    private var critResistance = 0.0
    private var critResilience = 0.0

    // ================= [ 防御系统（护甲 + 法术抗性） ] =================
    private var physicalDefenseScore = 0.0
    private var magicalDefenseScore = 0.0
    private var fixedPenetration = 0.0
    private var percentPenetration = 0.0
    private var magicalPenetration = 0.0

    // ================= [ 伤害减免系统 ] =================
    /** 通用伤害减免百分比（最终计算前应用，类似于减伤光环） */
    private var universalDamageReduction = 0.0
    /** 通用固定伤害减免 */
    private var universalFlatReduction = 0.0

    // ================= [ 特殊效果标记 ] =================
    /** 是否无法暴击 */
    var cannotCrit = false
    /** 是否无法被抵挡/格挡 */
    var cannotBlock = false
    /** 是否击穿护甲 */
    var armorPiercing = false

    // ================= [ 方法：基础操作 ] =================

    fun setDamageMultiplier(multiplier: Double) { 
        damageMultiplier *= multiplier 
    }
    
    fun addTag(tag: String) = tags.add(tag.uppercase())
    
    fun hasTag(tag: String) = tags.contains(tag.uppercase())
    
    fun removeTag(tag: String) = tags.remove(tag.uppercase())

    // ================= [ 方法：伤害桶操作 ] =================

    /** 向指定类型的桶增加伤害 (如 PHYSICAL, FIRE, WATER, DARK, LIGHT) */
    fun addBucketDamage(type: String, amount: Double) {
        val t = type.uppercase()
        damageBuckets[t] = damageBuckets.getOrDefault(t, 0.0) + amount
    }

    /** 设置指定类型的伤害倍率 */
    fun setBucketMultiplier(type: String, multiplier: Double) {
        val t = type.uppercase()
        damageMultiplierBuckets[t] = multiplier
    }

    /** 添加指定类型伤害倍率 */
    fun addBucketMultiplier(type: String, multiplier: Double) {
        val t = type.uppercase()
        damageMultiplierBuckets[t] = damageMultiplierBuckets.getOrDefault(t, 1.0) * multiplier
    }

    /** 增加指定类型的抗性 (百分比) */
    fun addBucketResistance(type: String, percent: Double) {
        val t = type.uppercase()
        resistanceBuckets[t] = resistanceBuckets.getOrDefault(t, 0.0) + percent
    }

    /** 增加指定类型的固定伤害减免 */
    fun addBucketFlatReduction(type: String, amount: Double) {
        val t = type.uppercase()
        flatReductionBuckets[t] = flatReductionBuckets.getOrDefault(t, 0.0) + amount
    }

    // ================= [ 快捷方法 ] =================
    
    // 默认加物理伤害
    fun addDamage(amount: Double) {
        taboolib.common.platform.function.console().sendMessage("§e[AC-DEBUG] §fDamageData.addDamage: amount=$amount, PHYSICAL(before)=${damageBuckets.getOrDefault("PHYSICAL", 0.0)}")
        addBucketDamage("PHYSICAL", amount)
        taboolib.common.platform.function.console().sendMessage("§a[AC-DEBUG] §fDamageData.addDamage: PHYSICAL(after)=${damageBuckets.getOrDefault("PHYSICAL", 0.0)}")
    }
    
    fun addDirectReductionPercent(percent: Double) = addBucketResistance("PHYSICAL", percent)
    
    fun addPhysicalDamage(amount: Double) = addBucketDamage("PHYSICAL", amount)
    
    fun addElementalDamage(type: String, amount: Double) = addBucketDamage(type, amount)

    // ================= [ 暴击系统 ] =================
    
    fun addCritDamage(value: Double) { 
        if (!cannotCrit) critMultiplierBonus += (value / 100.0) 
    }
    
    fun addCritResistance(value: Double) { 
        critResistance += value 
    }
    
    fun addCritResilience(value: Double) { 
        critResilience += (value / 100.0) 
    }

    fun rollCrit(totalChance: Double) {
        if (cannotCrit) {
            this.critTier = 0
            return
        }
        
        val actualChance = (totalChance - critResistance).coerceAtLeast(0.0)
        val baseTier = (actualChance / 100).toInt()
        val probability = actualChance % 100
        val random = ThreadLocalRandom.current().nextDouble(100.0)
        this.critTier = if (random < probability) baseTier + 1 else baseTier
    }

    // ================= [ 防御系统 ] =================
    
    fun addPhysicalDefense(amount: Double) { 
        physicalDefenseScore += amount 
    }
    
    fun addMagicalDefense(amount: Double) { 
        magicalDefenseScore += amount 
    }
    
    fun addDefenseScore(amount: Double) { 
        physicalDefenseScore += amount 
    }
    
    fun addFixedPenetration(amount: Double) { 
        fixedPenetration += amount 
    }
    
    fun addPercentPenetration(percent: Double) { 
        percentPenetration += percent 
    }
    
    fun addMagicalPenetration(amount: Double) { 
        magicalPenetration += amount 
    }

    // ================= [ 伤害减免系统 ] =================
    
    fun addUniversalReduction(percent: Double) {
        universalDamageReduction += percent
    }
    
    fun addUniversalFlatReduction(amount: Double) {
        universalFlatReduction += amount
    }

    // ================= [ 核心计算：多桶独立结算 ] =================

    /**
     * 核心计算逻辑：计算最终伤害
     * 流程：
     * 1. 物理伤害 -> 应用护甲减伤
     * 2. 元素伤害 -> 应用元素抗性
     * 3. 所有伤害 -> 应用百分比抗性、固定减免、通用减伤
     * 4. 暴击倍增
     * 5. 全局倍率
     */
    fun getFinalDamage(): Double {
        var totalBucketDamage = 0.0

        // 1. 遍历所有伤害桶独立结算
        damageBuckets.forEach { (type, rawDamage) ->
            var bucketDamage = rawDamage

            // 应用该桶的倍率（独立倍增器）
            val bucketMultiplier = damageMultiplierBuckets.getOrDefault(type, 1.0)
            bucketDamage *= bucketMultiplier

            // 物理伤害应用护甲减免
            if (type == "PHYSICAL") {
                bucketDamage = calculatePhysicalReduction(bucketDamage)
            }
            // 元素伤害应用元素抗性
            else {
                bucketDamage = calculateElementalReduction(type, bucketDamage)
            }

            // 应用该类型独立抗性
            val res = resistanceBuckets.getOrDefault(type, 0.0)
            bucketDamage *= (1.0 - res / 100.0).coerceIn(0.0, 1.0)

            // 应用该类型固定减免
            val flatRed = flatReductionBuckets.getOrDefault(type, 0.0)
            bucketDamage = (bucketDamage - flatRed).coerceAtLeast(0.0)

            totalBucketDamage += bucketDamage
        }

        // 2. 应用通用伤害减免（全局）
        totalBucketDamage *= (1.0 - universalDamageReduction / 100.0).coerceIn(0.0, 1.0)
        totalBucketDamage = (totalBucketDamage - universalFlatReduction).coerceAtLeast(0.0)

        // 3. 计算暴击倍率
        val baseCritMult = CoreConfig.defaultCritMultiplier
        val totalCritMultAttr = (baseCritMult + critMultiplierBonus - critResilience).coerceAtLeast(1.1)
        val finalCritMultiplier = if (critTier > 0) 1.0 + critTier * (totalCritMultAttr - 1.0) else 1.0

        // 4. 最终汇总
        return (totalBucketDamage * finalCritMultiplier * damageMultiplier).coerceAtLeast(0.0)
    }

    /**
     * 计算物理伤害的护甲减伤
     * K公式: damage * k / (armor + k)
     */
    private fun calculatePhysicalReduction(baseDamage: Double): Double {
        if (armorPiercing) return baseDamage

        val afterPercentArmor = physicalDefenseScore * (1.0 - (percentPenetration / 100.0).coerceIn(0.0, 1.0))
        val effectiveDefense = (afterPercentArmor - fixedPenetration).coerceAtLeast(0.0)
        val k = CoreConfig.armorK
        val armorMultiplier = k / (effectiveDefense + k)
        return baseDamage * armorMultiplier
    }

    /**
     * 计算元素伤害的魔法抗性
     */
    private fun calculateElementalReduction(type: String, baseDamage: Double): Double {
        // 目前元素伤害未应用魔法抗性，但预留接口供后续扩展
        val magicalResistance = 0.0
        return baseDamage * (1.0 - magicalResistance / 100.0).coerceIn(0.0, 1.0)
    }

    // ================= [ 调试方法 ] =================

    fun getDamageBuckets(): Map<String, Double> = damageBuckets.toMap()
    
    fun getResistanceBuckets(): Map<String, Double> = resistanceBuckets.toMap()
    
    fun getDefenseStats(): Map<String, Double> = mapOf(
        "physical_defense" to physicalDefenseScore,
        "magical_defense" to magicalDefenseScore,
        "fixed_penetration" to fixedPenetration,
        "percent_penetration" to percentPenetration
    )
}