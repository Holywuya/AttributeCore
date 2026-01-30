package com.attributecore.data.attribute.sub.defence

import com.attributecore.data.attribute.AttributeType
import com.attributecore.data.attribute.SubAttribute
import com.attributecore.data.eventdata.EventData
import com.attributecore.data.eventdata.sub.DamageData
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.LivingEntity

class Defense : SubAttribute("Defense", 2, AttributeType.DEFENCE) {

    override fun defaultConfig(config: YamlConfiguration): YamlConfiguration {
        config.set("Defense.DiscernName", "防御力")
        config.set("Defense.CombatPower", 2)
        config.set("PVPDefense.DiscernName", "PVP防御力")
        config.set("PVPDefense.CombatPower", 2)
        return config
    }

    override fun eventMethod(values: DoubleArray, eventData: EventData) {
        if (eventData is DamageData) {
            val defense = values[0]
            val pvpDefense = values[1]
            
            val attacker = eventData.attacker
            val effectiveDefense = if (attacker is org.bukkit.entity.Player) defense + pvpDefense else defense
            
            val damageReduction = effectiveDefense / (effectiveDefense + 100)
            val currentDamage = eventData.getDamage()
            eventData.setDamage(currentDamage * (1 - damageReduction))
        }
    }

    override fun getPlaceholder(values: DoubleArray, entity: LivingEntity, placeholder: String): Any? {
        return when (placeholder) {
            "Defense" -> values[0]
            "PVPDefense" -> values[1]
            "TotalDefense" -> values[0] + values[1]
            else -> null
        }
    }

    override fun getPlaceholders(): List<String> = listOf("Defense", "PVPDefense", "TotalDefense")

    override fun loadAttribute(values: DoubleArray, lore: String) {
        when {
            lore.contains(getString("PVPDefense.DiscernName")) -> values[1] += getNumber(lore)
            lore.contains(getString("Defense.DiscernName")) -> values[0] += getNumber(lore)
        }
    }

    override fun correct(values: DoubleArray) {
        values[0] = values[0].coerceAtLeast(0.0)
        values[1] = values[1].coerceAtLeast(0.0)
    }

    override fun calculationCombatPower(values: DoubleArray): Double {
        return (values[0] + values[1]) * getInt("Defense.CombatPower")
    }
}