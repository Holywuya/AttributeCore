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
 * 攻击速度属性
 * 增加/减少实体的攻击速度
 * 基础攻击速度为 4.0（玩家默认），属性值为百分比加成
 */
class AttackSpeed : SubAttribute("攻击速度", AttributeType.Update) {
    init {
        combatPowerWeight = 1.0
        register(this)
    }

    override val placeholder: String = "attack_speed"
    
    private val pattern = createPattern("攻击速度", "%")
    
    companion object {
        // 玩家默认攻击速度
        const val BASE_SPEED = 4.0
        // 最大攻击速度限制
        const val MAX_SPEED = 40.0
        // 最小攻击速度限制
        const val MIN_SPEED = 0.1
    }

    override fun loadAttribute(attributeData: AttributeData, lore: String) {
        matchValue(lore, pattern)?.let {
            attributeData.add(name, it)
            DebugLogger.logAttributeLoading("攻击速度属性解析成功: $it%")
        }
    }

    override fun eventMethod(attributeData: AttributeData, eventData: EventData) {
        if (eventData is UpdateEventData) {
            val entity = eventData.entity
            if (entity is LivingEntity) {
                val percentBonus = attributeData[name]
                val attribute = entity.getAttribute(Attribute.GENERIC_ATTACK_SPEED)
                if (attribute != null) {
                    // 百分比加成：基础速度 * (1 + 百分比/100)
                    val multiplier = 1.0 + (percentBonus / 100.0)
                    val newSpeed = min(MAX_SPEED, max(MIN_SPEED, BASE_SPEED * multiplier))
                    attribute.baseValue = newSpeed
                    DebugLogger.logAttributeLoading("设置 ${entity.name} 攻击速度为: $newSpeed (${percentBonus}%加成)")
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
