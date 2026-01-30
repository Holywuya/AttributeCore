package com.attributecore.data.attribute.sub.attack

import com.attributecore.data.attribute.AttributeType
import com.attributecore.data.attribute.SubAttribute
import com.attributecore.data.eventdata.EventData
import com.attributecore.data.eventdata.sub.DamageData
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.LivingEntity

class Crit : SubAttribute("Crit", 2, AttributeType.ATTACK) {

    override fun defaultConfig(config: YamlConfiguration): YamlConfiguration {
        config.set("CritRate.DiscernName", "暴击率")
        config.set("CritRate.CombatPower", 10)
        config.set("CritDamage.DiscernName", "暴击伤害")
        config.set("CritDamage.CombatPower", 5)
        config.set("Message.Crit", "&c&l暴击!")
        return config
    }

    override fun eventMethod(values: DoubleArray, eventData: EventData) {
        if (eventData is DamageData) {
            val critRate = values[0]
            val critDamage = values[1]
            
            if (probability(critRate)) {
                eventData.setCrit(true)
                val multiplier = 1.5 + (critDamage / 100)
                eventData.setDamage(eventData.getDamage() * multiplier)
            }
        }
    }

    override fun getPlaceholder(values: DoubleArray, entity: LivingEntity, placeholder: String): Any? {
        return when (placeholder) {
            "CritRate" -> values[0]
            "CritDamage" -> values[1]
            "Crit" -> "${getDf().format(values[0])}% + ${getDf().format(values[1])}%"
            else -> null
        }
    }

    override fun getPlaceholders(): List<String> = listOf("CritRate", "CritDamage", "Crit")

    override fun loadAttribute(values: DoubleArray, lore: String) {
        when {
            lore.contains(getString("CritRate.DiscernName")) -> values[0] += getNumber(lore)
            lore.contains(getString("CritDamage.DiscernName")) -> values[1] += getNumber(lore)
        }
    }

    override fun correct(values: DoubleArray) {
        values[0] = values[0].coerceIn(0.0, 100.0)
        values[1] = values[1].coerceAtLeast(0.0)
    }

    override fun calculationCombatPower(values: DoubleArray): Double {
        return values[0] * getInt("CritRate.CombatPower") + values[1] * getInt("CritDamage.CombatPower")
    }
}