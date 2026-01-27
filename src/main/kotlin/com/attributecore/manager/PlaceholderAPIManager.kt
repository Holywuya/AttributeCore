package com.attributecore.papi

import com.attributecore.manager.AttributeManager
import com.attributecore.manager.ShieldManager
import org.bukkit.entity.Player
import taboolib.platform.compat.PlaceholderExpansion

/**
 * AttributeCore 变量
 */

object AttributeExpansion : PlaceholderExpansion {

    override val identifier: String = "ac"

    override fun onPlaceholderRequest(player: Player?, args: String): String {
        if (player == null) return "0"
        val uuid = player.uniqueId

        // 1. 识别 _int 后缀用于强制取整
        val isInt = args.endsWith("_int")
        val key = if (isInt) args.removeSuffix("_int") else args

        // 2. 核心逻辑匹配
        return when {
            // --- [ 战斗力 (Combat Power) ] ---
            key.equals("cp", true) || key.equals("combat_power", true) -> {
                formatDouble(AttributeManager.getCombatPower(player), isInt)
            }

            // --- [ 护盾系统 (Shield) ] ---
            key.equals("shield", true) -> {
                formatDouble(ShieldManager.getCurrentShield(uuid), isInt)
            }
            key.equals("shield_max", true) -> {
                formatDouble(ShieldManager.getMaxShield(uuid), isInt)
            }
            key.equals("shield_percent", true) -> {
                val current = ShieldManager.getCurrentShield(uuid)
                val max = ShieldManager.getMaxShield(uuid)
                val percent = if (max <= 0) 0.0 else (current / max * 100)
                formatDouble(percent, isInt)
            }

            // --- [ 基础属性获取 ] ---
            // 格式: %ac_min_<属性ID>% (获取最小值)
            key.startsWith("min_", true) -> {
                val attrId = key.substring(4)
                formatDouble(AttributeManager.getData(player).get(attrId)[0], isInt)
            }
            // 格式: %ac_max_<属性ID>% (获取最大值)
            key.startsWith("max_", true) -> {
                val attrId = key.substring(4)
                formatDouble(AttributeManager.getData(player).get(attrId)[1], isInt)
            }
            // 格式: %ac_<属性ID>% (自动显示 100 或 100-200)
            else -> {
                val vals = AttributeManager.getData(player).get(key)
                if (vals[0] == vals[1]) {
                    formatDouble(vals[0], isInt)
                } else {
                    "${formatDouble(vals[0], isInt)}-${formatDouble(vals[1], isInt)}"
                }
            }
        }
    }

    /**
     * 格式化数值输出
     */
    private fun formatDouble(value: Double, toInt: Boolean): String {
        if (toInt) return value.toInt().toString()
        return if (value % 1 == 0.0) {
            value.toInt().toString()
        } else {
            // 保留一位小数
            String.format("%.1f", value)
        }
    }
}