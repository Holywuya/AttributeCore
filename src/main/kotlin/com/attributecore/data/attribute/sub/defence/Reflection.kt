package com.attributecore.data.attribute.sub.defence

import com.attributecore.data.attribute.AttributeType
import com.attributecore.data.attribute.SubAttribute
import com.attributecore.data.eventdata.EventData
import com.attributecore.data.eventdata.sub.DamageData
import org.bukkit.Particle
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.LivingEntity

class Reflection : SubAttribute("Reflection", 2, AttributeType.DEFENCE) {

    override fun defaultConfig(config: YamlConfiguration): YamlConfiguration {
        config.set("ReflectionRate.DiscernName", "反伤率")
        config.set("ReflectionRate.CombatPower", 6)
        config.set("ReflectionDamage.DiscernName", "反伤比例")
        config.set("ReflectionDamage.CombatPower", 4)
        config.set("Message.Reflection", "&c&l反伤!")
        return config
    }

    override fun eventMethod(values: DoubleArray, eventData: EventData) {
        if (eventData is DamageData) {
            val reflectRate = values[0]
            val reflectPercent = values[1] / 100
            
            if (probability(reflectRate)) {
                eventData.sendHolo(getString("Message.Reflection"))
                val damage = eventData.getDamage()
                val reflectDamage = damage * reflectPercent
                
                val attacker = eventData.attacker
                attacker.damage(reflectDamage)
                
                attacker.world.spawnParticle(Particle.DAMAGE_INDICATOR, attacker.location.add(0.0, 1.0, 0.0), 5)
            }
        }
    }

    override fun getPlaceholder(values: DoubleArray, entity: LivingEntity, placeholder: String): Any? {
        return when (placeholder) {
            "ReflectionRate" -> values[0]
            "ReflectionDamage" -> values[1]
            "Reflection" -> "${getDf().format(values[0])}% / ${getDf().format(values[1])}%"
            else -> null
        }
    }

    override fun getPlaceholders(): List<String> = listOf("ReflectionRate", "ReflectionDamage", "Reflection")

    override fun loadAttribute(values: DoubleArray, lore: String) {
        when {
            lore.contains(getString("ReflectionRate.DiscernName")) -> values[0] += getNumber(lore)
            lore.contains(getString("ReflectionDamage.DiscernName")) -> values[1] += getNumber(lore)
        }
    }

    override fun correct(values: DoubleArray) {
        values[0] = values[0].coerceIn(0.0, 100.0)
        values[1] = values[1].coerceIn(0.0, 100.0)
    }

    override fun calculationCombatPower(values: DoubleArray): Double {
        return values[0] * getInt("ReflectionRate.CombatPower") + values[1] * getInt("ReflectionDamage.CombatPower")
    }
}