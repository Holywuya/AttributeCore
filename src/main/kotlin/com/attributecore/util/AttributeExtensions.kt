package com.attributecore.util

import com.attributecore.manager.AttributeManager
import com.attributecore.manager.ShieldManager
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import java.util.concurrent.ThreadLocalRandom

/**
 * 为 LivingEntity 扩展的“拟人化”方法集
 * 使得在 Kotlin 或 JS 脚本中可以直接通过实体对象操作插件逻辑
 */

// ==========================================
//          属性读取相关 (Getters)
// ==========================================

/**
 * 获取实体的随机属性值 (Roll点)
 * 用法: entity.getRandomValue("physical_damage")
 */
fun LivingEntity.getRandomValue(key: String): Double {
    val vals = AttributeManager.getData(this).get(key)
    if (vals[0] == vals[1]) return vals[0]
    return ThreadLocalRandom.current().nextDouble(vals[0], vals[1])
}

/**
 * 获取属性最小值 (基础值)
 */
fun LivingEntity.getAttrMin(key: String): Double {
    return AttributeManager.getData(this).get(key)[0]
}

/**
 * 获取属性最大值
 */
fun LivingEntity.getAttrMax(key: String): Double {
    return AttributeManager.getData(this).get(key)[1]
}

/**
 * 获取当前战斗力
 */
fun LivingEntity.getCP(): Double {
    return AttributeManager.getCombatPower(this)
}

// ==========================================
//          伤害修改相关 (Damage Data)
// ==========================================

/**
 * 直接给当前攻击增加伤害数值 (作用于 PHYSICAL 桶)
 * 用法: attacker.addDamage(50.0)
 */
fun LivingEntity.addDamage(value: Double) {
    val activeData = DamageContext.getActiveData()
    // 安全检查：只有当前活跃的伤害发起者是自己时，操作才生效
    if (activeData != null && activeData.attacker == this) {
        activeData.addDamage(value)
    }
}

/**
 * 增加特定类型的伤害桶数值 (如 FIRE, WATER, MAGIC)
 * 用法: attacker.addBucketDamage("FIRE", 20.0)
 */
fun LivingEntity.addBucketDamage(type: String, value: Double) {
    val activeData = DamageContext.getActiveData()
    if (activeData != null && activeData.attacker == this) {
        activeData.addBucketDamage(type, value)
    }
}

/**
 * 设置本次攻击的全局伤害倍率
 * 用法: attacker.setDamageMultiplier(1.5)
 */
fun LivingEntity.setDamageMultiplier(value: Double) {
    val activeData = DamageContext.getActiveData()
    if (activeData != null && activeData.attacker == this) {
        activeData.setDamageMultiplier(value)
    }
}

/**
 * 设置本次攻击的暴击层级 (Warframe 风格)
 * 1=黄暴, 2=橙暴, 3=红暴...
 */
fun LivingEntity.setCritTier(tier: Int) {
    val activeData = DamageContext.getActiveData()
    if (activeData != null && activeData.attacker == this) {
        activeData.critTier = tier
    }
}

/**
 * 给本次攻击增加额外的暴击伤害百分比
 */
fun LivingEntity.addCritDamage(value: Double) {
    val activeData = DamageContext.getActiveData()
    if (activeData != null && activeData.attacker == this) {
        activeData.addCritDamage(value)
    }
}

// ==========================================
//          状态与护盾相关 (Status & Shield)
// ==========================================

/**
 * 修改实体的护盾值
 * 用法: entity.addShield(100.0) / entity.addShield(-50.0)
 */
fun LivingEntity.addShield(value: Double) {
    ShieldManager.modifyShield(this.uniqueId, value)
}

/**
 * 获取实体当前的护盾值
 */
fun LivingEntity.getShield(): Double {
    return ShieldManager.getCurrentShield(this.uniqueId)
}

/**
 * 治疗实体 (不会超过最大生命值)
 * 用法: entity.healSelf(10.0)
 */
fun LivingEntity.healSelf(amount: Double) {
    val maxHp = this.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
    val currentHp = this.health
    if (currentHp < maxHp) {
        this.health = (currentHp + amount).coerceAtMost(maxHp)
    }
}