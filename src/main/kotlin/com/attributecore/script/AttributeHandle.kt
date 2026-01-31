package com.attributecore.script

import com.attributecore.data.AttributeData
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile

class AttributeHandle(
    private val attacker: LivingEntity?,
    private val entity: LivingEntity?,
    private var damage: Double,
    private val isProjectile: Boolean = false,
    private val isSkillDamage: Boolean = false
) {
    private var cancelled = false
    
    private var attackerData: AttributeData? = null
    private var entityData: AttributeData? = null

    fun getAttackerOrKiller(): LivingEntity? = attacker
    
    fun getAttacker(): LivingEntity? = attacker

    fun getEntity(): LivingEntity? = entity
    
    fun getVictim(): LivingEntity? = entity

    fun getDamage(): Double = damage
    
    fun getDamage(entity: LivingEntity?): Double = damage

    fun setDamage(value: Double) {
        damage = value.coerceAtLeast(0.0)
    }
    
    fun setDamage(entity: LivingEntity?, value: Double) {
        damage = value.coerceAtLeast(0.0)
    }

    fun addDamage(value: Double) {
        damage = (damage + value).coerceAtLeast(0.0)
    }
    
    fun addDamage(entity: LivingEntity?, value: Double) {
        damage = (damage + value).coerceAtLeast(0.0)
    }

    fun takeDamage(value: Double) {
        damage = (damage - value).coerceAtLeast(0.0)
    }
    
    fun takeDamage(entity: LivingEntity?, value: Double) {
        damage = (damage - value).coerceAtLeast(0.0)
    }

    fun isCancelled(): Boolean = cancelled

    fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }

    fun isProjectile(): Boolean = isProjectile

    fun isSkillDamage(): Boolean = isSkillDamage
    
    fun getAttackerData(): AttributeData? = attackerData
    
    fun setAttackerData(data: AttributeData) {
        attackerData = data
    }
    
    fun getEntityData(): AttributeData? = entityData
    
    fun setEntityData(data: AttributeData) {
        entityData = data
    }

    fun heal(entity: LivingEntity?, amount: Double) {
        if (entity is Player) {
            entity.health = (entity.health + amount).coerceAtMost(entity.maxHealth)
        } else if (entity is LivingEntity) {
            entity.health = (entity.health + amount).coerceAtMost(entity.maxHealth)
        }
    }

    fun sendMessage(entity: LivingEntity?, message: String) {
        if (entity is Player) {
            entity.sendMessage(message)
        }
    }

    fun sendActionBar(entity: LivingEntity?, message: String) {
        if (entity is Player) {
            entity.sendMessage(message)
        }
    }

    companion object {
        fun fromDamageEvent(
            attacker: LivingEntity?,
            victim: LivingEntity,
            damage: Double,
            projectile: Projectile? = null
        ): AttributeHandle {
            return AttributeHandle(
                attacker = attacker,
                entity = victim,
                damage = damage,
                isProjectile = projectile != null
            )
        }
    }
}
