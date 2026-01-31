package com.attributecore.data

import com.attributecore.AttributeCore

/**
 * 元素类型枚举
 * 
 * 定义游戏中所有可用的元素类型
 * 物理 (PHYSICAL) 作为默认元素，不参与元素反应
 */
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

    /**
     * 获取带颜色的显示名称
     */
    fun coloredName(): String = "$color$displayName"

    /**
     * 获取该元素对应的抗性属性名
     */
    fun resistanceKey(): String = "${configKey}_resistance"

    /**
     * 获取该元素对应的伤害属性名
     */
    fun damageKey(): String = "${configKey}_damage"

    /**
     * 获取附着衰减速率（每秒）
     */
    fun getDecayRate(): Double {
        return AttributeCore.config.getDouble("elements.types.${configKey}.aura-decay-rate", 1.0)
    }

    /**
     * 获取抗性上限
     */
    fun getMaxResistance(): Double {
        return AttributeCore.config.getDouble("elements.types.${configKey}.max-resistance", 80.0)
    }

    companion object {
        private val prefixMap = mapOf(
            "fire_" to FIRE,
            "water_" to WATER,
            "ice_" to ICE,
            "electro_" to ELECTRO,
            "wind_" to WIND
        )

        private val cnNameMap = mapOf(
            "火" to FIRE,
            "水" to WATER,
            "冰" to ICE,
            "雷" to ELECTRO,
            "风" to WIND,
            "物理" to PHYSICAL
        )

        /**
         * 根据属性名推断元素类型
         * 例如: "fire_damage" -> FIRE, "攻击力" -> PHYSICAL
         */
        fun fromAttributeName(name: String): Element {
            // 检查英文前缀
            for ((prefix, element) in prefixMap) {
                if (name.startsWith(prefix, ignoreCase = true)) {
                    return element
                }
            }
            // 检查中文名
            for ((cnName, element) in cnNameMap) {
                if (name.contains(cnName)) {
                    return element
                }
            }
            // 默认为物理
            return PHYSICAL
        }

        /**
         * 根据配置键获取元素
         */
        fun fromConfigKey(key: String): Element? {
            return entries.find { it.configKey.equals(key, ignoreCase = true) }
        }

        /**
         * 获取所有非物理元素（可参与反应的元素）
         */
        fun reactiveElements(): List<Element> {
            return entries.filter { it != PHYSICAL }
        }
    }
}
