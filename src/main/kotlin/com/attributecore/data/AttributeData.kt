package com.attributecore.data

import org.bukkit.entity.LivingEntity

/**
 * 属性数据容器，存储实体当前的所有属性值
 * 参考了 SXAttribute 的数据结构[20]
 */
class AttributeData(val entity: LivingEntity) {

    /** 存储属性值，key为属性标识符，value为DoubleArray[最小值, 最大值][20] */
    val values = mutableMapOf<String, DoubleArray>()

    /**
     * 设置属性值
     * @param key 属性标识符
     * @param value 属性值
     */
    fun setValue(key: String, value: Double) {
        values[key] = doubleArrayOf(value, value)
    }

    /**
     * 设置属性范围值
     * @param key 属性标识符
     * @param min 最小值
     * @param max 最大值
     */
    fun setValueRange(key: String, min: Double, max: Double) {
        values[key] = doubleArrayOf(min, max)
    }

    /**
     * 获取属性值数组 [最小值, 最大值][20]
     * @param key 属性标识符或名称
     */
    fun get(key: String): DoubleArray {
        return values[key] ?: doubleArrayOf(0.0, 0.0)
    }

    /**
     * 获取属性的基础值（最小值）
     */
    fun getBaseValue(key: String): Double {
        return get(key)[0]
    }

    /**
     * 重置所有属性值
     */
    fun reset() {
        values.clear()
    }
}
