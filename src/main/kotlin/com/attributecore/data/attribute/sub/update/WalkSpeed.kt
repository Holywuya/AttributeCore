package com.attributecore.data.attribute.sub.update

import com.attributecore.data.attribute.AttributeType
import com.attributecore.data.attribute.SubAttribute
import com.attributecore.data.eventdata.EventData
import com.attributecore.data.eventdata.sub.UpdateData
import org.bukkit.attribute.Attribute
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.LivingEntity

class WalkSpeed : SubAttribute("WalkSpeed", 1, AttributeType.UPDATE) {

    override fun defaultConfig(config: YamlConfiguration): YamlConfiguration {
        config.set("WalkSpeed.DiscernName", "移动速度")
        config.set("WalkSpeed.CombatPower", 20)
        return config
    }

    override fun eventMethod(values: DoubleArray, eventData: EventData) {
        if (eventData is UpdateData) {
            val entity = eventData.getEntity()
            val speedBonus = values[0] / 100
            
            val speedAttr = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)
            speedAttr?.let { attr ->
                val baseSpeed = 0.1
                attr.baseValue = baseSpeed * (1 + speedBonus)
            }
        }
    }

    override fun getPlaceholder(values: DoubleArray, entity: LivingEntity, placeholder: String): Any? {
        return when (placeholder) {
            "WalkSpeed" -> values[0]
            else -> null
        }
    }

    override fun getPlaceholders(): List<String> = listOf("WalkSpeed")

    override fun loadAttribute(values: DoubleArray, lore: String) {
        if (lore.contains(getString("WalkSpeed.DiscernName"))) {
            values[0] += getNumber(lore)
        }
    }

    override fun correct(values: DoubleArray) {
        values[0] = values[0].coerceAtLeast(0.0)
    }

    override fun calculationCombatPower(values: DoubleArray): Double {
        return values[0] * getInt("WalkSpeed.CombatPower")
    }
}