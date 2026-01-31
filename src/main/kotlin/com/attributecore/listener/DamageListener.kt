package com.attributecore.listener

import com.attributecore.data.*
import com.attributecore.event.DamageEventData
import com.attributecore.event.DefenceEventData
import com.attributecore.event.KillerEventData
import com.attributecore.manager.AttributeManager
import com.attributecore.script.AttributeHandle
import com.attributecore.script.JsAttribute
import com.attributecore.script.ScriptContext
import com.attributecore.script.ScriptManager
import com.attributecore.script.ScriptPhase
import com.attributecore.util.DebugLogger
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Projectile
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import taboolib.common.platform.event.SubscribeEvent

object DamageListener {
    
    @SubscribeEvent
    fun onDamage(event: EntityDamageByEntityEvent) {
        val (attacker, isProjectile) = resolveAttacker(event) ?: return
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

        val handle = AttributeHandle.fromDamageBucket(attacker, victim, damageBucket)
        handle.setAttackerData(attackerData)
        handle.setEntityData(victimData)

        val attackEvent = DamageEventData(attacker, victim, event)
        attackEvent.damage = damageBucket.total()
        SubAttribute.getAttributes()
            .filter { it.containsType(AttributeType.Attack) && it !is JsAttribute }
            .forEach { it.eventMethod(attackerData, attackEvent) }

        if (attackEvent.damage != damageBucket.total()) {
            val ratio = if (damageBucket.total() > 0) attackEvent.damage / damageBucket.total() else 1.0
            handle.getDamageBucket().multiplyAll(ratio)
        }

        executeJsAttackAttributes(attacker, victim, attackerData, handle)

        if (handle.isCancelled()) {
            event.isCancelled = true
            DebugLogger.logDamageCalculation("伤害被 JS 属性取消")
            return
        }

        val handleBucket = handle.getDamageBucket()
        damageBucket.clear()
        damageBucket.merge(handleBucket)
        DebugLogger.logDamageCalculation("Attack 阶段后伤害桶: $damageBucket")

        processElementalReaction(attacker, victim, damageBucket, attackerData, victimData)

        val resistances = victimData.getAllResistances()
        damageBucket.applyResistances(resistances)
        DebugLogger.logDamageCalculation("抗性计算后: $damageBucket")

        handle.setDamageBucket(damageBucket)

        val defenceEvent = DefenceEventData(victim, attacker, event)
        defenceEvent.damage = damageBucket.total()
        SubAttribute.getAttributes()
            .filter { it.containsType(AttributeType.Defence) && it !is JsAttribute }
            .forEach { it.eventMethod(victimData, defenceEvent) }

        if (defenceEvent.damage != damageBucket.total()) {
            val ratio = if (damageBucket.total() > 0) defenceEvent.damage / damageBucket.total() else 1.0
            handle.getDamageBucket().multiplyAll(ratio)
        }

        executeJsDefenseAttributes(victim, attacker, victimData, handle)

        if (handle.isCancelled()) {
            event.isCancelled = true
            DebugLogger.logDamageCalculation("伤害被 JS 防御属性取消")
            return
        }

        val postDamageContext = ScriptContext(
            phase = ScriptPhase.POST_DAMAGE,
            attacker = attacker,
            victim = victim,
            attackerData = attackerData,
            victimData = victimData,
            damageBucket = damageBucket
        )
        ScriptManager.executePhase(ScriptPhase.POST_DAMAGE, postDamageContext)

        val baseDamage = handle.getDamage()
        val finalDamage = baseDamage * postDamageContext.damageMultiplier
        event.damage = finalDamage.coerceAtLeast(0.0)
        DebugLogger.logDamageCalculation("最终伤害: ${event.damage} (基础: $baseDamage, POST倍率: ${postDamageContext.damageMultiplier})")
    }

    @SubscribeEvent
    fun onEntityDeath(event: EntityDeathEvent) {
        val victim = event.entity
        val killer = victim.killer ?: return

        val killerData = AttributeManager.getEntityData(killer)
        val victimData = AttributeManager.getEntityData(victim)

        val handle = AttributeHandle(
            attacker = killer,
            entity = victim,
            initialDamage = 0.0
        )
        handle.setAttackerData(killerData)
        handle.setEntityData(victimData)

        executeJsKillerAttributes(killer, victim, killerData, handle)

        DebugLogger.logDamageCalculation("击杀事件: ${killer.name} 击杀了 ${victim.name}")
    }

    private fun resolveAttacker(event: EntityDamageByEntityEvent): Pair<LivingEntity, Boolean>? {
        val damager = event.damager
        return when {
            damager is LivingEntity -> damager to false
            damager is Projectile -> {
                val shooter = damager.shooter
                if (shooter is LivingEntity) shooter to true else null
            }
            else -> null
        }
    }

    private fun executeJsAttackAttributes(
        attacker: LivingEntity,
        victim: LivingEntity,
        attackerData: AttributeData,
        handle: AttributeHandle
    ) {
        SubAttribute.getAttributes()
            .filterIsInstance<JsAttribute>()
            .filter { it.containsType(AttributeType.Attack) }
            .sortedBy { it.priority }
            .forEach { jsAttr ->
                if (!handle.isCancelled()) {
                    val attrValue = attackerData[jsAttr.name]
                    if (attrValue > 0) {
                        DebugLogger.logDamageCalculation("执行 JS Attack 属性: ${jsAttr.name}, 值: $attrValue")
                        jsAttr.runAttack(attacker, victim, handle)
                    }
                }
            }
    }

    private fun executeJsDefenseAttributes(
        victim: LivingEntity,
        attacker: LivingEntity?,
        victimData: AttributeData,
        handle: AttributeHandle
    ) {
        SubAttribute.getAttributes()
            .filterIsInstance<JsAttribute>()
            .filter { it.containsType(AttributeType.Defence) }
            .sortedBy { it.priority }
            .forEach { jsAttr ->
                if (!handle.isCancelled()) {
                    val attrValue = victimData[jsAttr.name]
                    if (attrValue > 0) {
                        DebugLogger.logDamageCalculation("执行 JS Defence 属性: ${jsAttr.name}, 值: $attrValue")
                        jsAttr.runDefense(victim, attacker, handle)
                    }
                }
            }
    }

    private fun executeJsKillerAttributes(
        killer: LivingEntity,
        victim: LivingEntity,
        killerData: AttributeData,
        handle: AttributeHandle
    ) {
        SubAttribute.getAttributes()
            .filterIsInstance<JsAttribute>()
            .filter { it.containsType(AttributeType.Killer) }
            .sortedBy { it.priority }
            .forEach { jsAttr ->
                val attrValue = killerData[jsAttr.name]
                if (attrValue > 0) {
                    DebugLogger.logDamageCalculation("执行 JS Killer 属性: ${jsAttr.name}, 值: $attrValue")
                    jsAttr.runKiller(killer, victim, handle)
                }
            }
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
                _triggerElement = triggerElement,
                _auraElement = existingAura.element
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
