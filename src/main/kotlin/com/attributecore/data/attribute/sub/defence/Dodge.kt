package com.attributecore.data.attribute.sub.defence

import com.attributecore.data.attribute.AttributeType
import com.attributecore.data.attribute.SubAttribute
import com.attributecore.data.eventdata.EventData
import com.attributecore.data.eventdata.sub.DamageData
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.LivingEntity

class Dodge : SubAttribute("Dodge", 1, AttributeType.DEFENCE) {

    override fun defaultConfig(config: YamlConfiguration): YamlConfiguration {
        config.set("DodgeRate.DiscernName", "闪避率")
        config.set("DodgeRate.CombatPower", 12)
        config.set("Message.Dodge", "&a&l闪避!")
        return config
    }

    override fun eventMethod(values: DoubleArray, eventData: EventData) {
        if (eventData is DamageData) {
            val dodgeRate = values[0]
            
            if (probability(dodgeRate)) {
                eventData.sendHolo(getString("Message.Dodge"))
                eventData.setDamage(0.0)
                eventData.setCancelled(true)
            }
        }
    }

    override fun getPlaceholder(values: DoubleArray, entity: LivingEntity, placeholder: String): Any? {
        return when (placeholder) {
            "DodgeRate" -> values[0]
            "Dodge" -> "${getDf().format(values[0])}%"
            else -> null
        }
    }

    override fun getPlaceholders(): List<String> = listOf("DodgeRate", "Dodge")

    override fun loadAttribute(values: DoubleArray, lore: String) {
        if (lore.contains(getString("DodgeRate.DiscernName"))) {
            values[0] += getNumber(lore)
        }
    }

    override fun correct(values: DoubleArray) {
        values[0] = values[0].coerceIn(0.0, 100.0)
    }

    override fun calculationCombatPower(values: DoubleArray): Double {
        return values[0] * getInt("DodgeRate.CombatPower")
    }
}