package com.attributecore.data

import com.attributecore.event.CoreConfig
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageByEntityEvent
import java.util.concurrent.ThreadLocalRandom

/**
 * 伤害计算上下文
 * 核心逻辑：基础加伤 -> 穿甲/护甲过滤 -> 阶梯暴击 -> 最终倍率
 */
class DamageData(
    val attacker: LivingEntity,
    val defender: LivingEntity,
    val event: EntityDamageByEntityEvent
) {
    /** 原始伤害（Bukkit事件的基础伤害） */
    val originalDamage = event.damage

    /** 基础伤害增加值（固定值加成） */
    private var damageBonus = 0.0

    /** 最终伤害倍率（全局百分比加成） */
    private var damageMultiplier = 1.0

    /** 伤害标签系统（用于元素反应和定向防御） */
    val tags = mutableSetOf<String>()

    // ================= [ 暴击系统 (Warframe 风格) ] =================

    /** 暴击等级：0=无, 1=黄, 2=橙, 3=红, 4+=高阶红 */
    var critTier = 0

    /** 额外暴伤百分比 (例如 50.0 代表 +50% 暴伤) */
    private var critMultiplierBonus = 0.0

    /** 抗暴率（直接扣除攻击方的暴击几率） */
    private var critResistance = 0.0

    /** 暴击抗性（降低每一级暴击带来的收益百分比） */
    private var critResilience = 0.0

    // ================= [ 防御与穿甲系统 ] =================

    /** 护甲值（Defense Score） */
    private var totalDefenseScore = 0.0

    /** 直接减伤百分比 (0-100) */
    private var directReductionPercent = 0.0

    /** 固定穿甲（直接忽略 X 点护甲） */
    private var fixedPenetration = 0.0

    /** 百分比穿甲（忽略 X% 的护甲） */
    private var percentPenetration = 0.0

    // ================= [ 逻辑方法 ] =================

    fun addDamage(amount: Double) { damageBonus += amount }
    fun setDamageMultiplier(multiplier: Double) { damageMultiplier *= multiplier }

    fun addTag(tag: String) = tags.add(tag.uppercase())
    fun hasTag(tag: String): Boolean = tags.contains(tag.uppercase())

    // 暴击相关
    fun addCritDamage(value: Double) { critMultiplierBonus += (value / 100.0) }
    fun addCritResistance(value: Double) { critResistance += value }
    fun addCritResilience(value: Double) { critResilience += (value / 100.0) }

    // 防御相关
    fun addDefenseScore(amount: Double) { totalDefenseScore += amount }
    fun addDirectReductionPercent(percent: Double) { directReductionPercent += percent }

    // 穿甲相关
    fun addFixedPenetration(amount: Double) { fixedPenetration += amount }
    fun addPercentPenetration(percent: Double) { percentPenetration += percent }

    /**
     * Warframe 阶梯暴击判定逻辑
     * @param totalChance 攻击方总暴击率 (允许超过 100%)
     */
    fun rollCrit(totalChance: Double) {
        // 1. 实际暴击率 = 攻击几率 - 防御抗性
        val actualChance = (totalChance - critResistance).coerceAtLeast(0.0)

        // 2. 计算暴击层级
        // 例如: 250% -> 基础 2 层 (橙暴)，50% 几率升至 3 层 (红暴)
        val baseTier = (actualChance / 100).toInt()
        val probability = actualChance % 100

        val random = ThreadLocalRandom.current().nextDouble(100.0)
        this.critTier = if (random < probability) baseTier + 1 else baseTier
    }

    /**
     * 核心计算公式：汇总所有属性得出最终伤害
     */
    fun getFinalDamage(): Double {
        // 1. 基础攻击力汇总
        val rawTotalDamage = originalDamage + damageBonus

        // 2. 计算有效护甲 (计算顺序：先算百分比穿甲，再减固定穿甲)
        val afterPercentArmor = totalDefenseScore * (1.0 - (percentPenetration / 100.0).coerceIn(0.0, 1.0))
        val effectiveDefense = (afterPercentArmor - fixedPenetration).coerceAtLeast(0.0)

        // 3. 计算护甲减伤倍率 (K / (Def + K))
        val k = CoreConfig.armorK
        val armorMultiplier = k / (effectiveDefense + k)

        // 4. 计算直接百分比抗性
        val resistMultiplier = (1.0 - (directReductionPercent / 100.0)).coerceAtLeast(0.0)

        // 5. 计算阶梯暴击倍率
        // 公式：最终暴伤 = 1 + 层级 * (总暴伤倍率 - 1)
        val baseCritMult = CoreConfig.defaultCritMultiplier // 来自 config.yml，通常为 2.0
        val totalCritMultAttr = (baseCritMult + critMultiplierBonus - critResilience).coerceAtLeast(1.1)

        val finalCritMultiplier = if (critTier > 0) {
            1.0 + critTier * (totalCritMultAttr - 1.0)
        } else {
            1.0
        }

        // 6. 最终加乘：(基础总伤) * 护甲系数 * 抗性系数 * 暴击系数 * 全局倍率
        return (rawTotalDamage * armorMultiplier * resistMultiplier * finalCritMultiplier * damageMultiplier).coerceAtLeast(0.0)
    }
}