package com.attributecore.data

import java.util.concurrent.ConcurrentHashMap

data class AttributeData(
    private val values: MutableMap<String, Double> = ConcurrentHashMap()
) {
    var combatPower: Double = 0.0
        private set

    operator fun get(key: String): Double {
        return values[key] ?: 0.0
    }

    operator fun set(key: String, value: Double) {
        values[key] = value
    }

    fun add(key: String, value: Double) {
        values[key] = (values[key] ?: 0.0) + value
    }

    fun add(other: AttributeData) {
        other.values.forEach { (key, value) ->
            add(key, value)
        }
        combatPower += other.combatPower
    }

    fun subtract(other: AttributeData) {
        other.values.forEach { (key, value) ->
            add(key, -value)
        }
        combatPower -= other.combatPower
    }

    fun clear() {
        values.clear()
        combatPower = 0.0
    }

    fun isValid(): Boolean {
        return values.values.any { it != 0.0 } || combatPower != 0.0
    }

    fun getAll(): Map<String, Double> {
        return values.toMap()
    }

    fun getNonZeroAttributes(): Map<String, Double> {
        return values.filterValues { it != 0.0 }
    }

    fun calculateCombatPower(weights: Map<String, Double> = emptyMap()): Double {
        combatPower = values.entries.sumOf { (key, value) ->
            val weight = weights[key] ?: 1.0
            value * weight
        }
        return combatPower
    }

    fun correct(ranges: Map<String, DoubleRange> = emptyMap()) {
        values.replaceAll { key, value ->
            val range = ranges[key]
            when {
                range != null -> value.coerceIn(range)
                else -> value.coerceAtLeast(0.0)
            }
        }
    }

    override fun toString(): String {
        val attrs = getNonZeroAttributes().entries.joinToString(", ") { (k, v) ->
            "$k=$v"
        }
        return "AttributeData(combatPower=$combatPower, $attrs)"
    }
}
