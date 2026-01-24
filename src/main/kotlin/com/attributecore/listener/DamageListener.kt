package com.attributecore.listener

import com.attributecore.data.DamageData
import com.attributecore.manager.AttributeManager
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Projectile
import org.bukkit.event.entity.EntityDamageByEntityEvent
import taboolib.common.platform.event.SubscribeEvent

object DamageListener {

    @SubscribeEvent
    fun onDamage(e: EntityDamageByEntityEvent) {
        // 1. 处理攻击者（支持投射物）
        val attacker: LivingEntity? = when (e.damager) {
            is LivingEntity -> e.damager as LivingEntity
            is Projectile -> (e.damager as Projectile).shooter as? LivingEntity
            else -> null
        }

        // 2. 获取防御者
        val defender = e.entity as? LivingEntity ?: return

        if (attacker == null) return

        // 3. 更新双方属性
        AttributeManager.update(attacker)
        AttributeManager.update(defender)

        // 4. 获取属性数据
        val attackData = AttributeManager.getData(attacker)
        val defenceData = AttributeManager.getData(defender)

        // 5. 创建伤害上下文
        val damageData = DamageData(attacker, defender, attackData, defenceData, e)

        // 6. 运行战斗管线
        AttributeManager.getAttributes().forEach { attr ->
            // 攻击方属性效果
            attr.names.forEach { name ->
                val vals = attackData.get(name)
                if (vals[0] > 0) {
                    attr.onAttack(damageData, vals[0], vals[1])
                }
            }

            // 防御方属性效果
            val defVals = defenceData.get(attr.key)
            if (defVals[0] > 0) {
                attr.onDefend(damageData, defVals[0], defVals[1])
            }
        }

        // 7. 应用最终伤害
        e.damage = damageData.getFinalDamage()
    }
}
