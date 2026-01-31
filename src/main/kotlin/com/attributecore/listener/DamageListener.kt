package com.attributecore.listener

import com.attributecore.data.*
import com.attributecore.event.DamageEventData
import com.attributecore.event.DefenceEventData
import com.attributecore.manager.AttributeManager
import com.attributecore.script.ScriptContext
import com.attributecore.script.ScriptManager
import com.attributecore.script.ScriptPhase
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

        val damageBucket = attackerData.buildDamageBucket()
        if (damageBucket.total() <= 0) {
            damageBucket[Element.PHYSICAL] = event.damage
        }
        DebugLogger.logDamageCalculation("初始伤害桶: $damageBucket")

        val preDamageContext = ScriptContext(
            phase = ScriptPhase.PRE_DAMAGE,
            attacker = attacker,
            victim = victim,
            attackerData = attackerData,
            victimData = victimData,
            damageBucket = damageBucket
        )
        ScriptManager.executePhase(ScriptPhase.PRE_DAMAGE, preDamageContext)

        if (preDamageContext.cancelled) {
            event.isCancelled = true
            DebugLogger.logDamageCalculation("伤害被脚本取消")
            return
        }

        damageBucket.multiplyAll(preDamageContext.damageMultiplier)
        DebugLogger.logDamageCalculation("PRE_DAMAGE 后: $damageBucket, 倍率: ${preDamageContext.damageMultiplier}")

        val attackEvent = DamageEventData(attacker, victim, event)
        attackEvent.damage = damageBucket.total()
        SubAttribute.getAttributes()
            .filter { it.containsType(AttributeType.Attack) }
            .forEach { it.eventMethod(attackerData, attackEvent) }

        processElementalReaction(attacker, victim, damageBucket, attackerData, victimData)

        val resistances = victimData.getAllResistances()
        damageBucket.applyResistances(resistances)
        DebugLogger.logDamageCalculation("抗性计算后: $damageBucket")

        val defenceEvent = DefenceEventData(victim, attacker, event)
        defenceEvent.damage = damageBucket.total()
        SubAttribute.getAttributes()
            .filter { it.containsType(AttributeType.Defence) }
            .forEach { it.eventMethod(victimData, defenceEvent) }

        val postDamageContext = ScriptContext(
            phase = ScriptPhase.POST_DAMAGE,
            attacker = attacker,
            victim = victim,
            attackerData = attackerData,
            victimData = victimData,
            damageBucket = damageBucket
        )
        ScriptManager.executePhase(ScriptPhase.POST_DAMAGE, postDamageContext)

        val finalDamage = defenceEvent.damage * postDamageContext.damageMultiplier
        event.damage = finalDamage.coerceAtLeast(0.0)
        DebugLogger.logDamageCalculation("最终伤害: ${event.damage}")
    }

    private fun processElementalReaction(
        attacker: LivingEntity,
        victim: LivingEntity,
        damageBucket: DamageBucket,
        attackerData: AttributeData,
        victimData: AttributeData
    ) {
        if (!damageBucket.hasElementalDamage()) return

        val existingAura = ElementalAura.getAura(victim)
        val triggerElement = damageBucket.elements().firstOrNull { it != Element.PHYSICAL } ?: return

        if (existingAura != null && existingAura.element != triggerElement) {
            val reactionContext = ScriptContext(
                phase = ScriptPhase.REACTION,
                attacker = attacker,
                victim = victim,
                attackerData = attackerData,
                victimData = victimData,
                damageBucket = damageBucket,
                triggerElement = triggerElement,
                auraElement = existingAura.element
            )
            ScriptManager.executePhase(ScriptPhase.REACTION, reactionContext)

            if (!reactionContext.cancelled) {
                ElementalAura.consumeAura(victim, existingAura.element)
                damageBucket.multiplyAll(reactionContext.damageMultiplier)
                DebugLogger.logDamageCalculation(
                    "元素反应: ${existingAura.element.displayName} + ${triggerElement.displayName}, 倍率: ${reactionContext.damageMultiplier}"
                )
            }
        } else {
            ElementalAura.applyAura(victim, triggerElement)
            DebugLogger.logDamageCalculation("附着元素: ${triggerElement.displayName}")
        }
    }
}
