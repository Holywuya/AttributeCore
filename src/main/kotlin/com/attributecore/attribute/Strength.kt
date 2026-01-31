package com.attributecore.attribute

import com.attributecore.data.AttributeData
import com.attributecore.data.AttributeType
import com.attributecore.data.SubAttribute
import com.attributecore.event.DamageEventData
import com.attributecore.event.EventData
import com.attributecore.util.DebugLogger
import org.bukkit.entity.Player

/**
 * 力量属性
 * 增加物理攻击伤害（百分比加成）
 * 公式：最终伤害 = 原伤害 * (1 + 力量/100)
 */
class Strength : SubAttribute("力量", AttributeType.Attack) {
    init {
        combatPowerWeight = 1.8
        priority = 50
        register(this)
    }

    override val placeholder: String = "strength"
    
    private val pattern = createPattern("力量")

    override fun loadAttribute(attributeData: AttributeData, lore: String) {
        matchValue(lore, pattern)?.let {
            attributeData.add(name, it)
            DebugLogger.logAttributeLoading("力量属性解析成功: $it")
        }
    }

    override fun eventMethod(attributeData: AttributeData, eventData: EventData) {
        if (eventData is DamageEventData) {
            val value = attributeData[name]
            if (value > 0) {
                val oldDamage = eventData.damage
                // 力量提供百分比伤害加成
                val multiplier = 1.0 + (value / 100.0)
                eventData.damage *= multiplier
                DebugLogger.logDamageCalculation("力量加成: $value, 倍率: ${multiplier}x, 原伤害: $oldDamage, 新伤害: ${eventData.damage}")
            }
        }
    }

    override fun getPlaceholder(attributeData: AttributeData, player: Player, identifier: String): Any? {
        return when (identifier) {
            placeholder -> attributeData[name]
            else -> null
        }
    }

    override fun getPlaceholders(): List<String> {
        return listOf(placeholder)
    }
}
