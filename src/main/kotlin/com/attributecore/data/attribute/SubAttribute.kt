package com.attributecore.data.attribute

import com.attributecore.AttributeCore
import com.attributecore.data.eventdata.EventData
import com.attributecore.util.Config
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.LivingEntity
import taboolib.common.platform.function.getDataFolder
import java.io.File
import java.text.DecimalFormat
import java.util.concurrent.CopyOnWriteArrayList

abstract class SubAttribute(
    val name: String,
    val length: Int,
    vararg val types: AttributeType
) : Comparable<SubAttribute> {

    companion object {
        private val attributes = CopyOnWriteArrayList<SubAttribute>()
        private val random = java.util.Random()

        fun getAttributes(): List<SubAttribute> = attributes.toList()

        fun getSubAttribute(name: String): SubAttribute? {
            return attributes.find { it.name == name }
        }

        fun clearAttributes() {
            attributes.clear()
        }

        fun getDf(): DecimalFormat = DecimalFormat("#.##")

        fun probability(value: Double): Boolean {
            return value > 0 && value / 100.0 > random.nextDouble()
        }

        fun getNumber(lore: String): Double {
            val str = lore.replace(Regex("§+[a-z0-9]"), "")
                .replace(Regex("[^-0-9.]"), "")
            return if (str.isEmpty() || str.replace(Regex("[^.]"), "").length > 1) 0.0
            else str.toDoubleOrNull() ?: 0.0
        }
    }

    var priority: Int = -1
        private set

    private var configFile: File? = null
    var config: YamlConfiguration? = null
        private set

    init {
        val priorityList = Config.attributePriority
        for (i in priorityList.indices) {
            val split = priorityList[i].split("#")
            if (split[0] == name) {
                priority = if (split.size > 1 && split[1] != "AttributeCore") -1 else i
                break
            }
        }
    }

    fun registerAttribute() {
        if (priority < 0) {
            AttributeCore.logger.warning("[Attribute] >> Disable [$name] - Priority not set!")
            return
        }

        val existingIndex = attributes.indexOfFirst { it.priority == priority }
        if (existingIndex >= 0) {
            val old = attributes[existingIndex]
            attributes[existingIndex] = this
            AttributeCore.logger.info("[Attribute] >> [$name] Cover to [${old.name}]")
        } else {
            attributes.add(this)
            AttributeCore.logger.config("[Attribute] >> Register [$name] to Priority $priority")
        }

        loadConfig()
    }

    fun loadConfig(): SubAttribute {
        configFile = File(getDataFolder(), "Attribute${File.separator}AttributeCore${File.separator}$name.yml")
        
        if (configFile?.exists() != true) {
            val default = YamlConfiguration()
            defaultConfig(default)?.let {
                config = it
                saveConfig()
            }
        } else {
            config = YamlConfiguration.loadConfiguration(configFile!!)
        }
        return this
    }

    fun saveConfig() {
        try {
            config?.save(configFile!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    internal fun setPriority(p: Int): SubAttribute {
        priority = p
        return this
    }

    fun containsType(type: AttributeType): Boolean {
        return types.contains(type)
    }

    fun getString(path: String, default: String = ""): String {
        return config?.getString(path, default) ?: default
    }

    fun getInt(path: String, default: Int = 0): Int {
        return config?.getInt(path, default) ?: default
    }

    fun getDouble(path: String, default: Double = 0.0): Double {
        return config?.getDouble(path, default) ?: default
    }

    fun getBoolean(path: String, default: Boolean = false): Boolean {
        return config?.getBoolean(path, default) ?: default
    }

    protected open fun defaultConfig(config: YamlConfiguration): YamlConfiguration? {
        return null
    }

    open fun onEnable() {}

    open fun onReload() {}

    open fun onDisable() {}

    abstract fun eventMethod(values: DoubleArray, eventData: EventData)

    abstract fun getPlaceholder(values: DoubleArray, entity: LivingEntity, placeholder: String): Any?

    abstract fun getPlaceholders(): List<String>

    fun getValue(values: DoubleArray, entity: LivingEntity, placeholder: String): Double {
        val result = getPlaceholder(values, entity, placeholder)?.toString() ?: return 0.0
        return if (result.contains(" - ")) {
            val split = result.split(" - ")
            val min = split[0].toDoubleOrNull() ?: 0.0
            val max = split[1].toDoubleOrNull() ?: min
            min + random.nextDouble() * (max - min)
        } else {
            result.toDoubleOrNull() ?: 0.0
        }
    }

    abstract fun loadAttribute(values: DoubleArray, lore: String)

    open fun correct(values: DoubleArray) {
        for (i in values.indices) {
            values[i] = values[i].coerceAtLeast(0.0)
        }
    }

    open fun calculationCombatPower(values: DoubleArray): Double {
        return 0.0
    }

    override fun compareTo(other: SubAttribute): Int {
        return priority.compareTo(other.priority)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SubAttribute) return false
        return name == other.name && priority == other.priority
    }

    override fun hashCode(): Int {
        return 31 * name.hashCode() + priority
    }

    override fun toString(): String {
        return "SubAttribute(name='$name', priority=$priority, types=${types.toList()}, length=$length)"
    }
}