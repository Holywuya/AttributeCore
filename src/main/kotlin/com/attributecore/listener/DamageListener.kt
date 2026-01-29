package com.attributecore.listener

import com.attributecore.event.CoreConfig
import com.attributecore.data.DamageData
import com.attributecore.manager.AttributeManager
import com.attributecore.manager.ShieldManager
import com.attributecore.util.DamageContext
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.entity.EntityDamageByEntityEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.console
import java.util.concurrent.ThreadLocalRandom

object DamageListener {

    @SubscribeEvent
    fun onDamage(e: EntityDamageByEntityEvent) {
        val attacker = when (val damager = e.damager) {
            is LivingEntity -> damager
            is Projectile -> damager.shooter as? LivingEntity
            else -> null
        } ?: return
        val defender = e.entity as? LivingEntity ?: return

        val context = DamageContext.get()

        // 1. 拦截纯净伤害 (DOT/技能固定伤害)
        if (context != null && context.isPure) {
            e.damage = context.baseValue
            return
        }

        try {
            // 2. 初始化数据
            val attackData = AttributeManager.getData(attacker)
            val defenceData = AttributeManager.getData(defender)
            val damageData = DamageData(attacker, defender, e)

            if (CoreConfig.debug) {
                console().sendMessage("§7[Debug] §fDamage event: ${attacker.name} -> ${defender.name}, original=${e.damage}")
                console().sendMessage("§7[Debug] §fAttacker data: ${attackData.getNonZeroAttributes()}")
            }

            // 3. 注入计算上下文，这是 "attacker.addDamage()" 能够生效的核心
            DamageContext.setActiveData(damageData)

            // 4. 执行属性循环
            AttributeManager.getAttributes().forEach { attr ->

                // MM 技能过滤
                if (context != null && context.allowedAttributes != null) {
                    if (!context.allowedAttributes.contains(attr.key) && attr.type == com.attributecore.data.AttributeType.ATTACK) {
                        return@forEach
                    }
                }

                // 计算数值
                val atkVals = attackData.get(attr.key)
                var min = atkVals[0]; var max = atkVals[1]

                // MM 技能数值修正
                if (context != null && context.allowedAttributes?.contains(attr.key) == true) {
                    if (context.isClear) { min = context.baseValue; max = context.baseValue }
                    else { min += context.baseValue; max += context.baseValue }
                }

                if (min != 0.0 || max != 0.0) {
                    val roll = if (min == max) min else ThreadLocalRandom.current().nextDouble(Math.min(min, max), Math.max(min, max))
                    if (CoreConfig.debug) {
                        console().sendMessage("§7[Debug] §fAttr: ${attr.key}, min=$min, max=$max, roll=$roll")
                    }
                    // 执行行为 (Native 或 JS)
                    attr.onAttack(damageData, roll, max)
                }

                val defVals = defenceData.get(attr.key)
                if (defVals[0] != 0.0 || defVals[1] != 0.0) {
                    attr.onDefend(damageData, defVals[0], defVals[1])
                }
            }

            // 5. 最终结算
            var finalDamage = damageData.getFinalDamage()

            if (damageData.critTier > 0) handleCritFeedback(attacker, defender, damageData.critTier, finalDamage)

            if (finalDamage > 0) finalDamage = ShieldManager.absorbDamage(defender, finalDamage)

            e.damage = finalDamage

        } catch (ex: Exception) {
            console().sendMessage("§c[AttributeCore] 战斗结算异常: ${ex.message}")
            if (CoreConfig.debug) ex.printStackTrace()
        } finally {
            // ✅ 必须清理上下文
            DamageContext.clear()
        }
    }

    private fun handleCritFeedback(attacker: LivingEntity, defender: LivingEntity, tier: Int, damage: Double) {
        val color = when (tier) { 1 -> "§e"; 2 -> "§6"; 3 -> "§c"; else -> "§4" }
        val suffix = if (tier >= 3) "!".repeat(tier - 2) else ""
        if (attacker is Player) {
            adaptPlayer(attacker).sendActionBar("§f造成了 $color-${damage.toInt()}$suffix §f暴击！")
        }
        attacker.world.playSound(defender.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, (0.6f + tier * 0.2f).coerceAtMost(2.0f))
    }
}