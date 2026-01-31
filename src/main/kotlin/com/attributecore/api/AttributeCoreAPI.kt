package com.attributecore.api

import com.attributecore.data.AttributeData
import com.attributecore.data.DamageBucket
import com.attributecore.data.Element
import com.attributecore.data.ElementalAura
import com.attributecore.manager.AttributeManager
import com.attributecore.manager.ItemAttributeReader
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

object AttributeCoreAPI {
    private val pluginAttributes = ConcurrentHashMap<UUID, MutableMap<String, AttributeData>>()

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

    fun getPluginAttributeData(uuid: UUID): AttributeData {
        val result = AttributeData()
        pluginAttributes[uuid]?.values?.forEach { result.add(it) }
        return result
    }

    fun setPluginAttribute(pluginId: String, uuid: UUID, data: AttributeData) {
        pluginAttributes.computeIfAbsent(uuid) { mutableMapOf() }[pluginId] = data
    }

    fun removePluginAttribute(pluginId: String, uuid: UUID): AttributeData? {
        return pluginAttributes[uuid]?.remove(pluginId)
    }

    fun hasPluginAttribute(pluginId: String, uuid: UUID): Boolean {
        return pluginAttributes[uuid]?.containsKey(pluginId) == true
    }

    fun clearEntityPluginData(uuid: UUID) {
        pluginAttributes.remove(uuid)
    }

    fun clearPluginAllData(pluginId: String) {
        pluginAttributes.values.forEach { it.remove(pluginId) }
    }

    fun loadItemData(item: ItemStack): AttributeData {
        return ItemAttributeReader.readItem(item)
    }

    fun loadItemData(items: List<ItemStack>): AttributeData {
        val result = AttributeData()
        items.forEach { result.add(ItemAttributeReader.readItem(it)) }
        return result
    }

    fun updateEntityData(entity: LivingEntity) {
        AttributeManager.loadEntityData(entity)
    }

    fun buildDamageBucket(entity: LivingEntity): DamageBucket {
        return getEntityData(entity).buildDamageBucket()
    }

    fun getResistances(entity: LivingEntity): Map<Element, Double> {
        return getEntityData(entity).getAllResistances()
    }

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

    fun onAttributeChange(entity: LivingEntity, callback: Consumer<AttributeData>) {
        val data = getEntityData(entity)
        callback.accept(data)
    }
}
