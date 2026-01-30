package com.attributecore.api

import com.attributecore.data.DamageData

/**
 * ScriptHandle - 为JavaScript脚本提供伤害数据操作API
 * 使用明确的方法签名以确保Nashorn可以正确调用
 */
open class ScriptHandle(val data: DamageData?, private val _value: Double = 0.0) {

    @JvmName("getValue")
    fun getValue() = _value

    @JvmName("getDamageData")
    fun getDamageData() = data

    @JvmName("getFinalDamage")
    fun getFinalDamage() = data?.getFinalDamage() ?: 0.0

    @JvmName("getDamageBuckets")
    fun getDamageBuckets() = data?.getDamageBuckets() ?: emptyMap<String, Double>()

    @JvmName("getDefenseStats")
    fun getDefenseStats() = data?.getDefenseStats() ?: emptyMap<String, Double>()
}