package com.attributecore.attribute

import com.attributecore.data.AttributeData
import com.attributecore.data.AttributeType
import com.attributecore.data.SubAttribute
import com.attributecore.event.DefenceEventData
import com.attributecore.event.EventData
import org.bukkit.entity.Player

class Defense : SubAttribute("defense", AttributeType.Defence) {
    init {
        combatPowerWeight = 1.2
        register(this)
    }

    override val nbtName: String = "防御力"
    
    private val pattern = createPattern("防御力")

    override fun loadAttribute(attributeData: AttributeData, lore: String) {
        matchValue(lore, pattern)?.let {
            attributeData.add(name, it)
        }
    }

    override fun eventMethod(attributeData: AttributeData, eventData: EventData) {
        if (eventData is DefenceEventData) {
            val value = attributeData[name]
            if (value > 0) {
                val reduction = value / (value + 100)
                eventData.damage *= (1 - reduction)
            }
        }
    }

    override fun getPlaceholder(attributeData: AttributeData, player: Player, identifier: String): Any? {
        return when (identifier) {
            "defense" -> attributeData[name]
            else -> null
        }
    }

    override fun getPlaceholders(): List<String> {
        return listOf("defense")
    }
}
