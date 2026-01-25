package com.attributecore.manager

import com.attributecore.data.DamageData
import com.attributecore.data.ElementAura
import com.attributecore.event.AttributeBehaviors
import com.attributecore.event.CoreConfig
import com.attributecore.event.ReactionLoader
import org.bukkit.entity.LivingEntity
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object ReactionManager {

    private val auraMap = ConcurrentHashMap<UUID, ElementAura>()
    // 反应内置冷却: UUID -> (反应ID -> 上次触发时间)
    private val icdMap = ConcurrentHashMap<UUID, MutableMap<String, Long>>()

    /**
     * 处理元素附着与反应
     */
    fun handleElement(target: LivingEntity, newElement: String, damageData: DamageData): String? {
        val uuid = target.uniqueId
        val currentAura = auraMap[uuid]

        // 1. 检查附着
        if (currentAura == null || currentAura.isExpired()) {
            auraMap[uuid] = ElementAura(newElement, System.currentTimeMillis(), ReactionLoader.auraDuration * 50)
            return null
        }

        // 2. 元素相同则刷新时间
        if (currentAura.type == newElement) {
            currentAura.startTime = System.currentTimeMillis()
            return null
        }

        // 3. 匹配反应
        val reaction = ReactionLoader.reactionCache.find {
            it.elements.contains(currentAura.type) && it.elements.contains(newElement)
        } ?: return null

        // 4. 检查 ICD (防止同一秒触发几十次超载导致卡死)
        val entityICD = icdMap.getOrPut(uuid) { mutableMapOf() }
        val now = System.currentTimeMillis()
        if (now - (entityICD[reaction.id] ?: 0) < CoreConfig.reactionIcd) return null // 同种反应 0.5s 冷却

        // 5. 执行反应行为
        entityICD[reaction.id] = now
        if (reaction.behavior.isNotEmpty()) {
            // 通过 Behavior 系统运行脚本
            AttributeBehaviors.handleAttack(reaction.behavior, damageData, 1.0, listOf("REACTION"))
        }

        if (reaction.consume) auraMap.remove(uuid)

        return reaction.display
    }

    fun clear(uuid: UUID) {
        auraMap.remove(uuid)
        icdMap.remove(uuid)
    }
}