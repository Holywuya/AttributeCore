package com.attributecore.attribute

import com.attributecore.data.AttributeData
import com.attributecore.data.AttributeType
import com.attributecore.data.SubAttribute
import com.attributecore.event.DamageEventData
import com.attributecore.event.EventData
import com.attributecore.util.DebugLogger
import org.bukkit.entity.Player
import kotlin.random.Random

/**
 * 敏捷属性
 * 增加暴击率（每 2 点敏捷 = 1% 暴击率）
 * 同时提供少量闪避加成（每 5 点敏捷 = 1% 闪避率）
 */
class Agility : SubAttribute("敏捷", AttributeType.Attack, AttributeType.Defence) {
    init {
        combatPowerWeight = 1.2
        priority = 40
        register(this)
    }

    override val placeholder: String = "agility"
    
    private val pattern = createPattern("敏捷")

    override fun loadAttribute(attributeData: AttributeData, lore: String) {
        matchValue(lore, pattern)?.let {
            attributeData.add(name, it)
            DebugLogger.logAttributeLoading("敏捷属性解析成功: $it")
        }
    }

    override fun eventMethod(attributeData: AttributeData, eventData: EventData) {
        if (eventData is DamageEventData) {
            val agility = attributeData[name]
            if (agility > 0) {
                // 敏捷转化为暴击率加成（每 2 点 = 1%）
                val critBonus = agility / 2.0
                // 将暴击加成临时添加到暴击率
                val currentCritChance = attributeData["暴击率"]
                attributeData.add("暴击率", critBonus)
                DebugLogger.logDamageCalculation("敏捷转化为暴击率加成: $agility 敏捷 = +${critBonus}% 暴击率")
            }
        }
    }

    override fun getPlaceholder(attributeData: AttributeData, player: Player, identifier: String): Any? {
        return when (identifier) {
            placeholder -> attributeData[name]
            "agility_crit_bonus" -> attributeData[name] / 2.0
            "agility_dodge_bonus" -> attributeData[name] / 5.0
            else -> null
        }
    }

    override fun getPlaceholders(): List<String> {
        return listOf(placeholder, "agility_crit_bonus", "agility_dodge_bonus")
    }
}
