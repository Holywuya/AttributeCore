package com.attributecore.data.attribute.sub.attack

import com.attributecore.data.attribute.AttributeType
import com.attributecore.data.attribute.SubAttribute
import com.attributecore.data.eventdata.EventData
import com.attributecore.data.eventdata.sub.DamageData
import org.bukkit.Particle
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.LivingEntity

class Lightning : SubAttribute("Lightning", 2, AttributeType.ATTACK) {

    override fun defaultConfig(config: YamlConfiguration): YamlConfiguration {
        config.set("LightningChance.DiscernName", "雷电几率")
        config.set("LightningChance.CombatPower", 10)
        config.set("LightningDamage.DiscernName", "雷电伤害")
        config.set("LightningDamage.CombatPower", 4)
        return config
    }

    override fun eventMethod(values: DoubleArray, eventData: EventData) {
        if (eventData is DamageData) {
            val chance = values[0]
            val lightningDamage = values[1]
            
            if (probability(chance)) {
                val defender = eventData.defender
                defender.world.strikeLightningEffect(defender.location)
                defender.damage(lightningDamage)
                defender.world.spawnParticle(Particle.CRIT_MAGIC, defender.location, 20)
            }
        }
    }

    override fun getPlaceholder(values: DoubleArray, entity: LivingEntity, placeholder: String): Any? {
        return when (placeholder) {
            "LightningChance" -> values[0]
            "LightningDamage" -> values[1]
            "Lightning" -> "${getDf().format(values[0])}% / ${getDf().format(values[1])}"
            else -> null
        }
    }

    override fun getPlaceholders(): List<String> = listOf("LightningChance", "LightningDamage", "Lightning")

    override fun loadAttribute(values: DoubleArray, lore: String) {
        when {
            lore.contains(getString("LightningChance.DiscernName")) -> values[0] += getNumber(lore)
            lore.contains(getString("LightningDamage.DiscernName")) -> values[1] += getNumber(lore)
        }
    }

    override fun correct(values: DoubleArray) {
        values[0] = values[0].coerceIn(0.0, 100.0)
        values[1] = values[1].coerceAtLeast(0.0)
    }

    override fun calculationCombatPower(values: DoubleArray): Double {
        return values[0] * getInt("LightningChance.CombatPower") + values[1] * getInt("LightningDamage.CombatPower")
    }
}