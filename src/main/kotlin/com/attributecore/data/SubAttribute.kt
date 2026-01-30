package com.attributecore.data

import com.attributecore.event.EventData
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import java.io.File
import java.util.regex.Pattern

abstract class SubAttribute(
    val name: String,
    vararg val types: AttributeType
) : Comparable<SubAttribute> {
    var priority: Int = -1
        internal set

    var combatPowerWeight: Double = 1.0
        protected set

    protected var config: YamlConfiguration? = null

    companion object {
        private val attributes = mutableListOf<SubAttribute>()

        fun getAttributes(): List<SubAttribute> = attributes.toList()

        fun getByName(name: String): SubAttribute? = attributes.find { it.name == name }

        internal fun register(attribute: SubAttribute) {
            if (attribute.priority < 0) {
                return
            }
            val existing = attributes.find { it.priority == attribute.priority }
            if (existing != null) {
                attributes.remove(existing)
            }
            attributes.add(attribute)
            attributes.sortBy { it.priority }
        }
    }

    fun loadConfig(configDir: File) {
        val configFile = File(configDir, "$name.yml")
        if (!configFile.exists()) {
            val default = defaultConfig()
            if (default != null) {
                configFile.parentFile.mkdirs()
                default.save(configFile)
            }
        }
        if (configFile.exists()) {
            config = YamlConfiguration.loadConfiguration(configFile)
        }
    }

    open fun defaultConfig(): YamlConfiguration? = null

    abstract fun loadAttribute(attributeData: AttributeData, lore: String)

    abstract fun eventMethod(attributeData: AttributeData, eventData: EventData)

    abstract fun getPlaceholder(attributeData: AttributeData, player: Player, identifier: String): Any?

    open fun getPlaceholders(): List<String> = emptyList()

    open fun calculationCombatPower(attributeData: AttributeData): Double {
        return attributeData[name] * combatPowerWeight
    }

    fun containsType(type: AttributeType): Boolean {
        return types.contains(type)
    }

    override fun compareTo(other: SubAttribute): Int {
        return priority.compareTo(other.priority)
    }

    protected fun createPattern(prefix: String, suffix: String = ""): Pattern {
        // 支持两种格式：
        // 1. 带颜色代码：§c攻击力 §f100 或 §c攻击力：§f100
        // 2. 不带颜色代码：攻击力：100 或 攻击力 100
        val regex = if (suffix.isEmpty()) {
            "(?:§.)?${prefix}(?:§.)?[：: ]?(?:§.)?([+-]?\\d+\\.?\\d*)"
        } else {
            "(?:§.)?${prefix}(?:§.)?[：: ]?(?:§.)?([+-]?\\d+\\.?\\d*)(?:§.)?${suffix}"
        }
        return Pattern.compile(regex)
    }

    protected fun matchValue(lore: String, pattern: Pattern): Double? {
        val matcher = pattern.matcher(lore)
        return if (matcher.find()) {
            matcher.group(1).toDoubleOrNull()
        } else null
    }
}
