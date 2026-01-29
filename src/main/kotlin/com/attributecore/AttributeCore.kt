package com.attributecore

import com.attributecore.manager.AttributeManager
import ink.ptms.um.Mythic
import org.bukkit.Bukkit
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info

object AttributeCore : Plugin() {

    override fun onEnable() {
        info("§a[AttributeCore] 插件正在启用...")

        // 初始化属性管理器
        AttributeManager.init()

        // 检测并注册依赖插件
        detectDependencies()

        info("§a[AttributeCore] 插件启用完成！")
    }

    override fun onDisable() {
        info("§c[AttributeCore] 插件已禁用！")
    }

    private fun detectDependencies() {
        detectPlaceholderAPI()
        detectMythicMobs()
    }

    private fun detectPlaceholderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            info("[AttributeCore] 检测到 PlaceholderAPI，已注册")
        }
    }

    private fun detectMythicMobs() {
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
            info("[AttributeCore] 检测到 MythicMobs，已注册")
            if (Mythic.isLoaded()) {
                val api = Mythic.API
                if (api.isLegacy) {
                    info("[AttributeCore] MythicMobs 4.X 已注册")
                } else {
                    info("[AttributeCore] MythicMobs 5.X 已注册")
                }
            }
        }
    }
}
