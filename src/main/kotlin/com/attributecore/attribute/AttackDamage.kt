package com.attributecore.attribute

import com.attributecore.data.AttributeData
import com.attributecore.data.AttributeType
import com.attributecore.data.SubAttribute
import com.attributecore.event.DamageEventData
import com.attributecore.event.EventData
import com.attributecore.util.DebugLogger
import org.bukkit.entity.Player

class AttackDamage : SubAttribute("attack_damage", AttributeType.Attack) {
    init {
        combatPowerWeight = 1.5
        register(this)
    }

    private val pattern = createPattern("攻击力")

    override fun loadAttribute(attributeData: AttributeData, lore: String) {
        matchValue(lore, pattern)?.let {
            attributeData.add(name, it)
            DebugLogger.logAttributeLoading("攻击力属性解析成功: $it")
        }
    }

    override fun eventMethod(attributeData: AttributeData, eventData: EventData) {
        if (eventData is DamageEventData) {
            val value = attributeData[name]
            if (value > 0) {
                val oldDamage = eventData.damage
                eventData.damage += value
                DebugLogger.logDamageCalculation("攻击力加成: $value, 原伤害: $oldDamage, 新伤害: ${eventData.damage}")
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
