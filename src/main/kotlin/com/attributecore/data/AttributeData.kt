package com.attributecore.data

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * 存储某个实体的所有属性值
 * Key: 属性内部名 (例如 "Damage")
 * Value: 属性值 (例如 100.0)
 */
class AttributeData(val uuid: UUID) {
    val values = ConcurrentHashMap<String, Double>()

    fun get(attributeName: String): Double {
        return values.getOrDefault(attributeName, 0.0)
    }

    fun set(attributeName: String, value: Double) {
        values[attributeName] = value
    }

    fun add(attributeName: String, value: Double) {
        values.merge(attributeName, value) { old, new -> old + new }
    }

    // 清空当前数据，用于重算
    fun clear() {
        values.clear()
    }
}