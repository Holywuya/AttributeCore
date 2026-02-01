package com.attributecore.data

import java.util.concurrent.ConcurrentHashMap

data class AttributeData(
    private val flatValues: MutableMap<String, Double> = ConcurrentHashMap(),
    private val percentValues: MutableMap<String, Double> = ConcurrentHashMap()
) {
    var combatPower: Double = 0.0
        private set
    
    @Volatile
    private var cachedResistances: Map<String, Double>? = null
    @Volatile
    private var resistanceCacheValid = false

    operator fun get(key: String): Double = flatValues[key] ?: 0.0

    operator fun set(key: String, value: Double) {
        flatValues[key] = value
        invalidateResistanceCache(key)
    }

    fun add(key: String, value: Double) {
        flatValues[key] = (flatValues[key] ?: 0.0) + value
        invalidateResistanceCache(key)
    }

    fun getPercent(key: String): Double = percentValues[key] ?: 0.0

    fun setPercent(key: String, value: Double) {
        percentValues[key] = value
        invalidateResistanceCache(key)
    }

    fun addPercent(key: String, value: Double) {
        percentValues[key] = (percentValues[key] ?: 0.0) + value
        invalidateResistanceCache(key)
    }
    
    private fun invalidateResistanceCache(key: String) {
        if (key.endsWith("_resistance")) {
            resistanceCacheValid = false
            cachedResistances = null
        }
    }

    fun getFinal(key: String): Double {
        val flat = flatValues[key] ?: 0.0
        val percent = percentValues[key] ?: 0.0
        return flat * (1.0 + percent / 100.0)
    }

    fun add(other: AttributeData) {
        other.flatValues.forEach { (key, value) ->
            add(key, value)
        }
        other.percentValues.forEach { (key, value) ->
            addPercent(key, value)
        }
        combatPower += other.combatPower
    }

    fun subtract(other: AttributeData) {
        other.flatValues.forEach { (key, value) ->
            add(key, -value)
        }
        other.percentValues.forEach { (key, value) ->
            addPercent(key, -value)
        }
        combatPower -= other.combatPower
    }

    fun clear() {
        flatValues.clear()
        percentValues.clear()
        combatPower = 0.0
        resistanceCacheValid = false
        cachedResistances = null
    }

    fun isValid(): Boolean {
        return flatValues.values.any { it != 0.0 } || 
               percentValues.values.any { it != 0.0 } || 
               combatPower != 0.0
    }

    fun getAll(): Map<String, Double> = flatValues.toMap()

    fun getAllPercent(): Map<String, Double> = percentValues.toMap()

    fun getAllFinal(): Map<String, Double> {
        val keys = flatValues.keys + percentValues.keys
        return keys.associateWith { getFinal(it) }
    }

    fun getNonZeroAttributes(): Map<String, Double> {
        // 只返回已注册的属性中非零的值，过滤掉未注册的属性（如旧版本中已删除的属性）
        val registeredNames = SubAttribute.getAttributes().map { it.name }.toSet()
        return flatValues.filterKeys { it in registeredNames }
            .filterValues { it != 0.0 }
    }

    fun calculateCombatPower(weights: Map<String, Double> = emptyMap()): Double {
        val allKeys = flatValues.keys + percentValues.keys
        combatPower = allKeys.sumOf { key ->
            val finalValue = getFinal(key)
            val weight = weights[key] ?: 1.0
            finalValue * weight
        }
        return combatPower
    }

    fun correct(ranges: Map<String, ClosedFloatingPointRange<Double>> = emptyMap()) {
        flatValues.replaceAll { key, value ->
            val range = ranges[key]
            when {
                range != null -> value.coerceIn(range)
                else -> value.coerceAtLeast(0.0)
            }
        }
    }

    fun getResistance(element: String): Double {
        return getFinal(Elements.resistanceKey(element))
    }

    fun getAllResistances(): Map<String, Double> {
        if (resistanceCacheValid && cachedResistances != null) {
            return cachedResistances!!
        }
        
        val resistanceKeys = flatValues.keys.filter { it.endsWith("_resistance") } +
                             percentValues.keys.filter { it.endsWith("_resistance") }
        
        val result = resistanceKeys.distinct().associate { key ->
            val element = key.removeSuffix("_resistance").uppercase()
            element to getFinal(key)
        }
        
        cachedResistances = result
        resistanceCacheValid = true
        return result
    }
    
    fun getPenetration(element: String): Double {
        return getFinal(Elements.penetrationKey(element))
    }
    
    fun getAllPenetrations(): Map<String, Double> {
        val penetrationKeys = flatValues.keys.filter { it.endsWith("_penetration") } +
                              percentValues.keys.filter { it.endsWith("_penetration") }
        
        return penetrationKeys.distinct().associate { key ->
            val element = key.removeSuffix("_penetration").uppercase()
            element to getFinal(key)
        }
    }

    fun buildDamageBucket(): DamageBucket {
        return DamageBucket.fromAttributeData(this)
    }

    override fun toString(): String {
        val flatAttrs = flatValues.filterValues { it != 0.0 }
            .entries.joinToString(", ") { "${it.key}=${it.value}" }
        val percentAttrs = percentValues.filterValues { it != 0.0 }
            .entries.joinToString(", ") { "${it.key}%=${it.value}" }
        return "AttributeData(cp=$combatPower, flat=[$flatAttrs], percent=[$percentAttrs])"
    }
}
