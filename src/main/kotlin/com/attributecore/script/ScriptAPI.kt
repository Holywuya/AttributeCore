package com.attributecore.script

import com.attributecore.api.AttributeCoreAPI
import com.attributecore.api.ElementAPI
import com.attributecore.data.DamageBucket
import com.attributecore.data.Element
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import taboolib.common.platform.function.submit
import taboolib.platform.util.sendActionBar

@Suppress("unused")
object ScriptAPI {

    @JvmStatic
    fun tell(player: Player, message: String) {
        player.sendMessage(message.replace("&", "§"))
    }

    @JvmStatic
    fun tell(entity: LivingEntity, message: String) {
        if (entity is Player) {
            tell(entity, message)
        }
    }

    @JvmStatic
    fun broadcast(message: String) {
        Bukkit.broadcastMessage(message.replace("&", "§"))
    }

    @JvmStatic
    fun actionbar(player: Player, message: String) {
        player.sendActionBar(message.replace("&", "§"))
    }

    @JvmStatic
    fun title(player: Player, title: String, subtitle: String = "", fadeIn: Int = 10, stay: Int = 70, fadeOut: Int = 20) {
        player.sendTitle(
            title.replace("&", "§"),
            subtitle.replace("&", "§"),
            fadeIn, stay, fadeOut
        )
    }

    @JvmStatic
    fun delay(ticks: Long, action: Runnable) {
        submit(delay = ticks) { action.run() }
    }

    @JvmStatic
    fun async(action: Runnable) {
        submit(async = true) { action.run() }
    }

    @JvmStatic
    fun sync(action: Runnable) {
        submit(async = false) { action.run() }
    }

    @JvmStatic
    fun random(min: Double, max: Double): Double {
        return min + Math.random() * (max - min)
    }

    @JvmStatic
    fun random(min: Int, max: Int): Int {
        return (min..max).random()
    }

    @JvmStatic
    fun chance(percent: Double): Boolean {
        return Math.random() * 100 < percent
    }

    @JvmStatic
    fun getOnlinePlayers(): List<Player> {
        return Bukkit.getOnlinePlayers().toList()
    }

    @JvmStatic
    fun getPlayer(name: String): Player? {
        return Bukkit.getPlayer(name)
    }

    @JvmStatic
    fun getAttribute(entity: LivingEntity, key: String): Double {
        return AttributeCoreAPI.getEntityData(entity)[key]
    }

    @JvmStatic
    fun getAttributeFinal(entity: LivingEntity, key: String): Double {
        return AttributeCoreAPI.getEntityData(entity).getFinal(key)
    }

    @JvmStatic
    fun getResistance(entity: LivingEntity, element: String): Double {
        val el = Element.fromConfigKey(element) ?: return 0.0
        return AttributeCoreAPI.getEntityData(entity).getResistance(el)
    }

    @JvmStatic
    fun applyAura(entity: LivingEntity, element: String, gauge: Double = 1.0) {
        ElementAPI.applyAura(entity, element, gauge)
    }

    @JvmStatic
    fun hasAura(entity: LivingEntity, element: String): Boolean {
        return ElementAPI.hasAura(entity, element)
    }

    @JvmStatic
    fun consumeAura(entity: LivingEntity, element: String, amount: Double = 1.0): Boolean {
        return ElementAPI.consumeAura(entity, element, amount)
    }

    @JvmStatic
    fun clearAura(entity: LivingEntity) {
        ElementAPI.clearAura(entity, null as String?)
    }

    @JvmStatic
    fun getAuraElement(entity: LivingEntity): String? {
        return ElementAPI.getAura(entity)?.element?.configKey
    }

    @JvmStatic
    fun getAuraGauge(entity: LivingEntity): Double {
        return ElementAPI.getAura(entity)?.gauge ?: 0.0
    }

    @JvmStatic
    fun heal(entity: LivingEntity, amount: Double) {
        val maxHealth = entity.maxHealth
        entity.health = (entity.health + amount).coerceIn(0.0, maxHealth)
    }

    @JvmStatic
    fun damage(attacker: LivingEntity?, victim: LivingEntity, amount: Double) {
        if (attacker != null) {
            victim.damage(amount, attacker)
        } else {
            victim.damage(amount)
        }
    }

    @JvmStatic
    fun createDamageBucket(): DamageBucket {
        return DamageBucket()
    }

    @JvmStatic
    fun addElementDamage(bucket: DamageBucket, element: String, amount: Double) {
        val el = Element.fromConfigKey(element) ?: Element.PHYSICAL
        bucket.add(el, amount)
    }

    @JvmStatic
    fun getElement(name: String): Element? {
        return Element.fromConfigKey(name)
    }

    @JvmStatic
    fun getElements(): List<String> {
        return Element.entries.map { it.configKey }
    }

    @JvmStatic
    fun log(message: String) {
        taboolib.common.platform.function.info("[ScriptAPI] $message")
    }

    @JvmStatic
    fun warn(message: String) {
        taboolib.common.platform.function.warning("[ScriptAPI] $message")
    }

    @JvmStatic
    fun getNearbyEntities(entity: LivingEntity, radius: Double): List<LivingEntity> {
        return entity.getNearbyEntities(radius, radius, radius)
            .filterIsInstance<LivingEntity>()
    }

    @JvmStatic
    fun distance(entity1: LivingEntity, entity2: LivingEntity): Double {
        return entity1.location.distance(entity2.location)
    }

    @JvmStatic
    fun playSound(player: Player, sound: String, volume: Float = 1.0f, pitch: Float = 1.0f) {
        try {
            val soundEnum = org.bukkit.Sound.valueOf(sound.uppercase())
            player.playSound(player.location, soundEnum, volume, pitch)
        } catch (e: Exception) {
            player.playSound(player.location, sound, volume, pitch)
        }
    }

    @JvmStatic
    fun runCommand(command: String) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
    }

    @JvmStatic
    fun runCommandAsPlayer(player: Player, command: String) {
        player.performCommand(command)
    }
}
