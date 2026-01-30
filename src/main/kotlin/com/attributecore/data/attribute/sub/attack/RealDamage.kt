package com.attributecore.data.attribute.sub.attack

import com.attributecore.data.attribute.AttributeType
import com.attributecore.data.attribute.SubAttribute
import com.attributecore.data.eventdata.EventData
import com.attributecore.data.eventdata.sub.DamageData
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.LivingEntity

class RealDamage : SubAttribute("RealDamage", 1, AttributeType.ATTACK) {

    override fun defaultConfig(config: YamlConfiguration): YamlConfiguration {
        config.set("RealDamage.DiscernName", "真实伤害")
        config.set("RealDamage.CombatPower", 10)
        return config
    }

    override fun eventMethod(values: DoubleArray, eventData: EventData) {
        if (eventData is DamageData) {
            val realDamage = values[0]
            eventData.addDamage(realDamage)
        }
    }

    override fun getPlaceholder(values: DoubleArray, entity: LivingEntity, placeholder: String): Any? {
        return when (placeholder) {
            "RealDamage" -> values[0]
            else -> null
        }
    }

    override fun getPlaceholders(): List<String> = listOf("RealDamage")

    override fun loadAttribute(values: DoubleArray, lore: String) {
        if (lore.contains(getString("RealDamage.DiscernName"))) {
            values[0] += getNumber(lore)
        }
    }

    override fun correct(values: DoubleArray) {
        values[0] = values[0].coerceAtLeast(0.0)
    }

    override fun calculationCombatPower(values: DoubleArray): Double {
        return values[0] * getInt("RealDamage.CombatPower")
    }
}