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
import kotlin.math.max

/**
 * 最大生命值属性
 * 增加实体的最大生命上限
 */
class MaxHealth : SubAttribute("生命上限", AttributeType.Update) {
    init {
        combatPowerWeight = 2.0
        register(this)
    }

    override val placeholder: String = "max_health"
    
    private val pattern = createPattern("生命上限")

    override fun loadAttribute(attributeData: AttributeData, lore: String) {
        matchValue(lore, pattern)?.let {
            attributeData.add(name, it)
            DebugLogger.logAttributeLoading("生命上限属性解析成功: $it")
        }
    }

    override fun eventMethod(attributeData: AttributeData, eventData: EventData) {
        if (eventData is UpdateEventData) {
            val entity = eventData.entity
            if (entity is LivingEntity) {
                val value = attributeData[name]
                if (value > 0) {
                    val attribute = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)
                    if (attribute != null) {
                        // 基础生命值 20 + 额外生命值
                        val newMaxHealth = max(1.0, 20.0 + value)
                        attribute.baseValue = newMaxHealth
                        DebugLogger.logAttributeLoading("设置 ${entity.name} 最大生命值为: $newMaxHealth")
                    }
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
