package com.attributecore.listener

import com.attributecore.data.DamageData
import com.attributecore.event.CoreConfig
import com.attributecore.manager.AttributeManager
import com.attributecore.manager.ShieldManager
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.entity.EntityDamageByEntityEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.console
import taboolib.platform.util.sendActionBar
import java.util.concurrent.ThreadLocalRandom

/**
 * 核心伤害监听器
 * 负责驱动整个战斗计算管线
 */
object DamageListener {

    @SubscribeEvent
    fun onDamage(e: EntityDamageByEntityEvent) {
        // 1. 基础过滤：仅处理涉及生物的伤害
        val attacker = when (val damager = e.damager) {
            is LivingEntity -> damager
            is Projectile -> damager.shooter as? LivingEntity
            else -> null
        } ?: return

        val defender = e.entity as? LivingEntity ?: return

        // 2. 异常保底：防止计算过程报错导致停服或伤害异常
        try {
            // 3. 获取属性缓存 (注意：此处不调用 update 以保证 TPS 稳定)
            val attackData = AttributeManager.getData(attacker)
            val defenceData = AttributeManager.getData(defender)

            // 4. 初始化伤害计算上下文
            val damageData = DamageData(attacker, defender, e)

            // 5. 按照优先级顺序执行属性逻辑
            AttributeManager.getAttributes().forEach { attr ->

                // --- A. 攻击侧逻辑 ---
                val atkVals = attackData.get(attr.key)
                // 如果属性值不为 0 (最小值或最大值)
                if (atkVals[0] != 0.0 || atkVals[1] != 0.0) {
                    // 计算本次攻击的随机 Roll 点 (min - max)
                    val rollValue = if (atkVals[0] == atkVals[1]) {
                        atkVals[0]
                    } else {
                        ThreadLocalRandom.current().nextDouble(atkVals[0], atkVals[1])
                    }
                    // 执行攻击行为 (传入 3 参数：上下文, 随机值, 最大值)
                    attr.onAttack(damageData, rollValue, atkVals[1])
                }

                // --- B. 防御侧逻辑 ---
                val defVals = defenceData.get(attr.key)
                if (defVals[0] != 0.0 || defVals[1] != 0.0) {
                    // 防御端通常采用基础值(min)计算，也可根据需求改为随机
                    attr.onDefend(damageData, defVals[0], defVals[1])
                }
            }

            // 6. 获取最终计算伤害 (已含：穿甲、护甲K值、百分比抗性、阶梯暴击倍率)
            var finalDamage = damageData.getFinalDamage()

            // 7. 处理暴击视觉反馈 (Warframe 风格)
            if (damageData.critTier > 0) {
                handleCritFeedback(attacker, defender, damageData.critTier, finalDamage)
            }

            // 8. 护盾抵扣系统
            // 护盾在所有属性计算之后、最终扣血之前触发
            if (finalDamage > 0) {
                finalDamage = ShieldManager.absorbDamage(defender, finalDamage)
            }

            // 9. 应用伤害到 Bukkit 事件
            e.damage = finalDamage

            // 调试模式
            if (CoreConfig.debug) {
                val info = "§7[Debug] §f${attacker.name} -> ${defender.name} | 暴击层级: ${damageData.critTier} | 最终伤害: ${String.format("%.2f", finalDamage)}"
                console().sendMessage(info)
            }

        } catch (ex: Exception) {
            // 终极保底逻辑：如果插件逻辑崩溃，恢复原版伤害，确保游戏不中断
            console().sendMessage("§c[AttributeCore] 伤害结算发生严重异常！已恢复原版伤害。")
            if (CoreConfig.debug) ex.printStackTrace()
        }
    }

    /**
     * 处理阶梯暴击的视觉和声音反馈
     */
    private fun handleCritFeedback(attacker: LivingEntity, defender: LivingEntity, tier: Int, damage: Double) {
        val loc = defender.location.add(0.0, 1.0, 0.0)

        // 1. 颜色与前缀定义
        val critColor = when (tier) {
            1 -> "§e" // 黄暴
            2 -> "§6" // 橙暴
            3 -> "§c" // 红暴
            else -> "§4" // 深红暴 (4级以上)
        }

        // 2. 格式化文本 (4级以上增加感叹号)
        val suffix = if (tier >= 4) "!".repeat(tier - 2) else ""
        val critMsg = "$critColor-${damage.toInt()}$suffix"

        // 3. 发送反馈给攻击者 (ActionBar 避免刷屏)
        if (attacker is Player) {
            
            attacker.sendActionBar("§f造成了 $critMsg 暴击伤害！")
        }

    }
}