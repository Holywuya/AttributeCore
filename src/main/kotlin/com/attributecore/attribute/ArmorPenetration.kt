package com.attributecore.attribute

import com.attributecore.data.AttributeData
import com.attributecore.data.AttributeType
import com.attributecore.data.SubAttribute
import com.attributecore.event.DamageEventData
import com.attributecore.event.EventData
import com.attributecore.manager.AttributeManager
import com.attributecore.util.DebugLogger
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

/**
 * 护甲穿透属性
 * 忽略目标一定百分比的护甲
 * 在攻击时，临时降低目标护甲值用于伤害计算
 */
class ArmorPenetration : SubAttribute("护甲穿透", AttributeType.Attack) {
    init {
        combatPowerWeight = 1.3
        priority = 80
        register(this)
    }

    override val placeholder: String = "armor_penetration"
    
    private val pattern = createPattern("护甲穿透", "%")

    override fun loadAttribute(attributeData: AttributeData, lore: String) {
        matchValue(lore, pattern)?.let {
            attributeData.add(name, it)
            DebugLogger.logAttributeLoading("护甲穿透属性解析成功: $it%")
        }
    }

    override fun eventMethod(attributeData: AttributeData, eventData: EventData) {
        if (eventData is DamageEventData) {
            val penetration = attributeData[name]
            if (penetration > 0) {
                val victim = eventData.entity
                if (victim is LivingEntity) {
                    val victimData = AttributeManager.getEntityData(victim)
                    val victimArmor = victimData["护甲"]
                    
                    if (victimArmor > 0) {
                        val penetrationPercent = minOf(penetration, 100.0) / 100.0
                        val ignoredArmor = victimArmor * penetrationPercent
                        val effectiveArmor = victimArmor - ignoredArmor
                        
                        victimData.set("护甲", effectiveArmor)
                        DebugLogger.logDamageCalculation("护甲穿透: ${penetration}%, 目标护甲: $victimArmor -> $effectiveArmor")
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
