package com.attributecore.api

import com.attributecore.data.DamageData
import taboolib.common5.Coerce

class ScriptHandle(val data: DamageData?, val value: Double = 0.0) {

    fun addDamage(amount: Any) = data?.addDamage(Coerce.toDouble(amount))

    fun setMultiplier(multiplier: Any) = data?.setDamageMultiplier(Coerce.toDouble(multiplier))

    fun setCritTier(tier: Any) { data?.critTier = Coerce.toInteger(tier) }

    fun addBucketDamage(type: String, amount: Any) = data?.addBucketDamage(type, Coerce.toDouble(amount))

    fun setResistance(type: String, percent: Any) = data?.addBucketResistance(type, Coerce.toDouble(percent))

    fun addFixedPen(amount: Any) = data?.addFixedPenetration(Coerce.toDouble(amount))

    fun addPercentPen(percent: Any) = data?.addPercentPenetration(Coerce.toDouble(percent))

    fun addTag(tag: String) = data?.addTag(tag)
}