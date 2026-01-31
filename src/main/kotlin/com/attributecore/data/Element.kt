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

        /**
         * 根据字符串名称获取元素类型
         * 支持 enum 名称、configKey、displayName
         * @param name 元素名称字符串 (如 "FIRE", "fire", "火")
         * @return 对应的 Element，如果找不到返回 null
         */
        fun fromString(name: String): Element? {
            val normalized = name.trim().uppercase()
            return entries.find { it.name == normalized }
                ?: entries.find { it.configKey.equals(name, ignoreCase = true) }
                ?: entries.find { it.displayName == name }
        }
    }
}
