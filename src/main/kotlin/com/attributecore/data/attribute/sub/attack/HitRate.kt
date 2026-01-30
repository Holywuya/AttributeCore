package com.attributecore.data.attribute.sub.attack

import com.attributecore.data.attribute.AttributeType
import com.attributecore.data.attribute.SubAttribute
import com.attributecore.data.eventdata.EventData
import com.attributecore.data.eventdata.sub.DamageData
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.LivingEntity

class HitRate : SubAttribute("HitRate", 1, AttributeType.ATTACK) {

    override fun defaultConfig(config: YamlConfiguration): YamlConfiguration {
        config.set("HitRate.DiscernName", "命中率")
        config.set("HitRate.CombatPower", 8)
        return config
    }

    override fun eventMethod(values: DoubleArray, eventData: EventData) {
        if (eventData is DamageData) {
            val hitRate = values[0]
            if (!probability(hitRate)) {
                eventData.sendHolo("§7§o未命中!")
            }
        }
    }

    override fun getPlaceholder(values: DoubleArray, entity: LivingEntity, placeholder: String): Any? {
        return when (placeholder) {
            "HitRate" -> values[0]
            else -> null
        }
    }

    override fun getPlaceholders(): List<String> = listOf("HitRate")

    override fun loadAttribute(values: DoubleArray, lore: String) {
        if (lore.contains(getString("HitRate.DiscernName"))) {
            values[0] += getNumber(lore)
        }
    }

    override fun correct(values: DoubleArray) {
        values[0] = values[0].coerceIn(0.0, 100.0)
    }

    override fun calculationCombatPower(values: DoubleArray): Double {
        return values[0] * getInt("HitRate.CombatPower")
    }
}