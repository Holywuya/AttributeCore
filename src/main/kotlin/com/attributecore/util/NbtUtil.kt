package com.attributecore.util

import org.bukkit.inventory.ItemStack
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

// ==========================================
//          ItemStack 写入扩展 (自动保存)
// ==========================================

/**
 * 设置深度 NBT 值
 * @param value 如果为 null，则执行删除操作
 */
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

/**
 * 深度删除 NBT 节点
 */
fun ItemStack.removeDeep(path: String) {
    if (this.isAir()) return
    val tag = this.getItemTag()
    tag.removeDeep(path)
    tag.saveTo(this)
}

/**
 * 深度数值累加
 */
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

/**
 * 校验是否存在有效的自定义标签节点
 */
fun ItemStack?.hasCustomTag(path: String): Boolean {
    val data = getItemNBTData(path) ?: return false
    val str = data.asString()
    return str.isNotEmpty() && str != "none"
}

/**
 * 校验深度标签值是否等于指定字符串
 */
fun ItemStack?.hasTagValue(path: String, value: String): Boolean {
    return getItemNBTData(path)?.asString() == value
}

// ==========================================
//          ItemTag 深度读取扩展 (直接操作 Tag 对象)
// ==========================================

fun ItemTag.getDeepDouble(path: String, default: Double = 0.0) =
    this.getDeep(path)?.asDouble() ?: default

fun ItemTag.getDeepInt(path: String, default: Int = 0) =
    this.getDeep(path)?.asInt() ?: default

fun ItemTag.getDeepString(path: String, default: String = "") =
    this.getDeep(path)?.asString() ?: default

/**
 * ItemTag 累加扩展
 */
fun ItemTag.addDeep(path: String, value: Double) {
    val current = this.getDeep(path)?.asDouble() ?: 0.0
    this.putDeep(path, current + value)
}