package com.attributecore.data

import java.util.EnumMap

/**
 * 伤害桶 - 存储各元素类型的伤害值
 * 
 * 伤害不再是单一数值，而是按元素类型分别存储
 * 每种元素的伤害独立计算增幅和减免
 */
data class DamageBucket(
    private val damages: MutableMap<Element, Double> = EnumMap(Element::class.java)
) {
    /**
     * 获取指定元素的伤害值
     */
    operator fun get(element: Element): Double = damages[element] ?: 0.0

    /**
     * 设置指定元素的伤害值
     */
    operator fun set(element: Element, value: Double) {
        if (value > 0) {
            damages[element] = value
        } else {
            damages.remove(element)
        }
    }

    /**
     * 添加指定元素的伤害值
     */
    fun add(element: Element, value: Double) {
        if (value != 0.0) {
            damages[element] = (damages[element] ?: 0.0) + value
        }
    }

    /**
     * 乘以倍率
     */
    fun multiply(element: Element, multiplier: Double) {
        damages[element]?.let {
            damages[element] = it * multiplier
        }
    }

    /**
     * 对所有元素应用倍率
     */
    fun multiplyAll(multiplier: Double) {
        damages.replaceAll { _, v -> v * multiplier }
    }

    /**
     * 计算总伤害
     */
    fun total(): Double = damages.values.sum()

    /**
     * 获取包含伤害的所有元素
     */
    fun elements(): Set<Element> = damages.keys.toSet()

    /**
     * 获取非物理元素的伤害
     */
    fun elementalDamage(): Double = damages.entries
        .filter { it.key != Element.PHYSICAL }
        .sumOf { it.value }

    /**
     * 检查是否包含指定元素的伤害
     */
    fun hasElement(element: Element): Boolean = (damages[element] ?: 0.0) > 0

    /**
     * 检查是否包含任何非物理元素
     */
    fun hasElementalDamage(): Boolean = damages.keys.any { it != Element.PHYSICAL }

    /**
     * 克隆当前伤害桶
     */
    fun clone(): DamageBucket = DamageBucket(EnumMap(damages))

    /**
     * 清空所有伤害
     */
    fun clear() {
        damages.clear()
    }

    /**
     * 获取原始 Map（只读）
     */
    fun toMap(): Map<Element, Double> = damages.toMap()

    /**
     * 应用抗性计算
     * @param resistances 抗性数据 Map<Element, Double>
     * @param baseValue 防御公式基础值
     */
    fun applyResistances(resistances: Map<Element, Double>, baseValue: Double = 100.0) {
        damages.replaceAll { element, damage ->
            val resistance = resistances[element] ?: 0.0
            val maxResistance = element.getMaxResistance()
            val clampedResistance = resistance.coerceIn(0.0, maxResistance)
            // 减伤公式: damage * (1 - resistance / (resistance + baseValue))
            val reduction = clampedResistance / (clampedResistance + baseValue)
            damage * (1 - reduction)
        }
    }

    /**
     * 合并另一个伤害桶
     */
    fun merge(other: DamageBucket) {
        other.damages.forEach { (element, value) ->
            add(element, value)
        }
    }

    override fun toString(): String {
        val parts = damages.entries
            .filter { it.value > 0 }
            .joinToString(", ") { "${it.key.displayName}=${String.format("%.2f", it.value)}" }
        return "DamageBucket(total=${String.format("%.2f", total())}, $parts)"
    }

    companion object {
        /**
         * 从单一伤害值创建物理伤害桶
         */
        fun physical(damage: Double): DamageBucket {
            return DamageBucket().apply {
                this[Element.PHYSICAL] = damage
            }
        }

        /**
         * 从 AttributeData 构建伤害桶
         */
        fun fromAttributeData(data: AttributeData): DamageBucket {
            val bucket = DamageBucket()
            
            // 遍历所有属性，根据属性名推断元素类型
            data.getAll().forEach { (key, value) ->
                if (key.endsWith("_damage") || key == "attack_damage" || key.contains("攻击力")) {
                    val element = Element.fromAttributeName(key)
                    bucket.add(element, value)
                }
            }
            
            return bucket
        }
    }
}
