package com.attributecore.api

import com.attributecore.util.*
import org.bukkit.entity.LivingEntity
import taboolib.common5.Coerce

/**
 * 实体包装器 - 使得在 JS 中可以直接调用 entity.addDamage()
 */
class ScriptEntity(val bukkitEntity: LivingEntity) {

    // --- 战斗能力 ---
    fun addDamage(v: Any) = bukkitEntity.addDamage(Coerce.toDouble(v))
    fun addBucketDamage(type: String, v: Any) = bukkitEntity.addBucketDamage(type, Coerce.toDouble(v))
    fun setMultiplier(v: Any) = bukkitEntity.setDamageMultiplier(Coerce.toDouble(v))
    fun setCritTier(v: Any) = bukkitEntity.setCritTier(Coerce.toInteger(v))
    fun addCritDamage(v: Any) = bukkitEntity.addCritDamage(Coerce.toDouble(v))

    // --- 属性与状态 ---
    fun getRandomValue(key: String) = bukkitEntity.getRandomValue(key)
    fun getAttrMin(key: String) = bukkitEntity.getAttrMin(key)
    fun getAttrMax(key: String) = bukkitEntity.getAttrMax(key)
    fun getCP() = bukkitEntity.getCP()

    fun addShield(v: Any) = bukkitEntity.addShield(Coerce.toDouble(v))
    fun getShield() = bukkitEntity.getShield()
    fun heal(v: Any) = bukkitEntity.healSelf(Coerce.toDouble(v))

    // --- 快捷视觉与交互 ---
    fun tell(msg: String) = JavaScriptAPI.tell(bukkitEntity, msg)
    fun actionbar(msg: String) = JavaScriptAPI.actionbar(bukkitEntity, msg)
    fun sound(name: String) = JavaScriptAPI.sound(bukkitEntity, name)

    // --- 原生属性代理 ---
    fun getName() = bukkitEntity.name
    fun getHealth() = bukkitEntity.health
    fun getMaxHealth() = bukkitEntity.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
    fun getBukkit() = bukkitEntity
}