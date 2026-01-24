package com.attributecore.manager

import com.attributecore.data.DamageData
import com.attributecore.data.ElementAura
import org.bukkit.entity.LivingEntity
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object ReactionManager {

    // 存储实体的附着元素: UUID -> 附着对象
    private val auraMap = ConcurrentHashMap<UUID, ElementAura>()

    /**
     * 处理元素注入
     * @return 触发的反应名称，如果没有反应则返回 null
     */
    fun handleElement(target: LivingEntity, newElement: String, damageData: DamageData): String? {
        val uuid = target.uniqueId
        val currentAura = auraMap[uuid]

        // 1. 如果当前没有附着，或者旧附着已过期
        if (currentAura == null || currentAura.isExpired()) {
            auraMap[uuid] = ElementAura(newElement, System.currentTimeMillis(), 5000L) // 默认附着5秒
            return null
        }

        // 2. 如果新元素和旧元素相同，刷新时间
        if (currentAura.type == newElement) {
            currentAura.startTime = System.currentTimeMillis()
            return null
        }

        // 3. 触发反应逻辑 (核心)
        val reaction = triggerReaction(currentAura.type, newElement, damageData, target)

        // 4. 反应后通常会消耗掉附着元素
        if (reaction != null) {
            auraMap.remove(uuid)
        }

        return reaction
    }

    private fun triggerReaction(old: String, new: String, data: DamageData, target: LivingEntity): String? {
        val combination = setOf(old, new)

        return when {
            // 火 + 水 = 蒸发 (伤害翻倍)
            combination.containsAll(listOf("FIRE", "WATER")) -> {
                data.setDamageMultiplier(2.0)
                "§b§l蒸发 (Vaporize)"
            }

            // 火 + 雷 = 超载 (爆炸，造成额外固定伤害)
            combination.containsAll(listOf("FIRE", "LIGHTNING")) -> {
                data.addDamage(50.0) // 基础额外伤害
                target.velocity = target.velocity.add(target.location.direction.multiply(-1.5)) // 击退
                "§c§l超载 (Overloaded)"
            }

            // 水 + 冰 = 冻结 (限制移动)
            combination.containsAll(listOf("WATER", "ICE")) -> {
                // 这里可以调用你的 Potion 逻辑或设置速度为0
                target.addPotionEffect(org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SLOW, 40, 10))
                "§f§l冻结 (Frozen)"
            }

            // 冰 + 雷 = 超导 (大幅削减防御力)
            combination.containsAll(listOf("ICE", "LIGHTNING")) -> {
                data.addPercentPenetration(50.0) // 临时无视50%防御
                "§d§l超导 (Superconduct)"
            }

            else -> null
        }
    }
}