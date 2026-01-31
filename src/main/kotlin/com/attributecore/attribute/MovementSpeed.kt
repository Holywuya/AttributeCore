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
import kotlin.math.min

/**
 * 移动速度属性
 * 增加/减少实体的移动速度
 * 基础速度为 0.1（玩家默认），属性值为百分比加成
 */
class MovementSpeed : SubAttribute("移动速度", AttributeType.Update) {
    init {
        combatPowerWeight = 0.8
        register(this)
    }

    override val placeholder: String = "movement_speed"
    
    private val pattern = createPattern("移动速度", "%")
    
    companion object {
        // 玩家默认移动速度
        const val BASE_SPEED = 0.1
        // 最大速度限制（防止过快）
        const val MAX_SPEED = 1.0
        // 最小速度限制
        const val MIN_SPEED = 0.01
    }

    override fun loadAttribute(attributeData: AttributeData, lore: String) {
        matchValue(lore, pattern)?.let {
            attributeData.add(name, it)
            DebugLogger.logAttributeLoading("移动速度属性解析成功: $it%")
        }
    }

    override fun eventMethod(attributeData: AttributeData, eventData: EventData) {
        if (eventData is UpdateEventData) {
            val entity = eventData.entity
            if (entity is LivingEntity) {
                val percentBonus = attributeData[name]
                val attribute = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)
                if (attribute != null) {
                    // 百分比加成：基础速度 * (1 + 百分比/100)
                    val multiplier = 1.0 + (percentBonus / 100.0)
                    val newSpeed = min(MAX_SPEED, max(MIN_SPEED, BASE_SPEED * multiplier))
                    attribute.baseValue = newSpeed
                    DebugLogger.logAttributeLoading("设置 ${entity.name} 移动速度为: $newSpeed (${percentBonus}%加成)")
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
