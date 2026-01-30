package com.attributecore.data.eventdata.sub

import com.attributecore.data.attribute.SXAttributeData
import com.attributecore.data.eventdata.EventData
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageByEntityEvent
import java.util.UUID

class DamageData(
    val defender: LivingEntity,
    val attacker: LivingEntity,
    val defenderName: String,
    val attackerName: String,
    val defenderData: SXAttributeData,
    val attackerData: SXAttributeData,
    val event: EntityDamageByEntityEvent
) : EventData {

    private var damage: Double = 0.0
    private var isCancelled: Boolean = false
    private var isCrit: Boolean = false
    private var fromAPI: Boolean = false
    private val tags = mutableSetOf<String>()
    private val holoMessages = mutableListOf<String>()

    override fun getEntity(): LivingEntity = defender

    fun getDamage(): Double = damage

    fun setDamage(value: Double) {
        damage = value.coerceAtLeast(0.0)
    }

    fun addDamage(value: Double) {
        damage += value
    }

    fun isCancelled(): Boolean = isCancelled

    fun setCancelled(value: Boolean) {
        isCancelled = value
    }

    fun isCrit(): Boolean = isCrit

    fun setCrit(value: Boolean) {
        isCrit = value
    }

    fun isFromAPI(): Boolean = fromAPI

    fun setFromAPI(value: Boolean) {
        fromAPI = value
    }

    fun addTag(tag: String) {
        tags.add(tag.uppercase())
    }

    fun hasTag(tag: String): Boolean {
        return tags.contains(tag.uppercase())
    }

    fun getTags(): Set<String> = tags.toSet()

    fun sendHolo(message: String) {
        holoMessages.add(message)
    }

    fun getHoloMessages(): List<String> = holoMessages.toList()

    fun clearHoloMessages() {
        holoMessages.clear()
    }

    override fun toString(): String {
        return "DamageData(defender=$defenderName, attacker=$attackerName, damage=$damage, crit=$isCrit, cancelled=$isCancelled)"
    }
}