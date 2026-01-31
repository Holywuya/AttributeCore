package com.attributecore.attribute

import com.attributecore.data.AttributeData
import com.attributecore.data.AttributeType
import com.attributecore.data.SubAttribute
import com.attributecore.event.EventData
import org.bukkit.entity.Player

class LifeSteal : SubAttribute("life_steal", AttributeType.Attack) {
    init {
        combatPowerWeight = 0.6
        register(this)
    }

    private val pattern = createPattern("吸血", "%")

    override fun loadAttribute(attributeData: AttributeData, lore: String) {
        matchValue(lore, pattern)?.let {
            attributeData.add(name, it)
        }
    }

    override fun eventMethod(attributeData: AttributeData, eventData: EventData) {
    }

    override fun getPlaceholder(attributeData: AttributeData, player: Player, identifier: String): Any? {
        return when (identifier) {
            "life_steal" -> attributeData[name]
            else -> null
        }
    }

    override fun getPlaceholders(): List<String> = listOf("life_steal")
}
