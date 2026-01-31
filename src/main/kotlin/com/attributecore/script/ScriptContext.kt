package com.attributecore.script

import com.attributecore.data.AttributeData
import com.attributecore.data.DamageBucket
import com.attributecore.data.Element
import org.bukkit.entity.LivingEntity

class ScriptContext(
    val phase: ScriptPhase,
    val attacker: LivingEntity? = null,
    val victim: LivingEntity? = null,
    val attackerData: AttributeData? = null,
    val victimData: AttributeData? = null,
    val damageBucket: DamageBucket? = null,
    val triggerElement: Element? = null,
    val auraElement: Element? = null
) {
    var cancelled: Boolean = false
    var damageMultiplier: Double = 1.0
    val customData: MutableMap<String, Any> = mutableMapOf()

    fun cancel() {
        cancelled = true
    }

    fun multiplyDamage(multiplier: Double) {
        damageMultiplier *= multiplier
    }

    fun setCustom(key: String, value: Any) {
        customData[key] = value
    }

    fun getCustom(key: String): Any? = customData[key]
}
