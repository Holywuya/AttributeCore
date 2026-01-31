package com.attributecore.attribute

import com.attributecore.data.AttributeData
import com.attributecore.data.AttributeType
import com.attributecore.data.SubAttribute
import com.attributecore.event.EventData
import com.attributecore.event.UpdateEventData
import com.attributecore.util.DebugLogger
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

/**
 * 幸运值属性
 * 影响掉落物品的品质和数量
 * 直接修改 Minecraft 原生 GENERIC_LUCK 属性
 */
class Luck : SubAttribute("幸运", AttributeType.Update) {
    init {
        combatPowerWeight = 0.3
        register(this)
    }

    override val placeholder: String = "luck"
    
    private val pattern = createPattern("幸运")

    override fun loadAttribute(attributeData: AttributeData, lore: String) {
        matchValue(lore, pattern)?.let {
            attributeData.add(name, it)
            DebugLogger.logAttributeLoading("幸运属性解析成功: $it")
        }
    }

    override fun eventMethod(attributeData: AttributeData, eventData: EventData) {
        if (eventData is UpdateEventData) {
            val entity = eventData.entity
            if (entity is LivingEntity) {
                val value = attributeData[name]
                val attribute = entity.getAttribute(Attribute.GENERIC_LUCK)
                if (attribute != null) {
                    attribute.baseValue = value
                    DebugLogger.logAttributeLoading("设置 ${entity.name} 幸运值为: $value")
                }
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
