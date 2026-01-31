package com.attributecore.script

import com.attributecore.data.AttributeData
import com.attributecore.data.DamageBucket
import com.attributecore.data.Elements
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile

class AttributeHandle(
    private val attacker: LivingEntity?,
    private val entity: LivingEntity?,
    initialDamage: Double,
    private val isProjectile: Boolean = false,
    private val isSkillDamage: Boolean = false
) {
    private var cancelled = false
    
    private var attackerData: AttributeData? = null
    private var entityData: AttributeData? = null
    
    private val damageBucket: DamageBucket = DamageBucket.physical(initialDamage)
    private var finalDamageModifier: Double = 0.0

    fun getAttackerOrKiller(): LivingEntity? = attacker
    
    fun getAttacker(): LivingEntity? = attacker

    fun getEntity(): LivingEntity? = entity
    
    fun getVictim(): LivingEntity? = entity

    fun getDamage(): Double = damageBucket.total() + finalDamageModifier
    
    fun getDamage(entity: LivingEntity?): Double = getDamage()
    
    fun getDamage(element: String): Double = damageBucket[element]

    fun setDamage(value: Double) {
        damageBucket.clear()
        damageBucket[Elements.PHYSICAL] = value.coerceAtLeast(0.0)
    }
    
    fun setDamage(entity: LivingEntity?, value: Double) {
        setDamage(value)
    }
    
    fun setDamage(element: String, value: Double) {
        damageBucket[element] = value.coerceAtLeast(0.0)
    }

    fun addDamage(value: Double) {
        damageBucket.add(Elements.PHYSICAL, value)
    }
    
    fun addDamage(entity: LivingEntity?, value: Double) {
        addDamage(value)
    }
    
    fun addDamage(element: String, value: Double) {
        damageBucket.add(element, value)
    }

    fun takeDamage(value: Double) {
        val current = damageBucket[Elements.PHYSICAL]
        damageBucket[Elements.PHYSICAL] = (current - value).coerceAtLeast(0.0)
    }
    
    fun takeDamage(entity: LivingEntity?, value: Double) {
        takeDamage(value)
    }
    
    fun takeDamage(element: String, value: Double) {
        val current = damageBucket[element]
        damageBucket[element] = (current - value).coerceAtLeast(0.0)
    }
    
    fun addFinalDamage(value: Double) {
        finalDamageModifier += value
    }
    
    fun takeFinalDamage(value: Double) {
        finalDamageModifier -= value
    }
    
    fun setFinalDamage(value: Double) {
        finalDamageModifier = 0.0
        damageBucket.clear()
        damageBucket[Elements.PHYSICAL] = value.coerceAtLeast(0.0)
    }
    
    fun getFinalDamageModifier(): Double = finalDamageModifier
    
    fun getDamageBucket(): DamageBucket = damageBucket
    
    fun setDamageBucket(bucket: DamageBucket) {
        damageBucket.clear()
        damageBucket.merge(bucket)
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
                initialDamage = damage,
                isProjectile = projectile != null
            )
        }
        
        fun fromDamageBucket(
            attacker: LivingEntity?,
            victim: LivingEntity,
            bucket: DamageBucket,
            projectile: Projectile? = null
        ): AttributeHandle {
            return AttributeHandle(
                attacker = attacker,
                entity = victim,
                initialDamage = 0.0,
                isProjectile = projectile != null
            ).apply {
                setDamageBucket(bucket)
            }
        }
    }
}
