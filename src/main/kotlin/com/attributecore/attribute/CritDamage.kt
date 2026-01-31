package com.attributecore.attribute

import com.attributecore.data.AttributeData
import com.attributecore.data.AttributeType
import com.attributecore.data.SubAttribute
import com.attributecore.event.EventData
import org.bukkit.entity.Player

class CritDamage : SubAttribute("crit_damage", AttributeType.Other) {
    init {
        combatPowerWeight = 0.5
        register(this)
    }

    override val nbtName: String = "暴击伤害"
    
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
