package com.attributecore.data

import org.bukkit.entity.LivingEntity
import taboolib.common.platform.function.submit
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class AuraInstance(
    val element: Element,
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

    fun applyAura(entity: LivingEntity, element: Element, gauge: Double = DEFAULT_GAUGE) {
        if (element == Element.PHYSICAL) return

        val entityAuras = auras.computeIfAbsent(entity.uniqueId) { mutableListOf() }

        val existing = entityAuras.find { it.element == element }
        if (existing != null) {
            existing.gauge += gauge
            existing.createdAt = System.currentTimeMillis()
        } else {
            entityAuras.add(AuraInstance(element, gauge))
        }

        ensureDecayTaskRunning()
    }

    fun getAura(entity: LivingEntity): AuraInstance? {
        return auras[entity.uniqueId]?.maxByOrNull { it.gauge }
    }

    fun getAuras(entity: LivingEntity): List<AuraInstance> {
        return auras[entity.uniqueId]?.toList() ?: emptyList()
    }

    fun hasAura(entity: LivingEntity, element: Element): Boolean {
        return auras[entity.uniqueId]?.any { it.element == element } == true
    }

    fun consumeAura(entity: LivingEntity, element: Element, amount: Double = 1.0): Boolean {
        val entityAuras = auras[entity.uniqueId] ?: return false
        val aura = entityAuras.find { it.element == element } ?: return false

        aura.gauge -= amount
        if (aura.gauge <= 0) {
            entityAuras.remove(aura)
            if (entityAuras.isEmpty()) {
                auras.remove(entity.uniqueId)
            }
        }
        return true
    }

    fun clearAura(entity: LivingEntity, element: Element? = null) {
        if (element == null) {
            auras.remove(entity.uniqueId)
        } else {
            auras[entity.uniqueId]?.removeIf { it.element == element }
        }
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
                        val decayRate = aura.element.getDecayRate() / 20.0
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
