package com.attributecore.event

import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.File

data class ReactionConfig(
    val id: String,
    val display: String,
    val elements: Set<String>,
    val behavior: String,
    val consume: Boolean
)

object ReactionLoader {
    private val file = File(getDataFolder(), "reactions.yml")
    val reactionCache = mutableListOf<ReactionConfig>()
    var auraDuration = 100L

    fun load() {
        console().sendMessage("§7[AttributeCore] §f正在加载元素反应配置文件 §ereactions.yml§f...")

        // 1. 自动释放单文件资源
        if (!file.exists()) {
            try {
                releaseResourceFile("reactions.yml", false)
                console().sendMessage("§7[AttributeCore] §8已释放默认资源文件: §freactions.yml")
            } catch (e: Exception) {
                console().sendMessage("§7[AttributeCore] §8未在 Jar 中找到 reactions.yml，跳过释放步骤")
            }
        }

        try {
            val conf = Configuration.loadFromFile(file, Type.YAML)

            // 同步全局配置中的持续时间
            auraDuration = CoreConfig.auraDuration
            reactionCache.clear()

            val section = conf.getConfigurationSection("reactions")
            if (section == null) {
                console().sendMessage("§7[AttributeCore] §e警告: reactions.yml 中未找到 'reactions' 配置节")
                return
            }

            val keys = section.getKeys(false)
            keys.forEach { key ->
                try {
                    val elements = section.getStringList("$key.elements").map { it.uppercase() }.toSet()
                    val behavior = section.getString("$key.behavior") ?: ""

                    if (elements.size < 2) {
                        console().sendMessage("§7[AttributeCore] §e忽略反应 §f$key §e(需要至少 2 种元素参与)")
                        return@forEach
                    }

                    reactionCache.add(ReactionConfig(
                        id = key,
                        display = section.getString("$key.display") ?: key,
                        elements = elements,
                        behavior = behavior,
                        consume = section.getBoolean("$key.consume", true)
                    ))

                    // 打印详细的反应加载信息
                    val elementList = elements.joinToString(" + ")
                    console().sendMessage("§7[AttributeCore] §a已载入反应: §f$key §8[$elementList] §7行为: §8$behavior")
                } catch (e: Exception) {
                    console().sendMessage("§c[AttributeCore] §f解析反应条目 §e$key §c失败: ${e.localizedMessage}")
                }
            }
        } catch (e: Exception) {
            console().sendMessage("§c[AttributeCore] §f读取 §ereactions.yml §c失败 (YAML格式错误?): ${e.localizedMessage}")
        }

        console().sendMessage("§7[AttributeCore] §f加载完成，共计识别 §b${reactionCache.size} §f个元素反应公式")
    }
}