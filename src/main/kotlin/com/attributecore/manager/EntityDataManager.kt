package com.attributecore.manager

import com.attributecore.data.AttributeData
import org.bukkit.entity.LivingEntity
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object EntityDataManager {

    private val entityDataCache = ConcurrentHashMap<UUID, AttributeData>()

    fun getData(entity: LivingEntity): AttributeData =
        entityDataCache.getOrPut(entity.uniqueId) { AttributeData(entity) }

    fun removeData(uuid: UUID) {
        entityDataCache.remove(uuid)
        ShieldManager.removeData(uuid)
        ReactionManager.clear(uuid)
    }

    fun clear() {
        entityDataCache.keys.forEach { uuid ->
            removeData(uuid)
        }
    }

    fun contains(uuid: UUID): Boolean = entityDataCache.containsKey(uuid)

    fun size(): Int = entityDataCache.size

    fun getLoadedUUIDs(): Set<UUID> = entityDataCache.keys.toSet()
}