package com.attributecore.attribute

import com.attributecore.data.AttributeData
import com.attributecore.data.AttributeType
import com.attributecore.data.SubAttribute
import com.attributecore.event.EventData
import org.bukkit.entity.Player

class ExecuteThreshold : SubAttribute("execute_threshold", AttributeType.Attack) {
    init {
        combatPowerWeight = 0.3
        register(this)
    }

    private val pattern = createPattern("斩杀", "%")

    override fun loadAttribute(attributeData: AttributeData, lore: String) {
        matchValue(lore, pattern)?.let {
            attributeData.add(name, it)
        }
    }

    override fun eventMethod(attributeData: AttributeData, eventData: EventData) {
    }

    override fun getPlaceholder(attributeData: AttributeData, player: Player, identifier: String): Any? {
        return when (identifier) {
            "execute_threshold" -> attributeData[name]
            else -> null
        }
    }

    override fun getPlaceholders(): List<String> = listOf("execute_threshold")
}
