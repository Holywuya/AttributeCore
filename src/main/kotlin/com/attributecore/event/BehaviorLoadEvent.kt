package com.attributecore.event

import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * 自定义行为加载器
 * 负责加载 behaviors 文件夹下的脚本配置
 */
object BehaviorLoader {

    private const val FOLDER_NAME = "behaviors"

    // 缓存所有加载的脚本: key = 行为名(小写), value = Kether脚本内容
    val scriptCache = ConcurrentHashMap<String, String>()

    private val folder: File
        get() = File(getDataFolder(), FOLDER_NAME)

    fun loadBehaviors() {
        // 1. 清理旧缓存
        scriptCache.clear()

        // 2. 初始化文件夹
        if (!folder.exists()) {
            folder.mkdirs()
        }

        // 3. 扫描 .yml 文件
        val files = folder.listFiles { _, name -> name.endsWith(".yml") }
        if (files.isNullOrEmpty()) {
            console().sendMessage("§e[BehaviorLoader] §f未找到自定义行为文件。")
            return
        }

        var count = 0
        files.forEach { file ->
            try {
                val config = Configuration.loadFromFile(file, Type.YAML)
                // 遍历文件中的每一个 Key
                config.getKeys(false).forEach { key ->
                    // 获取脚本内容 (支持多行文本 |-)
                    val script = config.getString(key)
                    if (!script.isNullOrBlank()) {
                        // 存入缓存，key 转小写以忽略大小写差异
                        scriptCache[key.lowercase()] = script
                        count++
                    }
                }
            } catch (e: Exception) {
                console().sendMessage("§c[BehaviorLoader] §f加载行为文件 ${file.name} 失败: ${e.message}")
            }
        }

        console().sendMessage("§a[BehaviorLoader] §f已加载 §b$count §f个自定义行为脚本")
    }
}
