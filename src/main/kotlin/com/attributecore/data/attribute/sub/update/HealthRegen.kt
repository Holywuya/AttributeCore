package com.attributecore.data.attribute.sub.update

import com.attributecore.data.attribute.AttributeType
import com.attributecore.data.attribute.SubAttribute
import com.attributecore.data.eventdata.EventData
import com.attributecore.data.eventdata.sub.UpdateData
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.LivingEntity

class HealthRegen : SubAttribute("HealthRegen", 2, AttributeType.UPDATE) {

    private val lastHealTime = mutableMapOf<java.util.UUID, Long>()

    override fun defaultConfig(config: YamlConfiguration): YamlConfiguration {
        config.set("HealthRegen.DiscernName", "生命恢复")
        config.set("HealthRegen.CombatPower", 3)
        config.set("RegenInterval", 5.0)
        return config
    }

    override fun eventMethod(values: DoubleArray, eventData: EventData) {
        if (eventData is UpdateData) {
            val entity = eventData.getEntity()
            val regenAmount = values[0]
            val interval = values[1].coerceAtLeast(1.0)
            
            val now = System.currentTimeMillis()
            val lastTime = lastHealTime.getOrDefault(entity.uniqueId, 0)
            
            if (now - lastTime >= interval * 1000) {
                val maxHealth = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
                if (entity.health < maxHealth) {
                    entity.health = (entity.health + regenAmount).coerceAtMost(maxHealth)
                    lastHealTime[entity.uniqueId] = now
                    
                    if (regenAmount > 1) {
                        entity.world.spawnParticle(Particle.HEART, entity.location.add(0.0, 0.5, 0.0), 2, 0.3, 0.3, 0.3)
                    }
                }
            }
        }
    }

    override fun getPlaceholder(values: DoubleArray, entity: LivingEntity, placeholder: String): Any? {
        return when (placeholder) {
            "HealthRegen" -> values[0]
            "RegenInterval" -> values[1]
            else -> null
        }
    }

    override fun getPlaceholders(): List<String> = listOf("HealthRegen", "RegenInterval")

    override fun loadAttribute(values: DoubleArray, lore: String) {
        when {
            lore.contains(getString("HealthRegen.DiscernName")) -> values[0] += getNumber(lore)
        }
    }

    override fun correct(values: DoubleArray) {
        values[0] = values[0].coerceAtLeast(0.0)
        values[1] = values[1].coerceAtLeast(1.0)
    }

    override fun calculationCombatPower(values: DoubleArray): Double {
        return values[0] * getInt("HealthRegen.CombatPower")
    }
}