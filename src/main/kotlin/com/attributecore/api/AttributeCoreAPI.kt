package com.attributecore.api

import com.attributecore.data.AttributeData
import com.attributecore.data.DamageBucket
import com.attributecore.data.Element
import com.attributecore.data.SubAttribute
import com.attributecore.manager.AttributeManager
import com.attributecore.manager.ItemAttributeReader
import com.attributecore.script.JsAttribute
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

/**
 * AttributeCore 主 API 入口
 * 
 * 提供实体属性读写、物品属性解析、伤害计算等核心功能
 * 支持多插件来源的属性叠加
 */
object AttributeCoreAPI {
    private val pluginAttributes = ConcurrentHashMap<UUID, MutableMap<String, AttributeData>>()

    // ==================== 实体属性查询 ====================

    /**
     * 获取实体的完整属性数据（基础 + API 来源）
     */
    @JvmStatic
    fun getEntityData(entity: LivingEntity): AttributeData {
        val baseData = AttributeManager.getEntityData(entity)
        val apiData = getPluginAttributeData(entity.uniqueId)
        return if (apiData.isValid()) {
            AttributeData().apply {
                add(baseData)
                add(apiData)
            }
        } else {
            baseData
        }
    }

    /**
     * 获取实体的基础属性（不含 API 来源）
     */
    @JvmStatic
    fun getBaseEntityData(entity: LivingEntity): AttributeData {
        return AttributeManager.getEntityData(entity)
    }

    /**
     * 获取实体某个属性的固定值
     */
    @JvmStatic
    fun getAttribute(entity: LivingEntity, key: String): Double {
        return getEntityData(entity)[key]
    }

    /**
     * 获取实体某个属性的百分比加成
     */
    @JvmStatic
    fun getAttributePercent(entity: LivingEntity, key: String): Double {
        return getEntityData(entity).getPercent(key)
    }

    /**
     * 获取实体某个属性的最终值（固定值 * (1 + 百分比/100)）
     */
    @JvmStatic
    fun getAttributeFinal(entity: LivingEntity, key: String): Double {
        return getEntityData(entity).getFinal(key)
    }

    /**
     * 批量获取实体多个属性的最终值
     */
    @JvmStatic
    fun getAttributesBatch(entity: LivingEntity, keys: List<String>): Map<String, Double> {
        val data = getEntityData(entity)
        return keys.associateWith { data.getFinal(it) }
    }

    /**
     * 获取实体所有非零属性
     */
    @JvmStatic
    fun getAllNonZeroAttributes(entity: LivingEntity): Map<String, Double> {
        return getEntityData(entity).getNonZeroAttributes()
    }

    /**
     * 获取实体所有属性的最终值
     */
    @JvmStatic
    fun getAllAttributesFinal(entity: LivingEntity): Map<String, Double> {
        return getEntityData(entity).getAllFinal()
    }

    // ==================== 插件属性管理 ====================

    /**
     * 获取某实体所有 API 来源的属性数据
     */
    @JvmStatic
    fun getPluginAttributeData(uuid: UUID): AttributeData {
        val result = AttributeData()
        pluginAttributes[uuid]?.values?.forEach { result.add(it) }
        return result
    }

    /**
     * 设置插件为实体提供的属性数据
     */
    @JvmStatic
    fun setPluginAttribute(pluginId: String, uuid: UUID, data: AttributeData) {
        pluginAttributes.computeIfAbsent(uuid) { mutableMapOf() }[pluginId] = data
    }

    /**
     * 设置插件为实体提供的单个属性值
     */
    @JvmStatic
    fun setPluginAttribute(pluginId: String, uuid: UUID, key: String, value: Double) {
        val data = pluginAttributes.computeIfAbsent(uuid) { mutableMapOf() }
            .getOrPut(pluginId) { AttributeData() }
        data[key] = value
    }

    /**
     * 设置插件为实体提供的单个属性百分比值
     */
    @JvmStatic
    fun setPluginAttributePercent(pluginId: String, uuid: UUID, key: String, value: Double) {
        val data = pluginAttributes.computeIfAbsent(uuid) { mutableMapOf() }
            .getOrPut(pluginId) { AttributeData() }
        data.setPercent(key, value)
    }

    /**
     * 添加插件属性值（叠加）
     */
    @JvmStatic
    fun addPluginAttribute(pluginId: String, uuid: UUID, key: String, value: Double) {
        val data = pluginAttributes.computeIfAbsent(uuid) { mutableMapOf() }
            .getOrPut(pluginId) { AttributeData() }
        data.add(key, value)
    }

    /**
     * 添加插件属性百分比值（叠加）
     */
    @JvmStatic
    fun addPluginAttributePercent(pluginId: String, uuid: UUID, key: String, value: Double) {
        val data = pluginAttributes.computeIfAbsent(uuid) { mutableMapOf() }
            .getOrPut(pluginId) { AttributeData() }
        data.addPercent(key, value)
    }

    /**
     * 移除插件为实体提供的属性数据
     */
    @JvmStatic
    fun removePluginAttribute(pluginId: String, uuid: UUID): AttributeData? {
        return pluginAttributes[uuid]?.remove(pluginId)
    }

    /**
     * 检查插件是否为实体设置了属性
     */
    @JvmStatic
    fun hasPluginAttribute(pluginId: String, uuid: UUID): Boolean {
        return pluginAttributes[uuid]?.containsKey(pluginId) == true
    }

    /**
     * 清空实体的所有 API 来源属性
     */
    @JvmStatic
    fun clearEntityPluginData(uuid: UUID) {
        pluginAttributes.remove(uuid)
    }

    /**
     * 清空插件为所有实体设置的属性
     */
    @JvmStatic
    fun clearPluginAllData(pluginId: String) {
        pluginAttributes.values.forEach { it.remove(pluginId) }
    }

    // ==================== 实体属性刷新 ====================

    /**
     * 刷新实体的属性缓存
     */
    @JvmStatic
    fun updateEntityData(entity: LivingEntity) {
        AttributeManager.loadEntityData(entity)
    }

    /**
     * 清除实体的属性缓存
     */
    @JvmStatic
    fun clearEntityCache(entity: LivingEntity) {
        AttributeManager.clearEntityData(entity.uniqueId)
    }

    /**
     * 清除实体的属性缓存（通过 UUID）
     */
    @JvmStatic
    fun clearEntityCache(uuid: UUID) {
        AttributeManager.clearEntityData(uuid)
    }

    // ==================== 物品属性解析 ====================

    /**
     * 读取物品的属性数据
     */
    @JvmStatic
    fun loadItemData(item: ItemStack?): AttributeData {
        return ItemAttributeReader.readItem(item)
    }

    /**
     * 批量读取多个物品的属性数据（合并）
     */
    @JvmStatic
    fun loadItemData(items: List<ItemStack?>): AttributeData {
        val result = AttributeData()
        items.filterNotNull().forEach { result.add(ItemAttributeReader.readItem(it)) }
        return result
    }

    /**
     * 从 Lore 解析属性
     */
    @JvmStatic
    fun parseAttributesFromLore(lore: String): AttributeData {
        val data = AttributeData()
        ItemAttributeReader.parseAttributesFromLore(data, lore)
        return data
    }

    /**
     * 从 Lore 列表解析属性
     */
    @JvmStatic
    fun parseAttributesFromLore(lores: List<String>): AttributeData {
        val data = AttributeData()
        lores.forEach { ItemAttributeReader.parseAttributesFromLore(data, it) }
        return data
    }

    // ==================== 伤害系统 ====================

    /**
     * 构建实体的伤害桶（基于攻击类属性）
     */
    @JvmStatic
    fun buildDamageBucket(entity: LivingEntity): DamageBucket {
        return getEntityData(entity).buildDamageBucket()
    }

    /**
     * 获取实体的所有元素抗性
     */
    @JvmStatic
    fun getResistances(entity: LivingEntity): Map<Element, Double> {
        return getEntityData(entity).getAllResistances()
    }

    /**
     * 获取实体某个元素的抗性
     */
    @JvmStatic
    fun getResistance(entity: LivingEntity, element: Element): Double {
        return getEntityData(entity).getResistance(element)
    }

    /**
     * 获取实体某个元素的抗性（通过元素名称）
     */
    @JvmStatic
    fun getResistance(entity: LivingEntity, elementName: String): Double {
        val element = Element.fromConfigKey(elementName) ?: return 0.0
        return getEntityData(entity).getResistance(element)
    }

    /**
     * 计算从攻击者到受害者的最终伤害
     */
    @JvmStatic
    fun calculateFinalDamage(
        attacker: LivingEntity,
        victim: LivingEntity,
        baseDamage: Double
    ): Double {
        val bucket = buildDamageBucket(attacker)
        if (bucket.total() <= 0) {
            bucket[Element.PHYSICAL] = baseDamage
        }
        bucket.applyResistances(getResistances(victim))
        return bucket.total()
    }

    /**
     * 计算伤害桶应用抗性后的最终伤害
     */
    @JvmStatic
    fun calculateFinalDamage(bucket: DamageBucket, victim: LivingEntity): Double {
        val cloned = bucket.clone()
        cloned.applyResistances(getResistances(victim))
        return cloned.total()
    }

    // ==================== 战斗力计算 ====================

    /**
     * 获取实体的战斗力
     */
    @JvmStatic
    fun getCombatPower(entity: LivingEntity): Double {
        val data = getEntityData(entity)
        val weights = SubAttribute.getAttributes().associate { it.name to it.combatPowerWeight }
        return data.calculateCombatPower(weights)
    }

    // ==================== 回调注册 ====================

    /**
     * 触发属性变更回调
     */
    @JvmStatic
    fun onAttributeChange(entity: LivingEntity, callback: Consumer<AttributeData>) {
        val data = getEntityData(entity)
        callback.accept(data)
    }

    // ==================== 属性列表查询 ====================

    /**
     * 获取所有已注册属性的名称列表
     */
    @JvmStatic
    fun getRegisteredAttributeNames(): List<String> {
        return SubAttribute.getAttributes().map { it.name }
    }

    /**
     * 获取所有 JS 属性的名称列表
     */
    @JvmStatic
    fun getJsAttributeNames(): List<String> {
        return SubAttribute.getAttributes()
            .filterIsInstance<JsAttribute>()
            .map { it.name }
    }

    /**
     * 检查属性是否已注册
     */
    @JvmStatic
    fun isAttributeRegistered(name: String): Boolean {
        return SubAttribute.getByName(name) != null
    }

    /**
     * 获取属性的 NBT 名称（中文 pattern）
     */
    @JvmStatic
    fun getAttributeNbtName(name: String): String? {
        return SubAttribute.getByName(name)?.nbtName
    }

    /**
     * 获取属性的战斗力权重
     */
    @JvmStatic
    fun getAttributeCombatPowerWeight(name: String): Double {
        return SubAttribute.getByName(name)?.combatPowerWeight ?: 1.0
    }
}
