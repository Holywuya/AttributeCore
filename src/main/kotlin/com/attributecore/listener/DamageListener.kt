package com.attributecore.listener


import com.attributecore.data.DamageData
import com.attributecore.manager.AttributeManager
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Projectile
import org.bukkit.event.entity.EntityDamageByEntityEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent

object DamageListener {

    @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onDamage(e: EntityDamageByEntityEvent) {
        val defender = e.entity as? LivingEntity ?: return
        val attacker = when (e.damager) {
            is LivingEntity -> e.damager as LivingEntity
            is Projectile -> (e.damager as Projectile).shooter as? LivingEntity
            else -> null
        } ?: return

        val attackData = AttributeManager.getEntityData(attacker)
        val defenceData = AttributeManager.getEntityData(defender)
        val damageData = DamageData(attacker, defender, attackData, defenceData, e)

        // 运行属性管线
        AttributeManager.attributes.forEach {
            it.onAttack(damageData)
            it.onDefend(damageData)
        }

        e.damage = damageData.getFinalDamage()
    }
}