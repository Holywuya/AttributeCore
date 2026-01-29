package com.attributecore.util

import com.attributecore.manager.AttributeManager
import com.attributecore.manager.ShieldManager
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import java.util.concurrent.ThreadLocalRandom

/**
 * AttributePlus 风格 API
 * 使得在 JS 脚本中可以直接通过 attacker.addDamage() 操作
 */

// ==========================================
//          属性读取相关 (Getters)
// ==========================================

fun LivingEntity.getRandomValue(key: String): Double {
    val vals = AttributeManager.getData(this).get(key)
    if (vals[0] == vals[1]) return vals[0]
    return ThreadLocalRandom.current().nextDouble(vals[0], vals[1])
}

fun LivingEntity.getAttrMin(key: String): Double {
    return AttributeManager.getData(this).get(key)[0]
}

fun LivingEntity.getAttrMax(key: String): Double {
    return AttributeManager.getData(this).get(key)[1]
}

fun LivingEntity.getAttributeValue(key: String): DoubleArray {
    return AttributeManager.getData(this).get(key)
}

fun LivingEntity.getCP(): Double {
    return AttributeManager.getCombatPower(this)
}

// ==========================================
//          伤害操作 (AttributePlus Style)
// ==========================================

fun LivingEntity.getDamage(): Double {
    val activeData = DamageContext.getActiveData() ?: return 0.0
    return activeData.getFinalDamage()
}

fun LivingEntity.setDamage(value: Double) {
    val activeData = DamageContext.getActiveData() ?: return
    activeData.addDamage(value - activeData.originalDamage)
}

fun LivingEntity.addDamage(value: Double) {
    val activeData = DamageContext.getActiveData()
    taboolib.common.platform.function.console().sendMessage("§e[AC-DEBUG] §faddDamage调用: entity=${this.name}, value=$value, activeData=$activeData")
    if (activeData != null) {
        taboolib.common.platform.function.console().sendMessage("§e[AC-DEBUG] §f  attacker=${activeData.attacker.name}, this=${this.name}, match=${activeData.attacker == this}")
        if (activeData.attacker == this) {
            activeData.addDamage(value)
        } else {
            taboolib.common.platform.function.console().sendMessage("§c[AC-DEBUG] §f  攻击者不匹配，跳过")
        }
    } else {
        taboolib.common.platform.function.console().sendMessage("§c[AC-DEBUG] §f  activeData为null")
    }
}

fun LivingEntity.takeDamage(value: Double) {
    val activeData = DamageContext.getActiveData()
    if (activeData != null && activeData.attacker == this) {
        activeData.addDamage(-value)
    }
}

fun LivingEntity.addBucketDamage(type: String, value: Double) {
    val activeData = DamageContext.getActiveData()
    if (activeData != null && activeData.attacker == this) {
        activeData.addBucketDamage(type, value)
    }
}

fun LivingEntity.addElementalDamage(type: String, value: Double) {
    val activeData = DamageContext.getActiveData()
    if (activeData != null && activeData.attacker == this) {
        activeData.addElementalDamage(type, value)
    }
}

fun LivingEntity.setDamageMultiplier(value: Double) {
    val activeData = DamageContext.getActiveData()
    if (activeData != null && activeData.attacker == this) {
        activeData.setDamageMultiplier(value)
    }
}

// ==========================================
//          暴击系统 (Crit System)
// ==========================================

fun LivingEntity.setCritTier(tier: Int) {
    val activeData = DamageContext.getActiveData()
    if (activeData != null && activeData.attacker == this) {
        activeData.critTier = tier
    }
}

fun LivingEntity.addCritDamage(value: Double) {
    val activeData = DamageContext.getActiveData()
    if (activeData != null && activeData.attacker == this) {
        activeData.addCritDamage(value)
    }
}

fun LivingEntity.rollCrit(chance: Double) {
    val activeData = DamageContext.getActiveData()
    if (activeData != null && activeData.attacker == this) {
        activeData.rollCrit(chance)
    }
}

fun LivingEntity.addCritResistance(value: Double) {
    val activeData = DamageContext.getActiveData()
    if (activeData != null && (activeData.defender == this || activeData.attacker == this)) {
        activeData.addCritResistance(value)
    }
}

fun LivingEntity.addCritResilience(value: Double) {
    val activeData = DamageContext.getActiveData()
    if (activeData != null && (activeData.defender == this || activeData.attacker == this)) {
        activeData.addCritResilience(value)
    }
}

// ==========================================
//          防御系统 (Defense System)
// ==========================================

fun LivingEntity.addDefenseScore(value: Double) {
    val activeData = DamageContext.getActiveData()
    if (activeData != null && activeData.defender == this) {
        activeData.addDefenseScore(value)
    }
}

fun LivingEntity.addPhysicalDefense(value: Double) {
    val activeData = DamageContext.getActiveData()
    if (activeData != null && activeData.defender == this) {
        activeData.addPhysicalDefense(value)
    }
}

fun LivingEntity.addMagicalDefense(value: Double) {
    val activeData = DamageContext.getActiveData()
    if (activeData != null && activeData.defender == this) {
        activeData.addMagicalDefense(value)
    }
}

fun LivingEntity.addFixedPenetration(value: Double) {
    val activeData = DamageContext.getActiveData()
    if (activeData != null && activeData.attacker == this) {
        activeData.addFixedPenetration(value)
    }
}

fun LivingEntity.addPercentPenetration(value: Double) {
    val activeData = DamageContext.getActiveData()
    if (activeData != null && activeData.attacker == this) {
        activeData.addPercentPenetration(value)
    }
}

// ==========================================
//          伤害减免 (Damage Reduction)
// ==========================================

fun LivingEntity.addUniversalReduction(percent: Double) {
    val activeData = DamageContext.getActiveData()
    if (activeData != null && activeData.defender == this) {
        activeData.addUniversalReduction(percent)
    }
}

fun LivingEntity.addUniversalFlatReduction(amount: Double) {
    val activeData = DamageContext.getActiveData()
    if (activeData != null && activeData.defender == this) {
        activeData.addUniversalFlatReduction(amount)
    }
}

fun LivingEntity.addBucketResistance(type: String, percent: Double) {
    val activeData = DamageContext.getActiveData()
    if (activeData != null && activeData.defender == this) {
        activeData.addBucketResistance(type, percent)
    }
}

// ==========================================
//          标签系统 (Tag System)
// ==========================================

fun LivingEntity.addTag(tag: String) {
    val activeData = DamageContext.getActiveData() ?: return
    activeData.addTag(tag)
}

fun LivingEntity.hasTag(tag: String): Boolean {
    val activeData = DamageContext.getActiveData() ?: return false
    return activeData.hasTag(tag)
}

fun LivingEntity.removeTag(tag: String) {
    val activeData = DamageContext.getActiveData() ?: return
    activeData.removeTag(tag)
}

// ==========================================
//          护盾与生命 (Shield & Health)
// ==========================================

fun LivingEntity.addShield(value: Double) {
    ShieldManager.modifyShield(this.uniqueId, value)
}

fun LivingEntity.getShield(): Double {
    return ShieldManager.getCurrentShield(this.uniqueId)
}

fun LivingEntity.healSelf(amount: Double) {
    val maxHp = this.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
    val currentHp = this.health
    if (currentHp < maxHp) {
        this.health = (currentHp + amount).coerceAtMost(maxHp)
    }
}

// ==========================================
//          几率判定 (Chance)
// ==========================================

fun Double.chance(): Boolean {
    return ThreadLocalRandom.current().nextDouble(100.0) < this
}
