package com.attributecore.data

/**
 * 元素附着记录
 */
data class ElementAura(
    val type: String,      // 元素类型: FIRE, WATER, ICE...
    var startTime: Long,   // 附着开始时间
    var duration: Long     // 持续时间 (毫秒)
) {
    fun isExpired() = System.currentTimeMillis() > startTime + duration
}