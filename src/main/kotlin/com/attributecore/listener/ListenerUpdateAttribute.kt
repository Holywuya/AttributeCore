package com.attributecore.listener

import com.attributecore.AttributeCore
import com.attributecore.util.Config
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.submit

class ListenerUpdateAttribute : Listener {

    @Awake(LifeCycle.ENABLE)
    fun startUpdateTask() {
        submit(period = Config.refreshInterval) {
            Bukkit.getOnlinePlayers().forEach { player ->
                AttributeCore.attributeManager.loadEntityData(player)
                AttributeCore.attributeManager.attributeUpdateEvent(player)
            }
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        submit(delay = 20) {
            AttributeCore.attributeManager.loadEntityData(event.player)
            AttributeCore.attributeManager.attributeUpdateEvent(event.player)
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        AttributeCore.attributeManager.clearEntityData(event.player.uniqueId)
    }
}