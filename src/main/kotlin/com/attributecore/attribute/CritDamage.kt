package com.attributecore.attribute

import com.attributecore.data.AttributeData
import com.attributecore.data.AttributeType
import com.attributecore.data.SubAttribute
import com.attributecore.event.EventData
import org.bukkit.entity.Player

class CritDamage : SubAttribute("crit_damage", AttributeType.Other) {
    init {
        priority = 3
        combatPowerWeight = 0.5
        register(this)
    }

    private val pattern = createPattern("暴击伤害", "%")

    override fun loadAttribute(attributeData: AttributeData, lore: String) {
        matchValue(lore, pattern)?.let {
            attributeData.add(name, it)
        }
    }

    override fun eventMethod(attributeData: AttributeData, eventData: EventData) {
    }

    override fun getPlaceholder(attributeData: AttributeData, player: Player, identifier: String): Any? {
        return when (identifier) {
            "crit_damage" -> attributeData[name]
            else -> null
        }
    }

    override fun getPlaceholders(): List<String> {
        return listOf("crit_damage")
    }
}
