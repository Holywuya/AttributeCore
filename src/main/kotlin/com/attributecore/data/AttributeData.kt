package com.attributecore.data

import java.util.concurrent.ConcurrentHashMap

data class AttributeData(
    private val flatValues: MutableMap<String, Double> = ConcurrentHashMap(),
    private val percentValues: MutableMap<String, Double> = ConcurrentHashMap()
) {
    var combatPower: Double = 0.0
        private set

    operator fun get(key: String): Double = flatValues[key] ?: 0.0

    operator fun set(key: String, value: Double) {
        flatValues[key] = value
    }

    fun add(key: String, value: Double) {
        flatValues[key] = (flatValues[key] ?: 0.0) + value
    }

    fun getPercent(key: String): Double = percentValues[key] ?: 0.0

    fun setPercent(key: String, value: Double) {
        percentValues[key] = value
    }

    fun addPercent(key: String, value: Double) {
        percentValues[key] = (percentValues[key] ?: 0.0) + value
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
        return flatValues.filterValues { it != 0.0 }
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

    fun getResistance(element: Element): Double {
        return getFinal(element.resistanceKey())
    }

    fun getAllResistances(): Map<Element, Double> {
        return Element.entries.associateWith { getResistance(it) }
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
