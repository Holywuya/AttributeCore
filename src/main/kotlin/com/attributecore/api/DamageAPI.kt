package com.attributecore.api

import com.attributecore.data.DamageBucket
import com.attributecore.data.Elements
import org.bukkit.entity.LivingEntity
import java.util.function.Consumer

object DamageAPI {

    @JvmStatic
    fun createPhysicalBucket(damage: Double): DamageBucket {
        return DamageBucket.physical(damage)
    }

    @JvmStatic
    fun createElementalBucket(element: String, damage: Double): DamageBucket {
        return DamageBucket().apply { this[element] = damage }
    }

    @JvmStatic
    fun createMixedBucket(damages: Map<String, Double>): DamageBucket {
        val bucket = DamageBucket()
        damages.forEach { (element, damage) ->
            bucket.add(element, damage)
        }
        return bucket
    }

    @JvmStatic
    fun addDamage(bucket: DamageBucket, element: String, value: Double) {
        bucket.add(element, value)
    }

    @JvmStatic
    fun setDamage(bucket: DamageBucket, element: String, value: Double) {
        bucket[element] = value
    }

    @JvmStatic
    fun getDamage(bucket: DamageBucket, element: String): Double {
        return bucket[element]
    }

    @JvmStatic
    fun getTotalDamage(bucket: DamageBucket): Double {
        return bucket.total()
    }

    @JvmStatic
    fun getElementalDamage(bucket: DamageBucket): Double {
        return bucket.elementalDamage()
    }

    @JvmStatic
    fun getPhysicalDamage(bucket: DamageBucket): Double {
        return bucket[Elements.PHYSICAL]
    }

    @JvmStatic
    fun multiplyDamage(bucket: DamageBucket, element: String, multiplier: Double) {
        bucket.multiply(element, multiplier)
    }

    @JvmStatic
    fun multiplyAllDamage(bucket: DamageBucket, multiplier: Double) {
        bucket.multiplyAll(multiplier)
    }

    @JvmStatic
    fun applyResistances(bucket: DamageBucket, victim: LivingEntity): DamageBucket {
        val resistances = AttributeCoreAPI.getResistances(victim)
        val result = bucket.clone()
        result.applyResistances(resistances)
        return result
    }

    @JvmStatic
    fun applyResistancesInPlace(bucket: DamageBucket, victim: LivingEntity) {
        val resistances = AttributeCoreAPI.getResistances(victim)
        bucket.applyResistances(resistances)
    }

    @JvmStatic
    fun applyResistances(bucket: DamageBucket, resistances: Map<String, Double>): DamageBucket {
        val result = bucket.clone()
        result.applyResistances(resistances)
        return result
    }

    @JvmStatic
    fun mergeBuckets(vararg buckets: DamageBucket): DamageBucket {
        val result = DamageBucket()
        buckets.forEach { result.merge(it) }
        return result
    }

    @JvmStatic
    fun cloneBucket(bucket: DamageBucket): DamageBucket {
        return bucket.clone()
    }

    @JvmStatic
    fun hasElement(bucket: DamageBucket, element: String): Boolean {
        return bucket.hasElement(element)
    }

    @JvmStatic
    fun hasElementalDamage(bucket: DamageBucket): Boolean {
        return bucket.hasElementalDamage()
    }

    @JvmStatic
    fun getElements(bucket: DamageBucket): Set<String> {
        return bucket.elements()
    }

    @JvmStatic
    fun forEachDamage(bucket: DamageBucket, consumer: Consumer<Map.Entry<String, Double>>) {
        bucket.toMap().entries.forEach { consumer.accept(it) }
    }

    @JvmStatic
    fun calculateDamageReduction(resistance: Double, baseValue: Double = 100.0): Double {
        val clamped = resistance.coerceAtLeast(0.0)
        return clamped / (clamped + baseValue)
    }

    @JvmStatic
    fun calculateEffectiveDamage(damage: Double, resistance: Double, baseValue: Double = 100.0): Double {
        val reduction = calculateDamageReduction(resistance, baseValue)
        return damage * (1 - reduction)
    }
}
