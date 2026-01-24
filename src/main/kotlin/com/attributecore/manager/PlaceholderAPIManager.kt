package com.attributecore.manager

import org.bukkit.entity.Player
import taboolib.platform.compat.PlaceholderExpansion

/**
 * PlaceholderAPI 变量扩展
 * 变量格式: %ac_<attribute_key>%
 * 示例: %ac_attack_damage% -> 显示 "100" 或 "100-150"
 */
object AttributeExpansion : PlaceholderExpansion {

    override val identifier: String = "ac"

    override fun onPlaceholderRequest(player: Player?, args: String): String {
        if (player == null) return "0"

        // 获取玩家缓存数据
        val data = AttributeManager.getData(player)

        // args 即为属性 ID (key)
        val values = data.get(args)

        // 格式化输出
        // 如果 min == max，只显示一个数字
        if (values[0] == values[1]) {
            return formatDouble(values[0])
        }

        // 否则显示范围
        return "${formatDouble(values[0])}-${formatDouble(values[1])}"
    }

    // 去除多余的小数点 (如 10.0 -> 10)
    private fun formatDouble(value: Double): String {
        return if (value % 1 == 0.0) {
            value.toInt().toString()
        } else {
            String.format("%.1f", value)
        }
    }
}