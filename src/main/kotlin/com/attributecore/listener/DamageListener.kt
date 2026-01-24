package com.attributecore.listener

import com.attributecore.data.DamageData
import com.attributecore.event.CoreConfig
import com.attributecore.manager.AttributeManager
import com.attributecore.manager.ShieldManager
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Projectile
import org.bukkit.event.entity.EntityDamageByEntityEvent
import taboolib.common.platform.event.SubscribeEvent
import java.util.concurrent.ThreadLocalRandom

/**
 * 核心伤害监听器
 * 负责触发所有战斗属性逻辑
 */
object DamageListener {

    @SubscribeEvent
    fun onDamage(e: EntityDamageByEntityEvent) {
        // 1. 获取攻击者 (支持投射物)
        val attacker = when (e.damager) {
            is LivingEntity -> e.damager as LivingEntity
            is Projectile -> (e.damager as Projectile).shooter as? LivingEntity
            else -> null
        } ?: return

        // 2. 获取防御者
        val defender = e.entity as? LivingEntity ?: return

        // 4. 获取属性缓存容器
        val attackData = AttributeManager.getData(attacker)
        val defenceData = AttributeManager.getData(defender)

        // 5. 创建伤害计算上下文
        // 匹配构造函数: DamageData(attacker, defender, event)
        val damageData = DamageData(attacker, defender, e)

        // 6. 按照属性优先级执行逻辑
        AttributeManager.getAttributes().forEach { attr ->

            // --- 处理攻击侧属性 ---
            val atkVals = attackData.get(attr.key)
            if (atkVals[0] > 0 || atkVals[1] > 0) {
                // 如果是范围属性，在攻击时随机取一个值
                val randomValue = if (atkVals[0] == atkVals[1]) {
                    atkVals[0]
                } else {
                    ThreadLocalRandom.current().nextDouble(atkVals[0], atkVals[1])
                }
                // 调用 onAttack (匹配 3 参数: data, value, extraValue)
                attr.onAttack(damageData, randomValue, atkVals[1])
            }

            // --- 处理防御侧属性 ---
            val defVals = defenceData.get(attr.key)
            if (defVals[0] > 0 || defVals[1] > 0) {
                // 防御侧通常取基础值进行计算
                attr.onDefend(damageData, defVals[0], defVals[1])
            }
        }

        // 7. 计算最终物理伤害 (已包含 穿甲、护甲K值、暴击伤害等)
        var finalDamage = damageData.getFinalDamage()

        // 8. 护盾抵扣系统
        // 在最终应用伤害前，先扣除护盾
        if (finalDamage > 0) {
            finalDamage = ShieldManager.absorbDamage(defender, finalDamage)
        }

        if (damageData.isCrit) {
            // 暴击特效逻辑
        }

        // 9. 将结果写回 Bukkit 事件
        e.damage = finalDamage

        // 调试模式输出
         if (CoreConfig.debug) {
            attacker.sendMessage("§7[Debug] §f最终伤害: §c${String.format("%.2f", finalDamage)}")
         }
    }
}