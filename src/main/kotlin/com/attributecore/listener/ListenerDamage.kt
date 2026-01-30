package com.attributecore.listener

import com.attributecore.AttributeCore
import com.attributecore.data.attribute.AttributeType
import com.attributecore.data.attribute.SubAttribute
import com.attributecore.data.attribute.SXAttributeData
import com.attributecore.data.eventdata.sub.DamageData
import com.attributecore.util.Config
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.inventory.EntityEquipment
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class ListenerDamage : Listener {

    private val projectileData = ConcurrentHashMap<UUID, SXAttributeData>()

    @EventHandler
    fun onProjectileLaunch(event: EntityShootBowEvent) {
        if (event.isCancelled) return
        
        val shooter = event.entity
        if (shooter is LivingEntity) {
            val data = AttributeCore.attributeManager.getEntityData(shooter)
            projectileData[event.projectile.uniqueId] = data
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        if (event.isCancelled || Config.damageEventBlackList.contains(event.cause.name)) {
            return
        }

        val defender = event.entity as? LivingEntity ?: return
        if (defender is ArmorStand) return

        var attacker: LivingEntity? = null
        var attackData: SXAttributeData? = null

        when (val damager = event.damager) {
            is Projectile -> {
                val shooter = damager.shooter
                if (shooter is LivingEntity) {
                    attacker = shooter
                    attackData = projectileData[damager.uniqueId]
                }
            }
            is LivingEntity -> {
                attacker = damager
            }
        }

        if (defender == null || attacker == null) return
        if (!Config.isDamageCalculationToEVE && defender !is Player && attacker !is Player) return

        val defenderData = AttributeCore.attributeManager.getEntityData(defender)
        attackData = attackData ?: AttributeCore.attributeManager.getEntityData(attacker)

        val defenderName = defender.name
        val attackerName = attacker.name

        val damageData = DamageData(
            defender, attacker,
            defenderName, attackerName,
            defenderData, attackData,
            event
        )

        SubAttribute.getAttributes().forEach { attr ->
            when {
                attr.containsType(AttributeType.ATTACK) && attackData.isValid(attr) -> {
                    attr.eventMethod(attackData.getValues(attr), damageData)
                }
                attr.containsType(AttributeType.DEFENCE) && defenderData.isValid(attr) -> {
                    attr.eventMethod(defenderData.getValues(attr), damageData)
                }
            }

            if (damageData.isCancelled() || damageData.getDamage() <= 0) {
                damageData.setDamage(Config.minimumDamage)
                return@forEach
            }
        }

        event.damage = damageData.getDamage()
    }
}