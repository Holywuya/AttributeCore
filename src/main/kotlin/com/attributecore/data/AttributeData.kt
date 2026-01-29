package com.attributecore.data

import org.bukkit.entity.LivingEntity

class AttributeData(val entity: LivingEntity) {

    /** 属性值存储：key -> [最小值, 最大值] */
    val values = mutableMapOf<String, DoubleArray>()

    /** 属性优先级缓存（用于计算顺序） */
    private val priorityCache = mutableMapOf<String, Int>()

    /** 属性标签缓存（用于条件应用） */
    private val tagCache = mutableMapOf<String, Set<String>>()

    /** 最后更新时间 */
    var lastUpdateTime = 0L

    fun setValue(key: String, value: Double) {
        values[key] = doubleArrayOf(value, value)
    }

    fun setValueRange(key: String, min: Double, max: Double) {
        values[key] = doubleArrayOf(min, max)
    }

    fun get(key: String): DoubleArray {
        return values[key] ?: doubleArrayOf(0.0, 0.0)
    }

    fun getBaseValue(key: String): Double {
        return get(key)[0]
    }

    fun getMaxValue(key: String): Double {
        return get(key)[1]
    }

    fun getAverageValue(key: String): Double {
        val arr = get(key)
        return (arr[0] + arr[1]) / 2.0
    }

    /** 增加某属性的值（在原有基础上加） */
    fun addValue(key: String, amount: Double) {
        val current = get(key)
        setValueRange(key, current[0] + amount, current[1] + amount)
    }

    /** 增加某属性的范围 */
    fun addValueRange(key: String, minDelta: Double, maxDelta: Double) {
        val current = get(key)
        setValueRange(key, current[0] + minDelta, current[1] + maxDelta)
    }

    /** 乘以百分比倍率 */
    fun multiplyValue(key: String, multiplier: Double) {
        val current = get(key)
        setValueRange(key, current[0] * multiplier, current[1] * multiplier)
    }

    /** 检查属性是否存在非零值 */
    fun hasAttribute(key: String): Boolean {
        val arr = get(key)
        return arr[0] != 0.0 || arr[1] != 0.0
    }

    /** 获取所有非零属性的键 */
    fun getNonZeroAttributes(): Set<String> {
        return values.filterValues { arr -> arr[0] != 0.0 || arr[1] != 0.0 }.keys
    }

    /** 设置属性优先级缓存 */
    fun setPriority(key: String, priority: Int) {
        priorityCache[key] = priority
    }

    /** 获取属性优先级 */
    fun getPriority(key: String): Int {
        return priorityCache[key] ?: 0
    }

    /** 设置属性标签缓存 */
    fun setTags(key: String, tags: Set<String>) {
        tagCache[key] = tags
    }

    /** 获取属性标签 */
    fun getTags(key: String): Set<String> {
        return tagCache[key] ?: emptySet()
    }

    /** 重置所有属性值 */
    fun reset() {
        values.clear()
        lastUpdateTime = System.currentTimeMillis()
    }

    /** 清空所有缓存（用于重载） */
    fun clearCache() {
        priorityCache.clear()
        tagCache.clear()
    }

    /** 获取属性统计信息 */
    fun getStats(): Map<String, Any> {
        val totalAttrs = values.size
        val nonZero = getNonZeroAttributes().size
        val avgMin = values.values.map { it[0] }.average()
        val avgMax = values.values.map { it[1] }.average()
        
        return mapOf(
            "total_attributes" to totalAttrs,
            "non_zero_attributes" to nonZero,
            "average_min" to avgMin,
            "average_max" to avgMax,
            "last_update" to lastUpdateTime
        )
    }
}
