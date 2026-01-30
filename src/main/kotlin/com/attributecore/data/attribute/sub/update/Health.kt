package com.attributecore.data.attribute.sub.update

import com.attributecore.data.attribute.AttributeType
import com.attributecore.data.attribute.SubAttribute
import com.attributecore.data.eventdata.EventData
import com.attributecore.data.eventdata.sub.UpdateData
import org.bukkit.attribute.Attribute
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.LivingEntity

class Health : SubAttribute("Health", 2, AttributeType.UPDATE) {

    override fun defaultConfig(config: YamlConfiguration): YamlConfiguration {
        config.set("Health.DiscernName", "生命值")
        config.set("Health.CombatPower", 0.5)
        return config
    }

    override fun eventMethod(values: DoubleArray, eventData: EventData) {
        if (eventData is UpdateData) {
            val entity = eventData.getEntity()
            val bonusHealth = values[0]
            
            val maxHealthAttr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)
            maxHealthAttr?.let { attr ->
                val baseMax = attr.baseValue
                attr.baseValue = baseMax + bonusHealth
            }
        }
    }

    override fun getPlaceholder(values: DoubleArray, entity: LivingEntity, placeholder: String): Any? {
        return when (placeholder) {
            "Health" -> values[0]
            else -> null
        }
    }

    override fun getPlaceholders(): List<String> = listOf("Health")

    override fun loadAttribute(values: DoubleArray, lore: String) {
        if (lore.contains(getString("Health.DiscernName"))) {
            values[0] += getNumber(lore)
        }
    }

    override fun correct(values: DoubleArray) {
        values[0] = values[0].coerceAtLeast(0.0)
    }

    override fun calculationCombatPower(values: DoubleArray): Double {
        return values[0] * getInt("Health.CombatPower")
    }
}