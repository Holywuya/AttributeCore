package com.attributecore.data.attribute.sub.attack

import com.attributecore.data.attribute.AttributeType
import com.attributecore.data.attribute.SubAttribute
import com.attributecore.data.eventdata.EventData
import com.attributecore.data.eventdata.sub.DamageData
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.LivingEntity

class Ignition : SubAttribute("Ignition", 2, AttributeType.ATTACK) {

    override fun defaultConfig(config: YamlConfiguration): YamlConfiguration {
        config.set("IgnitionChance.DiscernName", "点燃几率")
        config.set("IgnitionChance.CombatPower", 5)
        config.set("IgnitionDuration.DiscernName", "点燃持续时间")
        config.set("IgnitionDuration.CombatPower", 2)
        return config
    }

    override fun eventMethod(values: DoubleArray, eventData: EventData) {
        if (eventData is DamageData) {
            val chance = values[0]
            val duration = values[1].toInt()
            
            if (probability(chance)) {
                eventData.defender.fireTicks = duration * 20
            }
        }
    }

    override fun getPlaceholder(values: DoubleArray, entity: LivingEntity, placeholder: String): Any? {
        return when (placeholder) {
            "IgnitionChance" -> values[0]
            "IgnitionDuration" -> values[1]
            "Ignition" -> "${getDf().format(values[0])}% / ${getDf().format(values[1])}s"
            else -> null
        }
    }

    override fun getPlaceholders(): List<String> = listOf("IgnitionChance", "IgnitionDuration", "Ignition")

    override fun loadAttribute(values: DoubleArray, lore: String) {
        when {
            lore.contains(getString("IgnitionChance.DiscernName")) -> values[0] += getNumber(lore)
            lore.contains(getString("IgnitionDuration.DiscernName")) -> values[1] += getNumber(lore)
        }
    }

    override fun correct(values: DoubleArray) {
        values[0] = values[0].coerceIn(0.0, 100.0)
        values[1] = values[1].coerceAtLeast(0.0)
    }

    override fun calculationCombatPower(values: DoubleArray): Double {
        return values[0] * getInt("IgnitionChance.CombatPower") + values[1] * getInt("IgnitionDuration.CombatPower")
    }
}