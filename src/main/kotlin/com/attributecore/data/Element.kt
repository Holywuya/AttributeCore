package com.attributecore.data

import com.attributecore.AttributeCore

/**
 * 元素常量和工具类
 * 
 * 元素不再是硬编码的枚举，而是基于字符串的动态系统
 * 任何属性都可以通过设置 element 配置来定义自己的元素类型
 * 
 * 预定义的元素常量仅用于便利，用户可以定义任意元素名称
 */
object Elements {
    const val PHYSICAL = "PHYSICAL"
    const val FIRE = "FIRE"
    const val WATER = "WATER"
    const val ICE = "ICE"
    const val ELECTRO = "ELECTRO"
    const val WIND = "WIND"
    
    private val displayNames = mapOf(
        PHYSICAL to "物理",
        FIRE to "火",
        WATER to "水",
        ICE to "冰",
        ELECTRO to "雷",
        WIND to "风"
    )
    
    private val colors = mapOf(
        PHYSICAL to "§f",
        FIRE to "§c",
        WATER to "§9",
        ICE to "§b",
        ELECTRO to "§5",
        WIND to "§a"
    )
    
    private val normalizeCache = java.util.concurrent.ConcurrentHashMap<String, String>()
    
    /**
     * 获取元素的显示名称
     * 如果是自定义元素，返回元素名本身
     */
    fun getDisplayName(element: String): String {
        return displayNames[element.uppercase()] ?: element
    }
    
    /**
     * 获取元素的颜色代码
     * 如果是自定义元素，返回白色
     */
    fun getColor(element: String): String {
        return colors[element.uppercase()] ?: "§f"
    }
    
    /**
     * 获取带颜色的显示名称
     */
    fun getColoredName(element: String): String {
        return "${getColor(element)}${getDisplayName(element)}"
    }
    
    /**
     * 检查是否为物理元素
     */
    fun isPhysical(element: String): Boolean {
        return element.uppercase() == PHYSICAL
    }
    
    /**
     * 检查是否为非物理元素（可参与反应）
     */
    fun isReactive(element: String): Boolean {
        return !isPhysical(element)
    }
    
    /**
     * 获取元素的抗性属性名
     */
    fun resistanceKey(element: String): String {
        return "${element.lowercase()}_resistance"
    }
    
    /**
     * 获取元素的伤害属性名
     */
    fun damageKey(element: String): String {
        return "${element.lowercase()}_damage"
    }
    
    fun penetrationKey(element: String): String {
        return "${element.lowercase()}_penetration"
    }
    
    /**
     * 获取元素的光环衰减速率（每秒）
     * 可在配置文件中自定义
     */
    fun getDecayRate(element: String): Double {
        return AttributeCore.config.getDouble(
            "elements.types.${element.lowercase()}.aura-decay-rate",
            1.0
        )
    }
    
    /**
     * 获取元素的抗性上限
     * 可在配置文件中自定义
     */
    fun getMaxResistance(element: String): Double {
        return AttributeCore.config.getDouble(
            "elements.types.${element.lowercase()}.max-resistance",
            80.0
        )
    }
    
    fun normalize(element: String): String {
        return normalizeCache.getOrPut(element) { element.trim().uppercase() }
    }
}

/**
 * 保留旧的 Element 枚举用于向后兼容
 * @deprecated 使用 Elements 对象和字符串元素名代替
 */
@Deprecated("Use Elements object with String element names instead")
enum class Element(
    val displayName: String,
    val color: String,
    val configKey: String
) {
    PHYSICAL("物理", "§f", "physical"),
    FIRE("火", "§c", "fire"),
    WATER("水", "§9", "water"),
    ICE("冰", "§b", "ice"),
    ELECTRO("雷", "§5", "electro"),
    WIND("风", "§a", "wind");

    fun coloredName(): String = "$color$displayName"
    fun resistanceKey(): String = "${configKey}_resistance"
    fun damageKey(): String = "${configKey}_damage"

    fun getDecayRate(): Double {
        return Elements.getDecayRate(name)
    }

    fun getMaxResistance(): Double {
        return Elements.getMaxResistance(name)
    }

    companion object {
        fun fromConfigKey(key: String): Element? {
            return entries.find { it.configKey.equals(key, ignoreCase = true) }
        }

        fun reactiveElements(): List<Element> {
            return entries.filter { it != PHYSICAL }
        }

        fun fromString(name: String): Element? {
            val normalized = name.trim().uppercase()
            return entries.find { it.name == normalized }
                ?: entries.find { it.configKey.equals(name, ignoreCase = true) }
                ?: entries.find { it.displayName == name }
        }
    }
}
