package com.attributecore.data

import com.attributecore.event.EventData
import com.attributecore.util.DebugLogger
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.regex.Pattern

/**
 * 属性抽象基类
 * 参考 AttributePlus 的 SubAttribute 设计
 *
 * @property name 属性唯一标识符
 * @property types 属性类型（支持多类型）
 */
abstract class SubAttribute(
    val name: String,
    vararg val types: AttributeType
) : Comparable<SubAttribute> {
    /**
     * 属性优先级（数值越小优先级越高）
     * 在配置文件或 JS 脚本中指定
     */
    var priority: Int = 100
        protected set

    /**
     * 战斗力权重
     */
    var combatPowerWeight: Double = 1.0
        protected set
    
    /**
     * 占位符名称（用于 PlaceholderAPI，默认等于 name）
     * 例如：攻击力属性的 placeholder 为 "attack_damage"
     * 使用 %attributecore_attack_damage% 获取
     */
    open val placeholder: String = name
    
    /**
     * NBT 属性名称（用于 NBT 读写，默认等于 name）
     * 由于 name 现在是中文 pattern，nbtName 默认等于 name
     */
    open val nbtName: String
        get() = name

    protected var config: YamlConfiguration? = null

    companion object {
        private val attributes = mutableListOf<SubAttribute>()
        
        // ===== 性能优化：预分类缓存 =====
        @Volatile
        private var cachedAttackAttributes: List<SubAttribute>? = null
        @Volatile
        private var cachedDefenceAttributes: List<SubAttribute>? = null
        @Volatile
        private var cachedKillerAttributes: List<SubAttribute>? = null
        @Volatile
        private var cachedUpdateAttributes: List<SubAttribute>? = null
        @Volatile
        private var cachedAttributesView: List<SubAttribute>? = null

        /**
         * 获取所有属性的不可变视图（性能优化：避免每次 toList()）
         */
        fun getAttributes(): List<SubAttribute> {
            return cachedAttributesView ?: synchronized(attributes) {
                cachedAttributesView ?: java.util.Collections.unmodifiableList(ArrayList(attributes)).also {
                    cachedAttributesView = it
                }
            }
        }
        
        /**
         * 获取攻击类型属性（预缓存，性能优化）
         */
        fun getAttackAttributes(): List<SubAttribute> {
            return cachedAttackAttributes ?: synchronized(attributes) {
                cachedAttackAttributes ?: attributes.filter { it.containsType(AttributeType.Attack) }
                    .sortedBy { it.priority }
                    .also { cachedAttackAttributes = it }
            }
        }
        
        /**
         * 获取防御类型属性（预缓存，性能优化）
         */
        fun getDefenceAttributes(): List<SubAttribute> {
            return cachedDefenceAttributes ?: synchronized(attributes) {
                cachedDefenceAttributes ?: attributes.filter { it.containsType(AttributeType.Defence) }
                    .sortedBy { it.priority }
                    .also { cachedDefenceAttributes = it }
            }
        }
        
        /**
         * 获取击杀类型属性（预缓存，性能优化）
         */
        fun getKillerAttributes(): List<SubAttribute> {
            return cachedKillerAttributes ?: synchronized(attributes) {
                cachedKillerAttributes ?: attributes.filter { it.containsType(AttributeType.Killer) }
                    .sortedBy { it.priority }
                    .also { cachedKillerAttributes = it }
            }
        }
        
        /**
         * 获取更新类型属性（预缓存，性能优化）
         */
        fun getUpdateAttributes(): List<SubAttribute> {
            return cachedUpdateAttributes ?: synchronized(attributes) {
                cachedUpdateAttributes ?: attributes.filter { it.containsType(AttributeType.Update) }
                    .sortedBy { it.priority }
                    .also { cachedUpdateAttributes = it }
            }
        }
        
        /**
         * 使缓存失效（属性变更时调用）
         */
        private fun invalidateCache() {
            cachedAttackAttributes = null
            cachedDefenceAttributes = null
            cachedKillerAttributes = null
            cachedUpdateAttributes = null
            cachedAttributesView = null
        }

        fun getByName(name: String): SubAttribute? = attributes.find { it.name == name }

        /**
         * 注册属性（简化版，直接添加到列表并排序）
         */
        fun register(attribute: SubAttribute) {
            synchronized(attributes) {
                val existing = attributes.find { it.name == attribute.name }
                if (existing != null) {
                    attributes.remove(existing)
                }
                attributes.add(attribute)
                attributes.sortBy { it.priority }
                invalidateCache()
            }
        }

        /**
         * 重新排序属性列表
         */
        fun resort() {
            synchronized(attributes) {
                attributes.sortBy { it.priority }
                invalidateCache()
            }
        }
        
        /**
         * 清空所有属性（用于重载）
         */
        fun clear() {
            synchronized(attributes) {
                attributes.clear()
                invalidateCache()
            }
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
        // 支持多种格式：
        // 1. 带颜色代码：§c攻击力 §f100 或 §c攻击力：§f100
        // 2. 不带颜色代码：攻击力：100 或 攻击力 100 或 攻击力: 100（冒号+空格）
        // 3. 多个颜色代码或无颜色代码：(?:§.)*
        val regex = if (suffix.isEmpty()) {
            "(?:§.)*${prefix}(?:§.)*[：: ]*(?:§.)*([+-]?\\d+\\.?\\d*)"
        } else {
            "(?:§.)*${prefix}(?:§.)*[：: ]*(?:§.)*([+-]?\\d+\\.?\\d*)(?:§.)*${suffix}"
        }
        return Pattern.compile(regex)
    }

    protected fun matchValue(lore: String, pattern: Pattern): Double? {
        val matcher = pattern.matcher(lore)
        val matched = matcher.find()
        if (matched) {
            val value = matcher.group(1).toDoubleOrNull()
            DebugLogger.logRegexMatch("匹配成功! Lore: $lore, 提取值: $value")
            return value
        } else {
            DebugLogger.logRegexMatch("匹配失败! Lore: $lore, Pattern: ${pattern.pattern()}")
            return null
        }
    }

    data class ParsedValue(val value: Double, val isPercent: Boolean)

    protected fun matchValueWithPercent(lore: String, pattern: Pattern): ParsedValue? {
        val matcher = pattern.matcher(lore)
        if (matcher.find()) {
            val value = matcher.group(1).toDoubleOrNull() ?: return null
            val isPercent = try {
                matcher.group(2)?.contains("%") == true
            } catch (e: Exception) {
                lore.contains("%")
            }
            DebugLogger.logRegexMatch("匹配成功! Lore: $lore, 值: $value, 百分比: $isPercent")
            return ParsedValue(value, isPercent)
        }
        return null
    }

    protected fun createPatternWithPercent(prefix: String): Pattern {
        val regex = "(?:§.)*${prefix}(?:§.)*[：: ]*(?:§.)*([+-]?\\d+\\.?\\d*)(?:§.)*(%?)"
        return Pattern.compile(regex)
    }
}
