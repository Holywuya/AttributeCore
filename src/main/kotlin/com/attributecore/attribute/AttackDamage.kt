package com.attributecore.attribute

import com.attributecore.data.AttributeData
import com.attributecore.data.AttributeType
import com.attributecore.data.SubAttribute
import com.attributecore.event.DamageEventData
import com.attributecore.event.EventData
import org.bukkit.entity.Player

class AttackDamage : SubAttribute("attack_damage", AttributeType.Attack) {
    init {
        priority = 0
        combatPowerWeight = 1.5
        register(this)
    }

    private val pattern = createPattern("攻击力")

    override fun loadAttribute(attributeData: AttributeData, lore: String) {
        matchValue(lore, pattern)?.let {
            attributeData.add(name, it)
        }
    }

    override fun eventMethod(attributeData: AttributeData, eventData: EventData) {
        if (eventData is DamageEventData) {
            val value = attributeData[name]
            if (value > 0) {
                eventData.damage += value
            }
        }
    }

    override fun getPlaceholder(attributeData: AttributeData, player: Player, identifier: String): Any? {
        return when (identifier) {
            "attack_damage" -> attributeData[name]
            else -> null
        }
    }

    override fun getPlaceholders(): List<String> {
        return listOf("attack_damage")
    }
}
