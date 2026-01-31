package com.attributecore.event

import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageByEntityEvent

sealed class EventData {
    abstract val entity: LivingEntity
}

data class DamageEventData(
    val attacker: LivingEntity,
    val victim: LivingEntity,
    val event: EntityDamageByEntityEvent
) : EventData() {
    override val entity: LivingEntity = attacker
    var damage: Double = event.damage
}

data class DefenceEventData(
    override val entity: LivingEntity,
    val attacker: LivingEntity,
    val event: EntityDamageByEntityEvent
) : EventData() {
    var damage: Double = event.damage
}

data class UpdateEventData(
    override val entity: LivingEntity
) : EventData()

data class KillerEventData(
    val killer: LivingEntity,
    val victim: LivingEntity
) : EventData() {
    override val entity: LivingEntity = killer
}
