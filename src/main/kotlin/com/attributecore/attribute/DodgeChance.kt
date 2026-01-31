package com.attributecore.attribute

import com.attributecore.data.AttributeData
import com.attributecore.data.AttributeType
import com.attributecore.data.SubAttribute
import com.attributecore.event.EventData
import org.bukkit.entity.Player

class DodgeChance : SubAttribute("dodge_chance", AttributeType.Defence) {
    init {
        combatPowerWeight = 0.7
        register(this)
    }

    private val pattern = createPattern("闪避", "%")

    override fun loadAttribute(attributeData: AttributeData, lore: String) {
        matchValue(lore, pattern)?.let {
            attributeData.add(name, it)
        }
    }

    override fun eventMethod(attributeData: AttributeData, eventData: EventData) {
    }

    override fun getPlaceholder(attributeData: AttributeData, player: Player, identifier: String): Any? {
        return when (identifier) {
            "dodge_chance" -> attributeData[name]
            else -> null
        }
    }

    override fun getPlaceholders(): List<String> = listOf("dodge_chance")
}
