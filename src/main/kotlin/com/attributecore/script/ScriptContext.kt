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
    private val _triggerElement: Element? = null,
    private val _auraElement: Element? = null
) {
    /**
     * 触发元素 - 返回字符串以便 JavaScript 比较
     * 例如: "FIRE", "WATER", "ICE", "ELECTRO", "WIND", "PHYSICAL"
     */
    val triggerElement: String?
        get() = _triggerElement?.name
    
    /**
     * 光环元素 - 返回字符串以便 JavaScript 比较
     * 例如: "FIRE", "WATER", "ICE", "ELECTRO", "WIND", "PHYSICAL"
     */
    val auraElement: String?
        get() = _auraElement?.name
    
    /**
     * 获取原始触发元素枚举
     */
    fun getTriggerElementEnum(): Element? = _triggerElement
    
    /**
     * 获取原始光环元素枚举
     */
    fun getAuraElementEnum(): Element? = _auraElement
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
