package com.attributecore.api

import com.attributecore.util.DamageContext
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.submit
import java.util.concurrent.ThreadLocalRandom

object JavaScriptAPI {

    @JvmStatic
    fun chance(p: Number) = ThreadLocalRandom.current().nextDouble(100.0) < p.toDouble()

    @JvmStatic
    fun broadcast(msg: String) = Bukkit.broadcastMessage(msg.replace("&", "§"))

    @JvmStatic
    fun tell(e: LivingEntity, msg: String) = e.sendMessage(msg.replace("&", "§"))

    @JvmStatic
    fun actionbar(e: LivingEntity, msg: String) {
        if (e is org.bukkit.entity.Player) adaptPlayer(e).sendActionBar(msg.replace("&", "§"))
    }

    @JvmStatic
    fun sound(e: LivingEntity, name: String) = try {
        (e as? org.bukkit.entity.Player)?.playSound(e.location, org.bukkit.Sound.valueOf(name.uppercase()), 1f, 1f)
    } catch(t: Throwable) {}


    @JvmStatic
    fun dot(target: LivingEntity, period: Long, count: Int, damage: Number, attacker: LivingEntity?) {
        var remaining = count
        submit(period = period) {
            if (remaining <= 0 || !target.isValid || target.isDead) { this.cancel(); return@submit }
            try {
                DamageContext.set(null, damage.toDouble(), isClear = true, isPure = true)
                target.damage(damage.toDouble(), attacker)
            } finally { DamageContext.clear(); remaining-- }
        }
    }
}