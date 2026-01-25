package com.attributecore.listener

import com.attributecore.manager.AttributeManager
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.*
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.common.platform.service.PlatformExecutor.PlatformTask
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 属性更新监听器
 * 使用防抖逻辑优化性能，防止装备频繁变动导致的计算堆叠
 */
object AttributeUpdateListener {

    // 记录每个玩家当前的更新任务
    private val debounceTasks = ConcurrentHashMap<UUID, PlatformTask>()

    @SubscribeEvent
    fun onJoin(e: PlayerJoinEvent) {
        AttributeManager.update(e.player)
    }

    @SubscribeEvent
    fun onQuit(e: PlayerQuitEvent) {
        val uuid = e.player.uniqueId
        debounceTasks.remove(uuid)?.cancel()
        AttributeManager.removeData(uuid)
    }

    @SubscribeEvent
    fun onClose(e: InventoryCloseEvent) {
        if (e.player is Player) debounceUpdate(e.player as Player)
    }

    @SubscribeEvent
    fun onItemHeld(e: PlayerItemHeldEvent) {
        debounceUpdate(e.player)
    }

    @SubscribeEvent
    fun onSwap(e: PlayerSwapHandItemsEvent) {
        debounceUpdate(e.player)
    }

    @SubscribeEvent
    fun onDrop(e: PlayerDropItemEvent) {
        debounceUpdate(e.player)
    }

    @SubscribeEvent
    fun onPickup(e: EntityPickupItemEvent) {
        if (e.entity is Player) debounceUpdate(e.entity as Player)
    }

    @SubscribeEvent
    fun onRespawn(e: PlayerRespawnEvent) {
        debounceUpdate(e.player)
    }

    /**
     * 防抖更新逻辑
     */
    private fun debounceUpdate(player: Player) {
        val uuid = player.uniqueId

        // 1. 如果已有正在排队的更新任务，直接取消它
        debounceTasks[uuid]?.cancel()

        // 2. 创建一个新任务，延迟 2 Tick 执行
        // submit 函数返回的就是 PlatformExecutor.PlatformTask
        val task = submit(delay = 2) {
            if (player.isOnline) {
                AttributeManager.update(player)
            }
            // 执行完后从缓存移除
            debounceTasks.remove(uuid)
        }

        // 3. 存入 Map 以便后续可能的取消操作
        debounceTasks[uuid] = task
    }
}