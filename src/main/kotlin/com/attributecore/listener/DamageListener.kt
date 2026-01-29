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
import taboolib.platform.util.isAir
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
            val attackData = AttributeManager.getData(attacker)
            val defenceData = AttributeManager.getData(defender)
            val damageData = DamageData(attacker, defender, e)

            console().sendMessage("§e[AC-DEBUG] §f伤害事件: ${attacker.name} -> ${defender.name}, 原始伤害=${e.damage}")
            console().sendMessage("§e[AC-DEBUG] §f攻击者属性: ${attackData.getNonZeroAttributes()}")
            if (attacker is Player) {
                val mainHand = attacker.inventory.itemInMainHand
                if (!mainHand.isAir()) {
                    console().sendMessage("§e[AC-DEBUG] §f主手物品: ${mainHand.type}, lore=${mainHand.itemMeta?.lore}")
                }
            }

            DamageContext.setActiveData(damageData)

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
                    console().sendMessage("§e[AC-DEBUG] §f属性: ${attr.key}, 范围=[$min, $max], 随机值=$roll, 即将执行脚本")
                    attr.onAttack(damageData, roll, max)
                }

                val defVals = defenceData.get(attr.key)
                if (defVals[0] != 0.0 || defVals[1] != 0.0) {
                    attr.onDefend(damageData, defVals[0], defVals[1])
                }
            }

            // 5. 最终结算
            var finalDamage = damageData.getFinalDamage()
            console().sendMessage("§e[AC-DEBUG] §f最终伤害计算: ${finalDamage} (原始=${e.damage})")

            if (damageData.critTier > 0) handleCritFeedback(attacker, defender, damageData.critTier, finalDamage)

            if (finalDamage > 0) finalDamage = ShieldManager.absorbDamage(defender, finalDamage)

            e.damage = finalDamage
            console().sendMessage("§a[AC-DEBUG] §f最终应用伤害: ${e.damage}")

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