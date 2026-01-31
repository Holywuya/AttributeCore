package com.attributecore.attribute

import com.attributecore.data.AttributeData
import com.attributecore.data.AttributeType
import com.attributecore.data.SubAttribute
import com.attributecore.event.DefenceEventData
import com.attributecore.event.EventData
import com.attributecore.util.DebugLogger
import org.bukkit.entity.Player

/**
 * 护甲属性
 * 减少受到的物理伤害
 * 公式：伤害减免 = 护甲 / (护甲 + 100)
 * 与防御力不同，护甲是线性叠加的减伤机制
 */
class Armor : SubAttribute("护甲", AttributeType.Defence) {
    init {
        combatPowerWeight = 1.0
        priority = 90
        register(this)
    }

    override val placeholder: String = "armor"
    
    private val pattern = createPattern("护甲")

    override fun loadAttribute(attributeData: AttributeData, lore: String) {
        matchValue(lore, pattern)?.let {
            attributeData.add(name, it)
            DebugLogger.logAttributeLoading("护甲属性解析成功: $it")
        }
    }

    override fun eventMethod(attributeData: AttributeData, eventData: EventData) {
        if (eventData is DefenceEventData) {
            val armorValue = attributeData[name]
            if (armorValue > 0) {
                val oldDamage = eventData.damage
                // 护甲减伤公式：伤害 * (1 - 护甲/(护甲+100))
                val reduction = armorValue / (armorValue + 100.0)
                eventData.damage *= (1.0 - reduction)
                DebugLogger.logDamageCalculation("护甲减伤: $armorValue (${(reduction * 100).toInt()}%), 原伤害: $oldDamage, 新伤害: ${eventData.damage}")
            }
        }
    }

    override fun getPlaceholder(attributeData: AttributeData, player: Player, identifier: String): Any? {
        return when (identifier) {
            placeholder -> attributeData[name]
            "armor_reduction" -> {
                val armor = attributeData[name]
                if (armor > 0) (armor / (armor + 100.0)) * 100.0 else 0.0
            }
            else -> null
        }
    }

    override fun getPlaceholders(): List<String> {
        return listOf(placeholder, "armor_reduction")
    }
}
