package com.attributecore.listener

import com.attributecore.manager.AttributeManager
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.*
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit

/**
 * 属性更新监听器
 * 参考 SX-Attribute 的 ListenerUpdateAttribute 实现
 * 负责在装备变动时实时刷新属性
 */
object AttributeUpdateListener {

    /**
     * 玩家进服：立即更新属性
     */
    @SubscribeEvent
    fun onJoin(e: PlayerJoinEvent) {
        AttributeManager.update(e.player)
    }

    /**
     * 玩家退服：清理缓存数据，防止内存泄漏
     */
    @SubscribeEvent
    fun onQuit(e: PlayerQuitEvent) {
        AttributeManager.removeData(e.player.uniqueId)
    }

    /**
     * 关闭背包：通常意味着装备栏或物品栏发生了变动
     * 延迟 1 tick 确保物品已归位
     */
    @SubscribeEvent
    fun onClose(e: InventoryCloseEvent) {
        if (e.player is Player) {
            updateAsync(e.player as Player)
        }
    }

    /**
     * 切换快捷栏物品 (1-9)
     */
    @SubscribeEvent
    fun onItemHeld(e: PlayerItemHeldEvent) {
        updateAsync(e.player)
    }

    /**
     * 切换副手物品 (F键)
     */
    @SubscribeEvent
    fun onSwap(e: PlayerSwapHandItemsEvent) {
        updateAsync(e.player)
    }

    /**
     * 丢弃物品
     */
    @SubscribeEvent
    fun onDrop(e: PlayerDropItemEvent) {
        updateAsync(e.player)
    }

    /**
     * 拾取物品 (捡起装备可能直接进入装备栏或手持)
     */
    @SubscribeEvent
    fun onPickup(e: EntityPickupItemEvent) {
        if (e.entity is Player) {
            updateAsync(e.entity as Player)
        }
    }

    /**
     * 玩家重生
     */
    @SubscribeEvent
    fun onRespawn(e: PlayerRespawnEvent) {
        updateAsync(e.player)
    }

    /**
     * 辅助方法：延迟 1 tick 更新属性
     * Bukkit 事件触发时，背包数据可能尚未更新完毕，因此需要延迟
     */
    private fun updateAsync(player: Player) {
        submit(delay = 1) {
            if (player.isOnline) {
                AttributeManager.update(player)
            }
        }
    }
}