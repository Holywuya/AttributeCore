package com.attributecore.api

import com.attributecore.data.AttributeData
import com.attributecore.data.DamageBucket
import com.attributecore.data.Elements
import com.attributecore.data.SubAttribute
import com.attributecore.manager.AttributeManager
import com.attributecore.manager.ItemAttributeReader
import com.attributecore.script.JsAttribute
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

object AttributeCoreAPI {
    private val pluginAttributes = ConcurrentHashMap<UUID, MutableMap<String, AttributeData>>()

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

    @JvmStatic
    fun getBaseEntityData(entity: LivingEntity): AttributeData {
        return AttributeManager.getEntityData(entity)
    }

    @JvmStatic
    fun getAttribute(entity: LivingEntity, key: String): Double {
        return getEntityData(entity)[key]
    }

    @JvmStatic
    fun getAttributePercent(entity: LivingEntity, key: String): Double {
        return getEntityData(entity).getPercent(key)
    }

    @JvmStatic
    fun getAttributeFinal(entity: LivingEntity, key: String): Double {
        return getEntityData(entity).getFinal(key)
    }

    @JvmStatic
    fun getAttributesBatch(entity: LivingEntity, keys: List<String>): Map<String, Double> {
        val data = getEntityData(entity)
        return keys.associateWith { data.getFinal(it) }
    }

    @JvmStatic
    fun getAllNonZeroAttributes(entity: LivingEntity): Map<String, Double> {
        return getEntityData(entity).getNonZeroAttributes()
    }

    @JvmStatic
    fun getAllAttributesFinal(entity: LivingEntity): Map<String, Double> {
        return getEntityData(entity).getAllFinal()
    }

    @JvmStatic
    fun getPluginAttributeData(uuid: UUID): AttributeData {
        val result = AttributeData()
        pluginAttributes[uuid]?.values?.forEach { result.add(it) }
        return result
    }

    @JvmStatic
    fun setPluginAttribute(pluginId: String, uuid: UUID, data: AttributeData) {
        pluginAttributes.computeIfAbsent(uuid) { mutableMapOf() }[pluginId] = data
    }

    @JvmStatic
    fun setPluginAttribute(pluginId: String, uuid: UUID, key: String, value: Double) {
        val data = pluginAttributes.computeIfAbsent(uuid) { mutableMapOf() }
            .getOrPut(pluginId) { AttributeData() }
        data[key] = value
    }

    @JvmStatic
    fun setPluginAttributePercent(pluginId: String, uuid: UUID, key: String, value: Double) {
        val data = pluginAttributes.computeIfAbsent(uuid) { mutableMapOf() }
            .getOrPut(pluginId) { AttributeData() }
        data.setPercent(key, value)
    }

    @JvmStatic
    fun addPluginAttribute(pluginId: String, uuid: UUID, key: String, value: Double) {
        val data = pluginAttributes.computeIfAbsent(uuid) { mutableMapOf() }
            .getOrPut(pluginId) { AttributeData() }
        data.add(key, value)
    }

    @JvmStatic
    fun addPluginAttributePercent(pluginId: String, uuid: UUID, key: String, value: Double) {
        val data = pluginAttributes.computeIfAbsent(uuid) { mutableMapOf() }
            .getOrPut(pluginId) { AttributeData() }
        data.addPercent(key, value)
    }

    @JvmStatic
    fun removePluginAttribute(pluginId: String, uuid: UUID): AttributeData? {
        return pluginAttributes[uuid]?.remove(pluginId)
    }

    @JvmStatic
    fun hasPluginAttribute(pluginId: String, uuid: UUID): Boolean {
        return pluginAttributes[uuid]?.containsKey(pluginId) == true
    }

    @JvmStatic
    fun clearEntityPluginData(uuid: UUID) {
        pluginAttributes.remove(uuid)
    }

    @JvmStatic
    fun clearPluginAllData(pluginId: String) {
        pluginAttributes.values.forEach { it.remove(pluginId) }
    }

    @JvmStatic
    fun updateEntityData(entity: LivingEntity) {
        AttributeManager.loadEntityData(entity)
    }

    @JvmStatic
    fun clearEntityCache(entity: LivingEntity) {
        AttributeManager.clearEntityData(entity.uniqueId)
    }

    @JvmStatic
    fun clearEntityCache(uuid: UUID) {
        AttributeManager.clearEntityData(uuid)
    }

    @JvmStatic
    fun loadItemData(item: ItemStack?): AttributeData {
        return ItemAttributeReader.readItem(item)
    }

    @JvmStatic
    fun loadItemData(items: List<ItemStack?>): AttributeData {
        val result = AttributeData()
        items.filterNotNull().forEach { result.add(ItemAttributeReader.readItem(it)) }
        return result
    }

    @JvmStatic
    fun parseAttributesFromLore(lore: String): AttributeData {
        val data = AttributeData()
        ItemAttributeReader.parseAttributesFromLore(data, lore)
        return data
    }

    @JvmStatic
    fun parseAttributesFromLore(lores: List<String>): AttributeData {
        val data = AttributeData()
        lores.forEach { ItemAttributeReader.parseAttributesFromLore(data, it) }
        return data
    }

    @JvmStatic
    fun buildDamageBucket(entity: LivingEntity): DamageBucket {
        return getEntityData(entity).buildDamageBucket()
    }

    @JvmStatic
    fun getResistances(entity: LivingEntity): Map<String, Double> {
        return getEntityData(entity).getAllResistances()
    }

    @JvmStatic
    fun getResistance(entity: LivingEntity, element: String): Double {
        return getEntityData(entity).getResistance(element)
    }

    @JvmStatic
    fun calculateFinalDamage(
        attacker: LivingEntity,
        victim: LivingEntity,
        baseDamage: Double
    ): Double {
        val bucket = buildDamageBucket(attacker)
        if (bucket.total() <= 0) {
            bucket[Elements.PHYSICAL] = baseDamage
        }
        bucket.applyResistances(getResistances(victim))
        return bucket.total()
    }

    @JvmStatic
    fun calculateFinalDamage(bucket: DamageBucket, victim: LivingEntity): Double {
        val cloned = bucket.clone()
        cloned.applyResistances(getResistances(victim))
        return cloned.total()
    }

    @JvmStatic
    fun getCombatPower(entity: LivingEntity): Double {
        val data = getEntityData(entity)
        val weights = SubAttribute.getAttributes().associate { it.name to it.combatPowerWeight }
        return data.calculateCombatPower(weights)
    }

    @JvmStatic
    fun onAttributeChange(entity: LivingEntity, callback: Consumer<AttributeData>) {
        val data = getEntityData(entity)
        callback.accept(data)
    }

    @JvmStatic
    fun getRegisteredAttributeNames(): List<String> {
        return SubAttribute.getAttributes().map { it.name }
    }

    @JvmStatic
    fun getJsAttributeNames(): List<String> {
        return SubAttribute.getAttributes()
            .filterIsInstance<JsAttribute>()
            .map { it.name }
    }

    @JvmStatic
    fun isAttributeRegistered(name: String): Boolean {
        return SubAttribute.getByName(name) != null
    }

    @JvmStatic
    fun getAttributeNbtName(name: String): String? {
        return SubAttribute.getByName(name)?.nbtName
    }

    @JvmStatic
    fun getAttributeCombatPowerWeight(name: String): Double {
        return SubAttribute.getByName(name)?.combatPowerWeight ?: 1.0
    }
    
    @JvmStatic
    fun getAttributeByPlaceholder(placeholder: String): SubAttribute? {
        return SubAttribute.getAttributes().find { it.placeholder.equals(placeholder, ignoreCase = true) }
    }
    
    @JvmStatic
    fun getAttributeNameByPlaceholder(placeholder: String): String? {
        return getAttributeByPlaceholder(placeholder)?.name
    }
    
    @JvmStatic
    fun getRegisteredPlaceholders(): List<String> {
        return SubAttribute.getAttributes().map { it.placeholder }
    }
}
