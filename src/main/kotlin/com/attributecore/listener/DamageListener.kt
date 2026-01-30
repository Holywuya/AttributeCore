package com.attributecore.listener

import com.attributecore.data.AttributeType
import com.attributecore.data.SubAttribute
import com.attributecore.event.DamageEventData
import com.attributecore.event.DefenceEventData
import com.attributecore.manager.AttributeManager
import com.attributecore.util.DebugLogger
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageByEntityEvent
import taboolib.common.platform.event.SubscribeEvent

object DamageListener {
    @SubscribeEvent
    fun onDamage(event: EntityDamageByEntityEvent) {
        val attacker = event.damager as? LivingEntity ?: return
        val victim = event.entity as? LivingEntity ?: return

        DebugLogger.logDamageCalculation("伤害事件触发: ${attacker.name} -> ${victim.name}, 基础伤害: ${event.damage}")

        val attackerData = AttributeManager.getEntityData(attacker)
        val victimData = AttributeManager.getEntityData(victim)

        DebugLogger.logDamageCalculation("攻击者属性: ${attackerData.getNonZeroAttributes()}")
        DebugLogger.logDamageCalculation("防御者属性: ${victimData.getNonZeroAttributes()}")

        val attackEvent = DamageEventData(attacker, victim, event)
        SubAttribute.getAttributes()
            .filter { it.containsType(AttributeType.Attack) }
            .forEach { it.eventMethod(attackerData, attackEvent) }

        val defenceEvent = DefenceEventData(victim, attacker, event)
        defenceEvent.damage = attackEvent.damage
        SubAttribute.getAttributes()
            .filter { it.containsType(AttributeType.Defence) }
            .forEach { it.eventMethod(victimData, defenceEvent) }

        event.damage = defenceEvent.damage
        DebugLogger.logDamageCalculation("最终伤害: ${event.damage}")
    }
}
