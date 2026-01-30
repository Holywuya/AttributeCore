package com.attributecore.listener

import com.attributecore.data.AttributeType
import com.attributecore.data.SubAttribute
import com.attributecore.event.UpdateEventData
import com.attributecore.manager.AttributeManager
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.inventory.InventoryClickEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit

object EquipmentListener {
    @SubscribeEvent
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? org.bukkit.entity.Player ?: return
        submit(delay = 1) {
            updatePlayerAttributes(player)
        }
    }

    @SubscribeEvent
    fun onItemHeld(event: PlayerItemHeldEvent) {
        submit(delay = 1) {
            updatePlayerAttributes(event.player)
        }
    }

    @SubscribeEvent
    fun onSwapHand(event: PlayerSwapHandItemsEvent) {
        submit(delay = 1) {
            updatePlayerAttributes(event.player)
        }
    }

    @SubscribeEvent
    fun onPlayerQuit(event: PlayerQuitEvent) {
        AttributeManager.clearEntityData(event.player.uniqueId)
    }

    @SubscribeEvent
    fun onPlayerDeath(event: PlayerDeathEvent) {
        AttributeManager.clearEntityData(event.entity.uniqueId)
    }

    private fun updatePlayerAttributes(player: org.bukkit.entity.Player) {
        val data = AttributeManager.loadEntityData(player)
        val updateEvent = UpdateEventData(player)

        SubAttribute.getAttributes()
            .filter { it.containsType(AttributeType.Update) }
            .forEach { it.eventMethod(data, updateEvent) }
    }
}
