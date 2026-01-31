package com.attributecore.data

import org.bukkit.entity.LivingEntity
import taboolib.common.platform.function.submit
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class AuraInstance(
    val element: String,
    var gauge: Double,
    var createdAt: Long = System.currentTimeMillis()
) {
    fun isExpired(maxDurationMs: Long): Boolean {
        return System.currentTimeMillis() - createdAt > maxDurationMs
    }

    fun decay(amount: Double): Boolean {
        gauge -= amount
        return gauge <= 0
    }
}

object ElementalAura {
    private val auras = ConcurrentHashMap<UUID, MutableList<AuraInstance>>()
    private var decayTaskRunning = false

    private const val DEFAULT_GAUGE = 1.0
    private const val DEFAULT_DURATION_MS = 12000L
    private const val DECAY_INTERVAL_TICKS = 20L

    fun applyAura(entity: LivingEntity, element: String, gauge: Double = DEFAULT_GAUGE) {
        val normalizedElement = Elements.normalize(element)
        if (Elements.isPhysical(normalizedElement)) return

        val entityAuras = auras.computeIfAbsent(entity.uniqueId) { mutableListOf() }

        val existing = entityAuras.find { it.element == normalizedElement }
        if (existing != null) {
            existing.gauge += gauge
            existing.createdAt = System.currentTimeMillis()
        } else {
            entityAuras.add(AuraInstance(normalizedElement, gauge))
        }

        ensureDecayTaskRunning()
    }

    @Deprecated("Use applyAura(entity, String, gauge) instead")
    fun applyAura(entity: LivingEntity, element: Element, gauge: Double = DEFAULT_GAUGE) {
        applyAura(entity, element.name, gauge)
    }

    fun getAura(entity: LivingEntity): AuraInstance? {
        return auras[entity.uniqueId]?.maxByOrNull { it.gauge }
    }

    fun getAuras(entity: LivingEntity): List<AuraInstance> {
        return auras[entity.uniqueId]?.toList() ?: emptyList()
    }

    fun hasAura(entity: LivingEntity, element: String): Boolean {
        val normalizedElement = Elements.normalize(element)
        return auras[entity.uniqueId]?.any { it.element == normalizedElement } == true
    }

    @Deprecated("Use hasAura(entity, String) instead")
    fun hasAura(entity: LivingEntity, element: Element): Boolean {
        return hasAura(entity, element.name)
    }

    fun consumeAura(entity: LivingEntity, element: String, amount: Double = 1.0): Boolean {
        val normalizedElement = Elements.normalize(element)
        val entityAuras = auras[entity.uniqueId] ?: return false
        val aura = entityAuras.find { it.element == normalizedElement } ?: return false

        aura.gauge -= amount
        if (aura.gauge <= 0) {
            entityAuras.remove(aura)
            if (entityAuras.isEmpty()) {
                auras.remove(entity.uniqueId)
            }
        }
        return true
    }

    @Deprecated("Use consumeAura(entity, String, amount) instead")
    fun consumeAura(entity: LivingEntity, element: Element, amount: Double = 1.0): Boolean {
        return consumeAura(entity, element.name, amount)
    }

    fun clearAura(entity: LivingEntity, element: String? = null) {
        if (element == null) {
            auras.remove(entity.uniqueId)
        } else {
            val normalizedElement = Elements.normalize(element)
            auras[entity.uniqueId]?.removeIf { it.element == normalizedElement }
        }
    }

    @Deprecated("Use clearAura(entity, String?) instead")
    fun clearAura(entity: LivingEntity, element: Element?) {
        clearAura(entity, element?.name)
    }

    fun clearAll() {
        auras.clear()
    }

    private fun ensureDecayTaskRunning() {
        if (decayTaskRunning) return
        decayTaskRunning = true

        submit(async = true, period = DECAY_INTERVAL_TICKS) {
            if (auras.isEmpty()) {
                decayTaskRunning = false
                cancel()
                return@submit
            }

            val iterator = auras.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val entityAuras = entry.value

                entityAuras.removeIf { aura ->
                    if (aura.isExpired(DEFAULT_DURATION_MS)) {
                        true
                    } else {
                        val decayRate = Elements.getDecayRate(aura.element) / 20.0
                        aura.decay(decayRate)
                    }
                }

                if (entityAuras.isEmpty()) {
                    iterator.remove()
                }
            }
        }
    }

    fun getActiveAuraCount(): Int = auras.values.sumOf { it.size }

    fun getAffectedEntityCount(): Int = auras.size
}
