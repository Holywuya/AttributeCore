package com.attributecore.api

import com.attributecore.util.*
import org.bukkit.entity.LivingEntity
import taboolib.common5.Coerce

/**
 * ScriptEntity - 为JavaScript脚本提供实体操作API
 * 使用开放类和明确的方法签名以确保Nashorn可以正确调用
 */
open class ScriptEntity(val bukkitEntity: LivingEntity) {

    // ===== 伤害操作 (AttributePlus Style) =====
    @JvmName("getDamage")
    fun getDamage(): Double = bukkitEntity.getDamage()
    
    @JvmName("setDamage")
    fun setDamage(v: Any) = bukkitEntity.setDamage(Coerce.toDouble(v))
    
    @JvmName("addDamage")
    fun addDamage(v: Any) = bukkitEntity.addDamage(Coerce.toDouble(v))
    
    @JvmName("takeDamage")
    fun takeDamage(v: Any) = bukkitEntity.takeDamage(Coerce.toDouble(v))
    
    @JvmName("addBucketDamage")
    fun addBucketDamage(type: String, v: Any) = bukkitEntity.addBucketDamage(type, Coerce.toDouble(v))
    
    @JvmName("addElementalDamage")
    fun addElementalDamage(type: String, v: Any) = bukkitEntity.addElementalDamage(type, Coerce.toDouble(v))
    
    @JvmName("setMultiplier")
    fun setMultiplier(v: Any) = bukkitEntity.setDamageMultiplier(Coerce.toDouble(v))

    // ===== 暴击系统 =====
    @JvmName("rollCrit")
    fun rollCrit(v: Any) = bukkitEntity.rollCrit(Coerce.toDouble(v))
    
    @JvmName("setCritTier")
    fun setCritTier(v: Any) = bukkitEntity.setCritTier(Coerce.toInteger(v))
    
    @JvmName("addCritDamage")
    fun addCritDamage(v: Any) = bukkitEntity.addCritDamage(Coerce.toDouble(v))
    
    @JvmName("addCritResistance")
    fun addCritResistance(v: Any) = bukkitEntity.addCritResistance(Coerce.toDouble(v))
    
    @JvmName("addCritResilience")
    fun addCritResilience(v: Any) = bukkitEntity.addCritResilience(Coerce.toDouble(v))

    // ===== 防御系统 =====
    @JvmName("addDefenseScore")
    fun addDefenseScore(v: Any) = bukkitEntity.addDefenseScore(Coerce.toDouble(v))
    
    @JvmName("addPhysicalDefense")
    fun addPhysicalDefense(v: Any) = bukkitEntity.addPhysicalDefense(Coerce.toDouble(v))
    
    @JvmName("addMagicalDefense")
    fun addMagicalDefense(v: Any) = bukkitEntity.addMagicalDefense(Coerce.toDouble(v))
    
    @JvmName("addFixedPenetration")
    fun addFixedPenetration(v: Any) = bukkitEntity.addFixedPenetration(Coerce.toDouble(v))
    
    @JvmName("addPercentPenetration")
    fun addPercentPenetration(v: Any) = bukkitEntity.addPercentPenetration(Coerce.toDouble(v))
    
    fun addFixedPen(v: Any) = addFixedPenetration(v)
    fun addPercentPen(v: Any) = addPercentPenetration(v)

    // ===== 伤害减免 =====
    @JvmName("addUniversalReduction")
    fun addUniversalReduction(v: Any) = bukkitEntity.addUniversalReduction(Coerce.toDouble(v))
    
    @JvmName("addUniversalFlatReduction")
    fun addUniversalFlatReduction(v: Any) = bukkitEntity.addUniversalFlatReduction(Coerce.toDouble(v))
    
    @JvmName("addBucketResistance")
    fun addBucketResistance(type: String, v: Any) = bukkitEntity.addBucketResistance(type, Coerce.toDouble(v))

    // ===== 标签系统 =====
    @JvmName("addTag")
    fun addTag(tag: String) = bukkitEntity.addTag(tag)
    
    @JvmName("hasTag")
    fun hasTag(tag: String) = bukkitEntity.hasTag(tag)
    
    @JvmName("removeTag")
    fun removeTag(tag: String) = bukkitEntity.removeTag(tag)

    // ===== 属性读取 =====
    @JvmName("getRandomValue")
    fun getRandomValue(key: String) = bukkitEntity.getRandomValue(key)
    
    @JvmName("getAttrMin")
    fun getAttrMin(key: String) = bukkitEntity.getAttrMin(key)
    
    @JvmName("getAttrMax")
    fun getAttrMax(key: String) = bukkitEntity.getAttrMax(key)
    
    @JvmName("getAttributeValue")
    fun getAttributeValue(key: String) = bukkitEntity.getAttributeValue(key)
    
    @JvmName("getCP")
    fun getCP() = bukkitEntity.getCP()

    // ===== 护盾与生命 =====
    @JvmName("addShield")
    fun addShield(v: Any) = bukkitEntity.addShield(Coerce.toDouble(v))
    
    @JvmName("getShield")
    fun getShield() = bukkitEntity.getShield()
    
    @JvmName("heal")
    fun heal(v: Any) = bukkitEntity.healSelf(Coerce.toDouble(v))

    // ===== 视觉与交互 =====
    @JvmName("tell")
    fun tell(msg: String) = JavaScriptAPI.tell(bukkitEntity, msg)
    
    @JvmName("actionbar")
    fun actionbar(msg: String) = JavaScriptAPI.actionbar(bukkitEntity, msg)
    
    @JvmName("sendActionBar")
    fun sendActionBar(msg: String) = JavaScriptAPI.actionbar(bukkitEntity, msg)
    
    @JvmName("sound")
    fun sound(name: String) = JavaScriptAPI.sound(bukkitEntity, name)

    // ===== 原生属性 =====
    @JvmName("getName")
    fun getName() = bukkitEntity.name
    
    @JvmName("getHealth")
    fun getHealth() = bukkitEntity.health
    
    @JvmName("getMaxHealth")
    fun getMaxHealth() = bukkitEntity.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
    
    @JvmName("getBukkit")
    fun getBukkit() = bukkitEntity
}