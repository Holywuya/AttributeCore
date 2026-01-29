package com.attributecore.api

import com.attributecore.data.AttributeData
import com.attributecore.data.DamageData
import com.attributecore.manager.AttributeManager
import com.attributecore.manager.CombatPowerCalculator
import com.attributecore.manager.ScriptManager
import com.attributecore.manager.ShieldManager
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageByEntityEvent
import java.util.UUID

/**
 * AttributeCore 公共 API
 * 供外部插件调用，涵盖属性管理、伤害系统、护盾系统、战斗力计算等
 */
object AttributeCoreAPI {

    // ========================================
    // 基础属性管理 API
    // ========================================

    /**
     * 获取实体的当前最终属性数据 (包含装备和API加成)
     */
    @JvmStatic
    fun getEntityData(entity: LivingEntity): AttributeData {
        return AttributeManager.getData(entity)
    }

    /**
     * 获取指定插件/来源赋予实体的属性数据
     * @param source 来源标识 (建议使用插件主类或插件名)
     * @param uuid 实体的 UUID
     */
    @JvmStatic
    fun getEntityAPIData(source: Any, uuid: UUID): Map<String, Double>? {
        return AttributeManager.getApiData(uuid, getSourceName(source))
    }

    /**
     * 设置指定来源的实体属性数据
     * @param source 来源标识
     * @param uuid 实体的 UUID
     * @param key 属性ID
     * @param value 属性值
     */
    @JvmStatic
    fun setEntityAPIData(source: Any, uuid: UUID, key: String, value: Double) {
        AttributeManager.setApiAttribute(uuid, getSourceName(source), key, value)
    }

    /**
     * 移除指定来源的实体属性数据
     * @param source 来源标识
     * @param uuid 实体的 UUID
     */
    @JvmStatic
    fun removeEntityAPIData(source: Any, uuid: UUID) {
        AttributeManager.removeApiData(uuid, getSourceName(source))
    }

    /**
     * 移除指定插件/来源的所有数据
     * 通常在插件卸载 (onDisable) 时调用
     * @param source 来源标识
     */
    @JvmStatic
    fun removePluginAllEntityData(source: Any) {
        AttributeManager.clearSourceData(getSourceName(source))
    }

    /**
     * 强制刷新实体属性
     */
    @JvmStatic
    fun updateEntity(entity: LivingEntity) {
        AttributeManager.update(entity)
    }

    // ========================================
    // 属性查询 API (批量/统计)
    // ========================================

    /**
     * 获取实体所有非零属性
     * @return Map<属性键, DoubleArray[基础值, 加成值]>
     */
    @JvmStatic
    fun getAllNonZeroAttributes(entity: LivingEntity): Map<String, DoubleArray> {
        val data = AttributeManager.getData(entity)
        return AttributeManager.getAttributes()
            .associate { attr -> attr.key to data.get(attr.key) }
            .filterValues { it[0] != 0.0 || it[1] != 0.0 }
    }

    /**
     * 获取实体指定属性的基础值
     */
    @JvmStatic
    fun getAttributeBase(entity: LivingEntity, key: String): Double {
        return AttributeManager.getData(entity).get(key)[0]
    }

    /**
     * 获取实体指定属性的加成值
     */
    @JvmStatic
    fun getAttributeBonus(entity: LivingEntity, key: String): Double {
        return AttributeManager.getData(entity).get(key)[1]
    }

    /**
     * 获取实体指定属性的总值 (基础 + 加成)
     */
    @JvmStatic
    fun getAttributeTotal(entity: LivingEntity, key: String): Double {
        val vals = AttributeManager.getData(entity).get(key)
        return vals[0] + vals[1]
    }

    /**
     * 批量获取多个属性值
     * @param keys 属性键列表
     * @return Map<属性键, 总值>
     */
    @JvmStatic
    fun getAttributesBatch(entity: LivingEntity, keys: List<String>): Map<String, Double> {
        val data = AttributeManager.getData(entity)
        return keys.associateWith { key ->
            val vals = data.get(key)
            vals[0] + vals[1]
        }
    }

    // ========================================
    // 护盾系统 API
    // ========================================

    /**
     * 获取实体当前护盾值
     */
    @JvmStatic
    fun getCurrentShield(entity: LivingEntity): Double {
        return ShieldManager.getCurrentShield(entity.uniqueId)
    }

    /**
     * 获取实体最大护盾值
     */
    @JvmStatic
    fun getMaxShield(entity: LivingEntity): Double {
        return ShieldManager.getMaxShield(entity.uniqueId)
    }

    /**
     * 增加/减少实体护盾
     * @param delta 变化量 (正数加盾，负数扣盾)
     */
    @JvmStatic
    fun modifyShield(entity: LivingEntity, delta: Double) {
        ShieldManager.modifyShield(entity.uniqueId, delta)
    }

    /**
     * 设置实体当前护盾值
     */
    @JvmStatic
    fun setCurrentShield(entity: LivingEntity, amount: Double) {
        val max = ShieldManager.getMaxShield(entity.uniqueId)
        val current = ShieldManager.getCurrentShield(entity.uniqueId)
        ShieldManager.modifyShield(entity.uniqueId, amount - current)
    }

    /**
     * 获取护盾百分比 (0-100)
     */
    @JvmStatic
    fun getShieldPercent(entity: LivingEntity): Double {
        val max = ShieldManager.getMaxShield(entity.uniqueId)
        if (max <= 0) return 0.0
        val current = ShieldManager.getCurrentShield(entity.uniqueId)
        return (current / max * 100.0).coerceIn(0.0, 100.0)
    }

    // ========================================
    // 战斗力计算 API
    // ========================================

    /**
     * 计算实体当前战斗力
     */
    @JvmStatic
    fun getCombatPower(entity: LivingEntity): Double {
        return CombatPowerCalculator.calculate(entity, AttributeManager.getAttributes())
    }

    /**
     * 强制重新计算战斗力 (会触发属性刷新)
     */
    @JvmStatic
    fun recalculateCombatPower(entity: LivingEntity): Double {
        AttributeManager.update(entity)
        return getCombatPower(entity)
    }

    // ========================================
    // 伤害系统 API (高级)
    // ========================================

    /**
     * 创建自定义伤害上下文 (用于技能系统/自定义伤害)
     * @param attacker 攻击者
     * @param defender 防御者
     * @param event Bukkit 伤害事件
     * @return DamageData 上下文对象 (可继续链式操作)
     */
    @JvmStatic
    fun createDamageContext(
        attacker: LivingEntity,
        defender: LivingEntity,
        event: EntityDamageByEntityEvent
    ): DamageData {
        return DamageData(attacker, defender, event)
    }

    /**
     * 向伤害上下文添加指定类型的伤害
     * @param damageData 伤害上下文
     * @param damageType 伤害类型 (PHYSICAL, FIRE, WATER, DARK, LIGHT 等)
     * @param amount 伤害数值
     */
    @JvmStatic
    fun addTypedDamage(damageData: DamageData, damageType: String, amount: Double) {
        damageData.addBucketDamage(damageType, amount)
    }

    /**
     * 设置伤害倍率
     * @param damageData 伤害上下文
     * @param multiplier 倍率 (1.0 = 100%)
     */
    @JvmStatic
    fun setDamageMultiplier(damageData: DamageData, multiplier: Double) {
        damageData.setDamageMultiplier(multiplier)
    }

    /**
     * 添加伤害标签 (用于特殊机制判断)
     * @param damageData 伤害上下文
     * @param tag 标签名称 (如 "SKILL", "CRIT", "BURN")
     */
    @JvmStatic
    fun addDamageTag(damageData: DamageData, tag: String) {
        damageData.addTag(tag)
    }

    /**
     * 检查伤害是否包含指定标签
     */
    @JvmStatic
    fun hasDamageTag(damageData: DamageData, tag: String): Boolean {
        return damageData.hasTag(tag)
    }

    /**
     * 设置特殊效果标记
     * @param damageData 伤害上下文
     * @param cannotCrit 是否无法暴击
     * @param armorPiercing 是否穿甲
     */
    @JvmStatic
    fun setDamageFlags(damageData: DamageData, cannotCrit: Boolean = false, armorPiercing: Boolean = false) {
        damageData.cannotCrit = cannotCrit
        damageData.armorPiercing = armorPiercing
    }

    // ========================================
    // 辅助方法
    // ========================================

    /**
     * 将对象转为 Source 字符串
     */
    private fun getSourceName(source: Any): String {
        return when (source) {
            is String -> source
            is Class<*> -> source.simpleName
            else -> source.javaClass.simpleName
        }
    }
}