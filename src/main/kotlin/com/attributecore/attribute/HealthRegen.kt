package com.attributecore.attribute

import com.attributecore.data.AttributeData
import com.attributecore.data.AttributeType
import com.attributecore.data.SubAttribute
import com.attributecore.event.EventData
import com.attributecore.event.UpdateEventData
import com.attributecore.util.DebugLogger
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import taboolib.common.platform.function.submit

/**
 * 生命恢复属性
 * 每秒恢复一定量的生命值
 * 通过 Update 类型在装备变更时启动恢复任务
 */
class HealthRegen : SubAttribute("生命恢复", AttributeType.Update) {
    init {
        combatPowerWeight = 1.5
        register(this)
    }

    override val placeholder: String = "health_regen"
    
    private val pattern = createPattern("生命恢复")
    
    // 存储每个实体的恢复任务状态
    private val regenTasks = mutableMapOf<java.util.UUID, Boolean>()

    override fun loadAttribute(attributeData: AttributeData, lore: String) {
        matchValue(lore, pattern)?.let {
            attributeData.add(name, it)
            DebugLogger.logAttributeLoading("生命恢复属性解析成功: $it")
        }
    }

    override fun eventMethod(attributeData: AttributeData, eventData: EventData) {
        if (eventData is UpdateEventData) {
            val entity = eventData.entity
            if (entity is LivingEntity) {
                val regenValue = attributeData[name]
                val uuid = entity.uniqueId
                
                // 如果已有恢复任务或没有恢复值，则跳过
                if (regenTasks[uuid] == true || regenValue <= 0) {
                    return
                }
                
                // 启动生命恢复任务（每 20 tick = 1 秒恢复一次）
                regenTasks[uuid] = true
                submit(async = false, delay = 20L, period = 20L) {
                    if (!entity.isValid || entity.isDead) {
                        regenTasks.remove(uuid)
                        cancel()
                        return@submit
                    }
                    
                    val currentRegen = attributeData[name]
                    if (currentRegen <= 0) {
                        regenTasks.remove(uuid)
                        cancel()
                        return@submit
                    }
                    
                    val maxHealth = entity.maxHealth
                    val currentHealth = entity.health
                    if (currentHealth < maxHealth) {
                        val newHealth = minOf(currentHealth + currentRegen, maxHealth)
                        entity.health = newHealth
                        DebugLogger.logAttributeLoading("${entity.name} 恢复生命: $currentRegen, 当前: $newHealth/$maxHealth")
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
