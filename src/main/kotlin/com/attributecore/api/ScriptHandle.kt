package com.attributecore.api

import com.attributecore.data.DamageData

class ScriptHandle(val data: DamageData?, private val _value: Double = 0.0) {

    fun getValue() = _value

    fun getDamageData() = data

    fun getFinalDamage() = data?.getFinalDamage() ?: 0.0

    fun getDamageBuckets() = data?.getDamageBuckets() ?: emptyMap<String, Double>()

    fun getDefenseStats() = data?.getDefenseStats() ?: emptyMap<String, Double>()
}
