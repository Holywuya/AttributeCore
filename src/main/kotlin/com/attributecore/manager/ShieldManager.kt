package com.attributecore.manager

import com.attributecore.event.CoreConfig
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import taboolib.common.platform.function.submit
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * 护盾管理器
 * 独立负责护盾的数值存储、自动恢复和伤害抵扣
 */
object ShieldManager {

    // 当前护盾值缓存 (UUID -> Current Shield)
    private val currentShieldMap = ConcurrentHashMap<UUID, Double>()

    // 最大护盾值缓存 (UUID -> Max Shield)
    // 这个值由 AttributeBehaviors -> setMaxShield 实时填入
    private val maxShieldMap = ConcurrentHashMap<UUID, Double>()

    fun init() {
        startShieldRegenTask()
    }

    /**
     * 设置实体的最大护盾值
     * 通常在 AttributeManager.update() 计算完属性后调用
     */
    fun setMaxShield(uuid: UUID, value: Double) {
        maxShieldMap[uuid] = value

        // 边界检查：如果最大护盾变小了（比如脱了装备），当前护盾也要削减
        val current = currentShieldMap.getOrDefault(uuid, 0.0)
        if (current > value) {
            currentShieldMap[uuid] = value
        }
    }

    /**
     * 获取当前护盾
     */
    fun getCurrentShield(uuid: UUID): Double {
        return currentShieldMap.getOrDefault(uuid, 0.0)
    }

    /**
     * 获取最大护盾
     */
    fun getMaxShield(uuid: UUID): Double {
        return maxShieldMap.getOrDefault(uuid, 0.0)
    }

    /**
     * 修改当前护盾 (加/减)
     * @param delta 变化量（正数加盾，负数扣盾）
     */
    fun modifyShield(uuid: UUID, delta: Double) {
        val max = maxShieldMap.getOrDefault(uuid, 0.0)
        if (max <= 0) {
            currentShieldMap.remove(uuid)
            return
        }

        val current = currentShieldMap.getOrDefault(uuid, 0.0)
        val newValue = (current + delta).coerceIn(0.0, max)

        currentShieldMap[uuid] = newValue
    }

    /**
     * 护盾抵扣伤害逻辑
     * @param entity 受伤实体
     * @param damage 原始伤害
     * @return 抵扣后剩余的需要扣血的伤害
     */
    fun absorbDamage(entity: LivingEntity, damage: Double): Double {
        val uuid = entity.uniqueId
        val currentShield = getCurrentShield(uuid)

        if (currentShield <= 0) return damage

        return if (currentShield >= damage) {
            modifyShield(uuid, -damage)
            0.0
        } else {
            modifyShield(uuid, -currentShield) // 护盾归零
            damage - currentShield // 返回剩余穿透的伤害
        }
    }

    /**
     * 清理数据 (玩家退服/实体死亡时调用)
     */
    fun removeData(uuid: UUID) {
        currentShieldMap.remove(uuid)
        maxShieldMap.remove(uuid)
    }

    /**
     * 护盾自动恢复任务
     */
    private fun startShieldRegenTask() {
        // ✅ 按照配置文件的频率运行 (20 tick = 1s)
        submit(period = 20) {
            // ✅ 检查配置文件开关
            if (!CoreConfig.shieldAutoRegen) return@submit

            Bukkit.getOnlinePlayers().forEach { p ->
                val max = maxShieldMap.getOrDefault(p.uniqueId, 0.0)
                if (max > 0) {
                    val current = currentShieldMap.getOrDefault(p.uniqueId, 0.0)
                    if (current < max) {
                        // ✅ 使用配置文件的恢复百分比
                        val regen = max * CoreConfig.shieldRegenSpeed
                        modifyShield(p.uniqueId, regen)


                    }
                }
            }
        }
    }
}