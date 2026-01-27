package com.attributecore.manager

import com.attributecore.event.CoreConfig
import com.attributecore.data.AttributeType
import com.attributecore.data.DamageData
import com.attributecore.data.ElementAura
import com.attributecore.data.attribute.BaseAttribute
import com.attributecore.event.ReactionLoader
import org.bukkit.entity.LivingEntity
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max

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
        // 使用 CoreConfig 中的配置 (转换 tick 为 ms)
        val durationMs = CoreConfig.auraDuration * 50

        if (currentAura == null || currentAura.isExpired()) {
            auraMap[uuid] = ElementAura(newElement, System.currentTimeMillis(), durationMs)
            return null
        }

        // 2. 元素相同则刷新时间
        if (currentAura.type == newElement) {
            currentAura.startTime = System.currentTimeMillis()
            return null
        }

        // 3. 匹配反应 (从 ReactionLoader 加载的缓存中查找)
        val reaction = ReactionLoader.reactionCache.find {
            it.elements.contains(currentAura.type) && it.elements.contains(newElement)
        } ?: return null

        // 4. 检查 ICD
        val entityICD = icdMap.getOrPut(uuid) { mutableMapOf() }
        val now = System.currentTimeMillis()
        // 使用 CoreConfig 中的配置
        if (now - (entityICD[reaction.id] ?: 0) < CoreConfig.reactionIcd) return null

        // 5. 执行反应行为
        entityICD[reaction.id] = now

        if (reaction.behavior.isNotEmpty()) {
            // 创建一个临时的“反应属性”对象传递给脚本
            // 这样脚本里的 attr.getDisplayName() 就能获取到反应名称 (如 "蒸发")
            val reactionAttr = object : BaseAttribute(
                key = reaction.id,
                names = listOf(reaction.display),
                type = AttributeType.ATTACK,
                priority = 0,
                // 将参与反应的元素作为标签传入，方便脚本判断
                tags = listOf("REACTION", currentAura.type, newElement)
            ) {
                override fun getDisplayName(): String = reaction.display
            }

            // 调用脚本管理器执行攻击逻辑
            // 这里的 reaction.behavior 对应 scripts/ 文件夹下的文件名 (不带.js)
            ScriptManager.invokeAttack(
                scriptId = reaction.behavior,
                attr = reactionAttr,
                attacker = damageData.attacker,
                victim = target, // 反应作用于防御者
                data = damageData,
                value = 1.0 // 反应通常没有固定数值，传1.0或0.0即可
            )
        }

        if (reaction.consume) auraMap.remove(uuid)

        return reaction.display
    }

    /**
     * 清理实体数据
     */
    fun clear(uuid: UUID) {
        auraMap.remove(uuid)
        icdMap.remove(uuid)
    }

    // ================= [ PAPI 支持方法 ] =================

    /**
     * 获取实体的附着元素类型名
     */
    fun getEntityAuraType(uuid: UUID): String {
        val aura = auraMap[uuid]
        return if (aura == null || aura.isExpired()) "NONE" else aura.type
    }

    /**
     * 获取附着剩余时间 (秒)
     */
    fun getEntityAuraTime(uuid: UUID): Double {
        val aura = auraMap[uuid] ?: return 0.0
        if (aura.isExpired()) return 0.0
        return max(0.0, (aura.startTime + aura.duration - System.currentTimeMillis()) / 1000.0)
    }
}