package com.attributecore.listener

import com.attributecore.data.DamageData
import com.attributecore.manager.AttributeManager
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Projectile
import org.bukkit.event.entity.EntityDamageByEntityEvent
import taboolib.common.platform.event.SubscribeEvent
import java.util.concurrent.ThreadLocalRandom

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

        // 仅处理生物之间的伤害
        if (attacker == null) return

        // 3. 更新双方属性（从装备的 Lore/NBT 重新扫描）
        AttributeManager.update(attacker)
        AttributeManager.update(defender)

        // 4. 获取属性数据容器
        val attackData = AttributeManager.getData(attacker)
        val defenceData = AttributeManager.getData(defender)

        // 5. 创建伤害上下文 (匹配你 DamageData 的 3 参数构造函数)
        val damageData = DamageData(attacker, defender, e)

        // 6. 运行战斗管线 (按照属性优先级排序执行)
        AttributeManager.getAttributes().forEach { attr ->

            // --- 处理攻击方属性 ---
            val atkVals = attackData.get(attr.key) // 获取 [最小值, 最大值]
            if (atkVals[0] > 0 || atkVals[1] > 0 && atkVals[1] >= atkVals[0]) {

                val randomValue = if (atkVals[0] == atkVals[1]) {
                    atkVals[0]
                } else {
                    ThreadLocalRandom.current().nextDouble(atkVals[0], atkVals[1])
                }

                attr.onAttack(damageData, atkVals[0], atkVals[1])
            }

            // --- 处理防御方属性 ---
            val defVals = defenceData.get(attr.key)
            if (defVals[0] > 0 || defVals[1] > 0 && atkVals[1] >= atkVals[0]) {

                attr.onDefend(damageData, defVals[0], defVals[1])
            }
        }

        e.damage = damageData.getFinalDamage()
    }
}