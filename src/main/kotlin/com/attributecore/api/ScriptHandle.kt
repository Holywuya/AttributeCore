package com.attributecore.api

import com.attributecore.data.DamageData
import taboolib.common5.Coerce

/**
 * 脚本操作句柄 - 对应脚本参数中的 "handle"
 */
class ScriptHandle(val data: DamageData?, val value: Double = 0.0) {

    /** 获取当前属性的数值 */
    fun getValue(): Double = value

    /** 增加物理伤害数值 */
    fun addDamage(amount: Any) = data?.addDamage(Coerce.toDouble(amount))

    /** 设置伤害倍率 (1.5 = +50%) */
    fun setMultiplier(multiplier: Any) = data?.setDamageMultiplier(Coerce.toDouble(multiplier))

    /** 设置暴击层级 (1=黄, 2=橙, 3=红) */
    fun setCritTier(tier: Any) { data?.critTier = Coerce.toInteger(tier) }

    /** 增加特定类型的伤害桶 (如 FIRE) */
    fun addBucketDamage(type: String, amount: Any) = data?.addBucketDamage(type, Coerce.toDouble(amount))

    /** 设置特定类型的抗性百分比 */
    fun setResistance(type: String, percent: Any) = data?.addBucketResistance(type, Coerce.toDouble(percent))

    /** 增加固定穿甲 */
    fun addFixedPen(amount: Any) = data?.addFixedPenetration(Coerce.toDouble(amount))

    /** 增加百分比穿甲 */
    fun addPercentPen(percent: Any) = data?.addPercentPenetration(Coerce.toDouble(percent))

    /** 注入伤害标签 */
    fun addTag(tag: String) = data?.addTag(tag)
}