package com.attributecore.api

import com.attributecore.data.DamageData
import taboolib.common5.Coerce

class ScriptHandle(val data: DamageData?, private val _value: Double = 0.0) {

    // ===== [ 基础方法 ] =====
    fun getValue() = _value
    
    fun getDamageData() = data

    // ===== [ 基础伤害操作 ] =====
    fun addDamage(amount: Any) = data?.addDamage(Coerce.toDouble(amount))

    fun setMultiplier(multiplier: Any) = data?.setDamageMultiplier(Coerce.toDouble(multiplier))

    // ===== [ 多类型伤害桶操作 ] =====
    fun addBucketDamage(type: String, amount: Any) = data?.addBucketDamage(type, Coerce.toDouble(amount))

    fun setBucketMultiplier(type: String, multiplier: Any) = data?.setBucketMultiplier(type, Coerce.toDouble(multiplier))

    fun addBucketMultiplier(type: String, multiplier: Any) = data?.addBucketMultiplier(type, Coerce.toDouble(multiplier))

    fun setResistance(type: String, percent: Any) = data?.addBucketResistance(type, Coerce.toDouble(percent))

    fun addBucketResistance(type: String, percent: Any) = data?.addBucketResistance(type, Coerce.toDouble(percent))

    fun addBucketFlatReduction(type: String, amount: Any) = data?.addBucketFlatReduction(type, Coerce.toDouble(amount))

    // ===== [ 元素/特殊伤害 ] =====
    fun addPhysicalDamage(amount: Any) = data?.addPhysicalDamage(Coerce.toDouble(amount))

    fun addElementalDamage(type: String, amount: Any) = data?.addElementalDamage(type, Coerce.toDouble(amount))

    // ===== [ 暴击系统 ] =====
    fun addCritDamage(value: Any) = data?.addCritDamage(Coerce.toDouble(value))

    fun addCritResistance(value: Any) = data?.addCritResistance(Coerce.toDouble(value))

    fun addCritResilience(value: Any) = data?.addCritResilience(Coerce.toDouble(value))

    fun rollCrit(totalChance: Any) = data?.rollCrit(Coerce.toDouble(totalChance))

    fun setCritTier(tier: Any) { data?.critTier = Coerce.toInteger(tier) }

    // ===== [ 防御系统 ] =====
    fun addPhysicalDefense(amount: Any) = data?.addPhysicalDefense(Coerce.toDouble(amount))

    fun addMagicalDefense(amount: Any) = data?.addMagicalDefense(Coerce.toDouble(amount))

    fun addDefenseScore(amount: Any) = data?.addDefenseScore(Coerce.toDouble(amount))

    fun addFixedPenetration(amount: Any) = data?.addFixedPenetration(Coerce.toDouble(amount))

    fun addFixedPen(amount: Any) = data?.addFixedPenetration(Coerce.toDouble(amount))

    fun addPercentPenetration(percent: Any) = data?.addPercentPenetration(Coerce.toDouble(percent))

    fun addPercentPen(percent: Any) = data?.addPercentPenetration(Coerce.toDouble(percent))

    fun addMagicalPenetration(amount: Any) = data?.addMagicalPenetration(Coerce.toDouble(amount))

    // ===== [ 伤害减免系统 ] =====
    fun addUniversalReduction(percent: Any) = data?.addUniversalReduction(Coerce.toDouble(percent))

    fun addUniversalFlatReduction(amount: Any) = data?.addUniversalFlatReduction(Coerce.toDouble(amount))

    // ===== [ 特殊效果 ] =====
    fun addTag(tag: String) = data?.addTag(tag)

    // ===== [ 调试/计算 ] =====
    fun getFinalDamage() = data?.getFinalDamage() ?: 0.0

    fun getDamageBuckets() = data?.getDamageBuckets() ?: emptyMap<String, Double>()

    fun getDefenseStats() = data?.getDefenseStats() ?: emptyMap<String, Double>()
}