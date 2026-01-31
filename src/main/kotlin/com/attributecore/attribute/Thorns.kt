package com.attributecore.attribute

import com.attributecore.data.AttributeData
import com.attributecore.data.AttributeType
import com.attributecore.data.SubAttribute
import com.attributecore.event.EventData
import org.bukkit.entity.Player

class Thorns : SubAttribute("thorns", AttributeType.Defence) {
    init {
        combatPowerWeight = 0.4
        register(this)
    }

    private val pattern = createPattern("荆棘", "%")

    override fun loadAttribute(attributeData: AttributeData, lore: String) {
        matchValue(lore, pattern)?.let {
            attributeData.add(name, it)
        }
    }

    override fun eventMethod(attributeData: AttributeData, eventData: EventData) {
    }

    override fun getPlaceholder(attributeData: AttributeData, player: Player, identifier: String): Any? {
        return when (identifier) {
            "thorns" -> attributeData[name]
            else -> null
        }
    }

    override fun getPlaceholders(): List<String> = listOf("thorns")
}
