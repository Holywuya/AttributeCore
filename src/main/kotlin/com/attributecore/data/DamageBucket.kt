package com.attributecore.data

import java.util.concurrent.ConcurrentHashMap

data class DamageBucket(
    private val damages: MutableMap<String, Double> = ConcurrentHashMap()
) {
    operator fun get(element: String): Double = damages[Elements.normalize(element)] ?: 0.0

    @Deprecated("Use get(String) instead")
    operator fun get(element: Element): Double = get(element.name)

    operator fun set(element: String, value: Double) {
        val normalized = Elements.normalize(element)
        if (value > 0) {
            damages[normalized] = value
        } else {
            damages.remove(normalized)
        }
    }

    @Deprecated("Use set(String, value) instead")
    operator fun set(element: Element, value: Double) {
        set(element.name, value)
    }

    fun add(element: String, value: Double) {
        if (value != 0.0) {
            val normalized = Elements.normalize(element)
            damages[normalized] = (damages[normalized] ?: 0.0) + value
        }
    }

    @Deprecated("Use add(String, value) instead")
    fun add(element: Element, value: Double) {
        add(element.name, value)
    }

    fun multiply(element: String, multiplier: Double) {
        val normalized = Elements.normalize(element)
        damages[normalized]?.let {
            damages[normalized] = it * multiplier
        }
    }

    @Deprecated("Use multiply(String, multiplier) instead")
    fun multiply(element: Element, multiplier: Double) {
        multiply(element.name, multiplier)
    }

    fun multiplyAll(multiplier: Double) {
        damages.replaceAll { _, v -> v * multiplier }
    }

    fun total(): Double = damages.values.sum()

    fun elements(): Set<String> = damages.keys.toSet()

    fun elementalDamage(): Double = damages.entries
        .filter { !Elements.isPhysical(it.key) }
        .sumOf { it.value }

    fun hasElement(element: String): Boolean = (damages[Elements.normalize(element)] ?: 0.0) > 0

    @Deprecated("Use hasElement(String) instead")
    fun hasElement(element: Element): Boolean = hasElement(element.name)

    fun hasElementalDamage(): Boolean = damages.keys.any { !Elements.isPhysical(it) }

    fun clone(): DamageBucket = DamageBucket(ConcurrentHashMap(damages))

    fun clear() {
        damages.clear()
    }

    fun toMap(): Map<String, Double> = damages.toMap()

    fun applyResistances(resistances: Map<String, Double>, baseValue: Double = 100.0) {
        applyResistances(resistances, emptyMap(), baseValue)
    }
    
    fun applyResistances(
        resistances: Map<String, Double>,
        penetrations: Map<String, Double>,
        baseValue: Double = 100.0
    ) {
        damages.replaceAll { element, damage ->
            val resistance = resistances[element] ?: resistances[element.lowercase()] ?: 0.0
            val penetration = penetrations[element] ?: penetrations[element.lowercase()] ?: 0.0
            val penetrationPercent = penetration.coerceIn(0.0, 100.0) / 100.0
            val effectiveResistance = resistance * (1.0 - penetrationPercent)
            val maxResistance = Elements.getMaxResistance(element)
            val clampedResistance = effectiveResistance.coerceIn(0.0, maxResistance)
            val reduction = clampedResistance / (clampedResistance + baseValue)
            damage * (1 - reduction)
        }
    }

    fun merge(other: DamageBucket) {
        other.damages.forEach { (element, value) ->
            add(element, value)
        }
    }

    override fun toString(): String {
        val parts = damages.entries
            .filter { it.value > 0 }
            .joinToString(", ") { "${Elements.getDisplayName(it.key)}=${String.format("%.2f", it.value)}" }
        return "DamageBucket(total=${String.format("%.2f", total())}, $parts)"
    }

    companion object {
        fun physical(damage: Double): DamageBucket {
            return DamageBucket().apply {
                this[Elements.PHYSICAL] = damage
            }
        }
        
        fun elemental(element: String, damage: Double): DamageBucket {
            return DamageBucket().apply {
                this[Elements.normalize(element)] = damage
            }
        }
        
        fun fromWeaponElement(attackDamage: Double, weaponElement: String): DamageBucket {
            val bucket = DamageBucket()
            val normalizedElement = Elements.normalize(weaponElement)
            bucket[normalizedElement] = attackDamage.coerceAtLeast(0.0)
            return bucket
        }

        fun fromAttributeData(data: AttributeData): DamageBucket {
            val bucket = DamageBucket()
            
            val attackDamage = data.getFinal("攻击力")
            if (attackDamage > 0) {
                bucket.add(Elements.PHYSICAL, attackDamage)
            }
            
            return bucket
        }
    }
}
