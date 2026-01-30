package com.attributecore.util

import com.attributecore.AttributeCore
import org.bukkit.configuration.file.YamlConfiguration
import taboolib.common.platform.function.getDataFolder
import java.io.File

object Config {

    private lateinit var config: YamlConfiguration
    private lateinit var message: YamlConfiguration

    const val DEFAULT_ATTRIBUTE = "DefaultAttribute"
    const val ATTRIBUTE_PRIORITY = "AttributePriority"
    const val DAMAGE_EVENT_PRIORITY = "DamageEventPriority"
    const val MINIMUM_DAMAGE = "MinimumDamage"
    const val DAMAGE_CALCULATION_TO_EVE = "DamageCalculationToEVE"
    const val BOW_CLOSE_RANGE_ATTACK = "BowCloseRangeAttack"
    const val DAMAGE_GAUGES = "DamageGauges"
    const val REFRESH_INTERVAL = "RefreshInterval"
    const val DEBUG = "Debug"
    const val DAMAGE_EVENT_BLACK_LIST = "DamageEventBlackList"

    fun load() {
        val configFile = File(getDataFolder(), "config.yml")
        if (!configFile.exists()) {
            config = defaultConfig()
            save()
        } else {
            config = YamlConfiguration.loadConfiguration(configFile)
        }

        val messageFile = File(getDataFolder(), "message.yml")
        if (!messageFile.exists()) {
            message = defaultMessage()
            saveMessage(messageFile)
        } else {
            message = YamlConfiguration.loadConfiguration(messageFile)
        }
    }

    fun reload() {
        load()
    }

    fun getConfig(): YamlConfiguration = config

    fun getMessage(): YamlConfiguration = message

    fun save() {
        try {
            config.save(File(getDataFolder(), "config.yml"))
        } catch (e: Exception) {
            AttributeCore.logger.warning("Failed to save config: ${e.message}")
        }
    }

    private fun saveMessage(file: File) {
        try {
            message.save(file)
        } catch (e: Exception) {
            AttributeCore.logger.warning("Failed to save message: ${e.message}")
        }
    }

    private fun defaultConfig(): YamlConfiguration {
        val yaml = YamlConfiguration()
        
        yaml.set("$ATTRIBUTE_PRIORITY", listOf(
            "Damage#AttributeCore",
            "Crit#AttributeCore",
            "HitRate#AttributeCore",
            "Ignition#AttributeCore",
            "LifeSteal#AttributeCore",
            "Lightning#AttributeCore",
            "Real#AttributeCore",
            "Defense#AttributeCore",
            "Block#AttributeCore",
            "Dodge#AttributeCore",
            "Reflection#AttributeCore",
            "Toughness#AttributeCore",
            "Health#AttributeCore",
            "HealthRegen#AttributeCore",
            "WalkSpeed#AttributeCore",
            "AttackSpeed#AttributeCore"
        ))
        
        yaml.set("$DEFAULT_ATTRIBUTE", listOf<String>())
        yaml.set(DAMAGE_EVENT_PRIORITY, "HIGH")
        yaml.set(MINIMUM_DAMAGE, 1.0)
        yaml.set(DAMAGE_CALCULATION_TO_EVE, false)
        yaml.set(BOW_CLOSE_RANGE_ATTACK, false)
        yaml.set(DAMAGE_GAUGES, true)
        yaml.set(REFRESH_INTERVAL, 20)
        yaml.set(DEBUG, false)
        yaml.set(DAMAGE_EVENT_BLACK_LIST, listOf("FALL", "SUFFOCATION"))
        
        return yaml
    }

    private fun defaultMessage(): YamlConfiguration {
        val yaml = YamlConfiguration()
        yaml.set("Prefix", "§7[§6AttributeCore§7] §f")
        yaml.set("NoPermission", "§c你没有权限执行此命令")
        yaml.set("PlayerNotFound", "§c玩家不存在或不在线")
        yaml.set("ReloadSuccess", "§a配置重载成功！")
        return yaml
    }

    val attributePriority: List<String>
        get() = config.getStringList(ATTRIBUTE_PRIORITY)

    val defaultAttribute: List<String>
        get() = config.getStringList(DEFAULT_ATTRIBUTE)

    val damageEventPriority: String
        get() = config.getString(DAMAGE_EVENT_PRIORITY, "HIGH") ?: "HIGH"

    val minimumDamage: Double
        get() = config.getDouble(MINIMUM_DAMAGE, 1.0)

    val isDamageCalculationToEVE: Boolean
        get() = config.getBoolean(DAMAGE_CALCULATION_TO_EVE, false)

    val isBowCloseRangeAttack: Boolean
        get() = config.getBoolean(BOW_CLOSE_RANGE_ATTACK, false)

    val isDamageGauges: Boolean
        get() = config.getBoolean(DAMAGE_GAUGES, true)

    val refreshInterval: Long
        get() = config.getLong(REFRESH_INTERVAL, 20)

    val debug: Boolean
        get() = config.getBoolean(DEBUG, false)

    val damageEventBlackList: List<String>
        get() = config.getStringList(DAMAGE_EVENT_BLACK_LIST)

    fun getMessage(key: String): String {
        return message.getString(key, key) ?: key
    }
}