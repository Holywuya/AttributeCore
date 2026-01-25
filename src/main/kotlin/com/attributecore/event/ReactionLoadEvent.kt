package com.attributecore.event

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
        if (!file.exists()) releaseResourceFile("reactions.yml")
        val conf = Configuration.loadFromFile(file, Type.YAML)

        auraDuration = conf.getLong("aura_duration", 100L)
        reactionCache.clear()

        val section = conf.getConfigurationSection("reactions") ?: return
        section.getKeys(false).forEach { key ->
            val el = section.getStringList("$key.elements").map { it.uppercase() }.toSet()
            reactionCache.add(ReactionConfig(
                id = key,
                display = section.getString("$key.display") ?: key,
                elements = el,
                behavior = section.getString("$key.behavior") ?: "",
                consume = section.getBoolean("$key.consume", true)
            ))
        }
    }
}