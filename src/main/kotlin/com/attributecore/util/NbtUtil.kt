package com.attributecore.util

import org.bukkit.inventory.ItemStack
import taboolib.common5.Coerce
import taboolib.module.nms.ItemTag
import taboolib.module.nms.ItemTagData
import taboolib.module.nms.getItemTag
import taboolib.platform.util.isAir

/**
 * NBT 工具扩展 - 简化 TabooLib NBT 操作
 */

// --- 私有辅助：安全获取深度数据 ---
private fun ItemStack?.getItemNBTData(path: String): ItemTagData? {
    if (this == null || this.isAir()) return null
    return this.getItemTag().getDeep(path)
}

// ==========================================
//          ItemStack 读取扩展
// ==========================================

fun ItemStack?.getDeepDouble(path: String, default: Double = 0.0) =
    getItemNBTData(path)?.asDouble() ?: default

fun ItemStack?.getDeepInt(path: String, default: Int = 0) =
    getItemNBTData(path)?.asInt() ?: default

fun ItemStack?.getDeepString(path: String, default: String = "") =
    getItemNBTData(path)?.asString() ?: default

fun ItemStack?.getDeepLong(path: String, default: Long = 0L) =
    getItemNBTData(path)?.asLong() ?: default

/**
 * ✅ 新增：获取深度范围值
 * 支持 NBT 存储为数字 (10) 或 字符串 ("10-20")
 */
fun ItemStack?.getDeepRange(path: String): DoubleArray {
    val data = getItemNBTData(path) ?: return doubleArrayOf(0.0, 0.0)
    val str = data.asString()

    // 如果包含分隔符，解析为范围
    if (str.contains("-")) {
        val split = str.split("-")
        val v1 = Coerce.toDouble(split[0].trim())
        val v2 = if (split.size > 1) Coerce.toDouble(split[1].trim()) else v1
        return doubleArrayOf(Math.min(v1, v2), Math.max(v1, v2))
    }

    // 否则作为单值处理
    val v = Coerce.toDouble(str)
    return doubleArrayOf(v, v)
}

// ==========================================
//          ItemStack 写入扩展 (自动保存)
// ==========================================

fun ItemStack.setDeep(path: String, value: Any?) {
    if (this.isAir()) return
    val tag = this.getItemTag()
    if (value == null) {
        tag.removeDeep(path)
    } else {
        tag.putDeep(path, value)
    }
    tag.saveTo(this)
}

fun ItemStack.removeDeep(path: String) {
    if (this.isAir()) return
    val tag = this.getItemTag()
    tag.removeDeep(path)
    tag.saveTo(this)
}

fun ItemStack.addDeep(path: String, value: Double) {
    if (this.isAir()) return
    val tag = this.getItemTag()
    val current = tag.getDeep(path)?.asDouble() ?: 0.0
    tag.putDeep(path, current + value)
    tag.saveTo(this)
}

// ==========================================
//          校验扩展
// ==========================================

fun ItemStack?.hasCustomTag(path: String): Boolean {
    val data = getItemNBTData(path) ?: return false
    val str = data.asString()
    return str.isNotEmpty() && str != "none"
}

fun ItemStack?.hasTagValue(path: String, value: String): Boolean {
    return getItemNBTData(path)?.asString() == value
}

// ==========================================
//          ItemTag 深度读取扩展
// ==========================================

fun ItemTag.getDeepDouble(path: String, default: Double = 0.0) =
    this.getDeep(path)?.asDouble() ?: default

fun ItemTag.getDeepInt(path: String, default: Int = 0) =
    this.getDeep(path)?.asInt() ?: default

fun ItemTag.getDeepString(path: String, default: String = "") =
    this.getDeep(path)?.asString() ?: default

/**
 * ✅ 新增：ItemTag 范围读取
 */
fun ItemTag.getDeepRange(path: String): DoubleArray {
    val data = this.getDeep(path) ?: return doubleArrayOf(0.0, 0.0)
    val str = data.asString()
    if (str.contains("-")) {
        val split = str.split("-")
        val v1 = Coerce.toDouble(split[0].trim())
        val v2 = if (split.size > 1) Coerce.toDouble(split[1].trim()) else v1
        return doubleArrayOf(Math.min(v1, v2), Math.max(v1, v2))
    }
    val v = Coerce.toDouble(str)
    return doubleArrayOf(v, v)
}

fun ItemTag.addDeep(path: String, value: Double) {
    val current = this.getDeep(path)?.asDouble() ?: 0.0
    this.putDeep(path, current + value)
}