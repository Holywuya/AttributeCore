package com.attributecore.data.attribute.sub.defence

import com.attributecore.data.attribute.AttributeType
import com.attributecore.data.attribute.SubAttribute
import com.attributecore.data.eventdata.EventData
import com.attributecore.data.eventdata.sub.DamageData
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.LivingEntity

class Toughness : SubAttribute("Toughness", 1, AttributeType.DEFENCE) {

    override fun defaultConfig(config: YamlConfiguration): YamlConfiguration {
        config.set("Toughness.DiscernName", "韧性")
        config.set("Toughness.CombatPower", 5)
        return config
    }

    override fun eventMethod(values: DoubleArray, eventData: EventData) {
        if (eventData is DamageData) {
            val toughness = values[0]
            
            if (eventData.isCrit()) {
                val critReduction = toughness / (toughness + 50)
                val currentDamage = eventData.getDamage()
                eventData.setDamage(currentDamage * (1 - critReduction))
            }
        }
    }

    override fun getPlaceholder(values: DoubleArray, entity: LivingEntity, placeholder: String): Any? {
        return when (placeholder) {
            "Toughness" -> values[0]
            else -> null
        }
    }

    override fun getPlaceholders(): List<String> = listOf("Toughness")

    override fun loadAttribute(values: DoubleArray, lore: String) {
        if (lore.contains(getString("Toughness.DiscernName"))) {
            values[0] += getNumber(lore)
        }
    }

    override fun correct(values: DoubleArray) {
        values[0] = values[0].coerceAtLeast(0.0)
    }

    override fun calculationCombatPower(values: DoubleArray): Double {
        return values[0] * getInt("Toughness.CombatPower")
    }
}