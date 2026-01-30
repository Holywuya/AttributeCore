package com.attributecore.manager

import com.attributecore.data.AttributeData
import com.attributecore.data.SubAttribute
import com.attributecore.util.DebugLogger
import org.bukkit.entity.LivingEntity
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object AttributeManager {
    private val entityDataCache = ConcurrentHashMap<UUID, AttributeData>()

    @Awake(LifeCycle.ENABLE)
    fun init() {
        SubAttribute.getAttributes().forEach { attribute ->
            attribute.loadConfig(getConfigDir())
        }
    }

    fun getEntityData(entity: LivingEntity): AttributeData {
        val cached = entityDataCache[entity.uniqueId]
        if (cached != null) {
            return cached
        }
        DebugLogger.logAttributeLoading("首次加载实体属性: ${entity.name}")
        return loadEntityData(entity)
    }

    fun clearEntityData(uuid: UUID) {
        entityDataCache.remove(uuid)
    }

    fun updateEntityData(entity: LivingEntity, newData: AttributeData) {
        entityDataCache[entity.uniqueId] = newData
    }

    fun loadEntityData(entity: LivingEntity): AttributeData {
        val data = AttributeData()
        val equipment = entity.equipment ?: return data

        DebugLogger.logEquipmentUpdate("加载实体装备: ${entity.name}")

        listOf(
            equipment.itemInMainHand,
            equipment.itemInOffHand,
            equipment.helmet,
            equipment.chestplate,
            equipment.leggings,
            equipment.boots
        ).filterNotNull().forEach { item ->
            if (!item.hasItemMeta()) return@forEach
            val lore = item.itemMeta?.lore ?: return@forEach

            DebugLogger.logAttributeLoading("扫描物品: ${item.type}, Lore 行数: ${lore.size}")

            lore.forEach { line ->
                ItemAttributeReader.parseAttributesFromLore(data, line)
            }

            ItemAttributeReader.parseAttributesFromNBT(data, item)
        }

        DebugLogger.logAttributeLoading("加载完成，属性数据: ${data.getNonZeroAttributes()}")
        updateEntityData(entity, data)
        return data
    }

    private fun getConfigDir(): java.io.File {
        return java.io.File("plugins/AttributeCore/attributes")
    }
}
