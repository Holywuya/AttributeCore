package com.attributecore.attribute

import com.attributecore.data.AttributeData
import com.attributecore.data.AttributeType
import com.attributecore.data.SubAttribute
import com.attributecore.event.DamageEventData
import com.attributecore.event.EventData
import org.bukkit.entity.Player
import kotlin.random.Random

class CritChance : SubAttribute("crit_chance", AttributeType.Attack) {
    init {
        priority = 2
        combatPowerWeight = 0.8
        register(this)
    }

    private val pattern = createPattern("暴击率", "%")

    override fun loadAttribute(attributeData: AttributeData, lore: String) {
        matchValue(lore, pattern)?.let {
            attributeData.add(name, it)
        }
    }

    override fun eventMethod(attributeData: AttributeData, eventData: EventData) {
        if (eventData is DamageEventData) {
            val chance = attributeData[name]
            if (chance > 0 && Random.nextDouble(100.0) < chance) {
                val critDamage = attributeData["crit_damage"]
                val multiplier = 1 + (critDamage / 100.0)
                eventData.damage *= multiplier
                if (eventData.attacker is Player) {
                    (eventData.attacker as Player).sendMessage("§6§l暴击! §e${multiplier}x 伤害")
                }
            }
        }
    }

    override fun getPlaceholder(attributeData: AttributeData, player: Player, identifier: String): Any? {
        return when (identifier) {
            "crit_chance" -> attributeData[name]
            else -> null
        }
    }

    override fun getPlaceholders(): List<String> {
        return listOf("crit_chance")
    }
}
