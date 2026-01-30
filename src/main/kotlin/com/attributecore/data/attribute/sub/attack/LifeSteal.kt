package com.attributecore.data.attribute.sub.attack

import com.attributecore.data.attribute.AttributeType
import com.attributecore.data.attribute.SubAttribute
import com.attributecore.data.eventdata.EventData
import com.attributecore.data.eventdata.sub.DamageData
import org.bukkit.Particle
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.LivingEntity

class LifeSteal : SubAttribute("LifeSteal", 1, AttributeType.ATTACK) {

    override fun defaultConfig(config: YamlConfiguration): YamlConfiguration {
        config.set("LifeSteal.DiscernName", "生命偷取")
        config.set("LifeSteal.CombatPower", 3)
        return config
    }

    override fun eventMethod(values: DoubleArray, eventData: EventData) {
        if (eventData is DamageData) {
            val stealRate = values[0] / 100
            val damage = eventData.getDamage()
            val healAmount = damage * stealRate
            
            val attacker = eventData.attacker
            val maxHealth = attacker.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
            attacker.health = (attacker.health + healAmount).coerceAtMost(maxHealth)
            
            attacker.world.spawnParticle(Particle.HEART, attacker.location.add(0.0, 1.0, 0.0), 3)
        }
    }

    override fun getPlaceholder(values: DoubleArray, entity: LivingEntity, placeholder: String): Any? {
        return when (placeholder) {
            "LifeSteal" -> values[0]
            else -> null
        }
    }

    override fun getPlaceholders(): List<String> = listOf("LifeSteal")

    override fun loadAttribute(values: DoubleArray, lore: String) {
        if (lore.contains(getString("LifeSteal.DiscernName"))) {
            values[0] += getNumber(lore)
        }
    }

    override fun correct(values: DoubleArray) {
        values[0] = values[0].coerceAtLeast(0.0)
    }

    override fun calculationCombatPower(values: DoubleArray): Double {
        return values[0] * getInt("LifeSteal.CombatPower")
    }
}