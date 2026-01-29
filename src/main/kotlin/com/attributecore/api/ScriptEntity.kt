package com.attributecore.api

import com.attributecore.util.*
import org.bukkit.entity.LivingEntity
import taboolib.common5.Coerce

class ScriptEntity(val bukkitEntity: LivingEntity) {

    // ===== 伤害操作 (AttributePlus Style) =====
    fun getDamage() = bukkitEntity.getDamage()
    fun setDamage(v: Any) = bukkitEntity.setDamage(Coerce.toDouble(v))
    fun addDamage(v: Any) = bukkitEntity.addDamage(Coerce.toDouble(v))
    fun takeDamage(v: Any) = bukkitEntity.takeDamage(Coerce.toDouble(v))
    fun addBucketDamage(type: String, v: Any) = bukkitEntity.addBucketDamage(type, Coerce.toDouble(v))
    fun addElementalDamage(type: String, v: Any) = bukkitEntity.addElementalDamage(type, Coerce.toDouble(v))
    fun setMultiplier(v: Any) = bukkitEntity.setDamageMultiplier(Coerce.toDouble(v))

    // ===== 暴击系统 =====
    fun rollCrit(v: Any) = bukkitEntity.rollCrit(Coerce.toDouble(v))
    fun setCritTier(v: Any) = bukkitEntity.setCritTier(Coerce.toInteger(v))
    fun addCritDamage(v: Any) = bukkitEntity.addCritDamage(Coerce.toDouble(v))
    fun addCritResistance(v: Any) = bukkitEntity.addCritResistance(Coerce.toDouble(v))
    fun addCritResilience(v: Any) = bukkitEntity.addCritResilience(Coerce.toDouble(v))

    // ===== 防御系统 =====
    fun addDefenseScore(v: Any) = bukkitEntity.addDefenseScore(Coerce.toDouble(v))
    fun addPhysicalDefense(v: Any) = bukkitEntity.addPhysicalDefense(Coerce.toDouble(v))
    fun addMagicalDefense(v: Any) = bukkitEntity.addMagicalDefense(Coerce.toDouble(v))
    fun addFixedPenetration(v: Any) = bukkitEntity.addFixedPenetration(Coerce.toDouble(v))
    fun addPercentPenetration(v: Any) = bukkitEntity.addPercentPenetration(Coerce.toDouble(v))
    fun addFixedPen(v: Any) = addFixedPenetration(v)
    fun addPercentPen(v: Any) = addPercentPenetration(v)

    // ===== 伤害减免 =====
    fun addUniversalReduction(v: Any) = bukkitEntity.addUniversalReduction(Coerce.toDouble(v))
    fun addUniversalFlatReduction(v: Any) = bukkitEntity.addUniversalFlatReduction(Coerce.toDouble(v))
    fun addBucketResistance(type: String, v: Any) = bukkitEntity.addBucketResistance(type, Coerce.toDouble(v))

    // ===== 标签系统 =====
    fun addTag(tag: String) = bukkitEntity.addTag(tag)
    fun hasTag(tag: String) = bukkitEntity.hasTag(tag)
    fun removeTag(tag: String) = bukkitEntity.removeTag(tag)

    // ===== 属性读取 =====
    fun getRandomValue(key: String) = bukkitEntity.getRandomValue(key)
    fun getAttrMin(key: String) = bukkitEntity.getAttrMin(key)
    fun getAttrMax(key: String) = bukkitEntity.getAttrMax(key)
    fun getAttributeValue(key: String) = bukkitEntity.getAttributeValue(key)
    fun getCP() = bukkitEntity.getCP()

    // ===== 护盾与生命 =====
    fun addShield(v: Any) = bukkitEntity.addShield(Coerce.toDouble(v))
    fun getShield() = bukkitEntity.getShield()
    fun heal(v: Any) = bukkitEntity.healSelf(Coerce.toDouble(v))

    // ===== 视觉与交互 =====
    fun tell(msg: String) = JavaScriptAPI.tell(bukkitEntity, msg)
    fun actionbar(msg: String) = JavaScriptAPI.actionbar(bukkitEntity, msg)
    fun sendActionBar(msg: String) = JavaScriptAPI.actionbar(bukkitEntity, msg)
    fun sound(name: String) = JavaScriptAPI.sound(bukkitEntity, name)

    // ===== 原生属性 =====
    fun getName() = bukkitEntity.name
    fun getHealth() = bukkitEntity.health
    fun getMaxHealth() = bukkitEntity.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
    fun getBukkit() = bukkitEntity
}
