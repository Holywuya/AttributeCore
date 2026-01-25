package com.attributecore.listener

import com.attributecore.manager.AttributeManager
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.world.ChunkUnloadEvent
import taboolib.common.platform.event.SubscribeEvent

object CleanupListener {

    /**
     * 怪物死亡清理
     */
    @SubscribeEvent
    fun onDeath(e: EntityDeathEvent) {
        AttributeManager.removeData(e.entity.uniqueId)
    }

    /**
     * 区块卸载清理
     * 如果怪物所在的区块卸载了，通常这些怪物的属性缓存也可以清理
     * 玩家再次靠近时会重新生成/扫描
     */
    @SubscribeEvent
    fun onChunkUnload(e: ChunkUnloadEvent) {
        e.chunk.entities.forEach { entity ->
            // 玩家除外，玩家数据由 PlayerQuitEvent 处理
            if (entity !is org.bukkit.entity.Player) {
                AttributeManager.removeData(entity.uniqueId)
            }
        }
    }
}