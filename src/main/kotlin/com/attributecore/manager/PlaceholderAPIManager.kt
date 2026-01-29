package com.attributecore.papi

import com.attributecore.manager.AttributeManager
import com.attributecore.manager.ShieldManager
import org.bukkit.entity.Player
import taboolib.platform.compat.PlaceholderExpansion

/**
 * AttributeCore PlaceholderAPI扩展
 * 
 * 支持以下占位符格式：
 * - %ac_cp% / %ac_combat_power% - 战斗力
 * - %ac_cp_int% - 战斗力（取整）
 * - %ac_shield% - 当前护盾
 * - %ac_shield_max% - 最大护盾
 * - %ac_shield_percent% - 护盾百分比
 * - %ac_<属性ID>% - 属性值（自动判断范围）
 * - %ac_min_<属性ID>% - 属性最小值
 * - %ac_max_<属性ID>% - 属性最大值
 * - %ac_<属性ID>_int% - 属性值（取整）
 * - %ac_level% - 玩家等级
 * - %ac_health% - 当前生命值
 * - %ac_health_max% - 最大生命值
 * - %ac_health_percent% - 生命百分比
 */
object AttributeExpansion : PlaceholderExpansion {

    override val identifier: String = "ac"

    override fun onPlaceholderRequest(player: Player?, args: String): String {
        if (player == null) return "0"
        
        try {
            return handlePlaceholder(player, args)
        } catch (e: Exception) {
            return "ERROR"
        }
    }

    private fun handlePlaceholder(player: Player, args: String): String {
        val uuid = player.uniqueId

        // 1. 识别 _int 后缀用于强制取整
        val isInt = args.endsWith("_int")
        val key = if (isInt) args.removeSuffix("_int") else args

        // 2. 战斗力 (Combat Power)
        if (key.equals("cp", true) || key.equals("combat_power", true)) {
            return formatDouble(AttributeManager.getCombatPower(player), isInt)
        }

        // 3. 护盾系统 (Shield)
        when {
            key.equals("shield", true) -> {
                return formatDouble(ShieldManager.getCurrentShield(uuid), isInt)
            }
            key.equals("shield_max", true) -> {
                return formatDouble(ShieldManager.getMaxShield(uuid), isInt)
            }
            key.equals("shield_percent", true) -> {
                val current = ShieldManager.getCurrentShield(uuid)
                val max = ShieldManager.getMaxShield(uuid)
                val percent = if (max <= 0) 0.0 else (current / max * 100)
                return formatDouble(percent, isInt)
            }
        }

        // 4. 生命值系统 (Health)
        when {
            key.equals("health", true) -> {
                return formatDouble(player.health, isInt)
            }
            key.equals("health_max", true) -> {
                @Suppress("DEPRECATION")
                return formatDouble(player.maxHealth, isInt)
            }
            key.equals("health_percent", true) -> {
                @Suppress("DEPRECATION")
                val percent = (player.health / player.maxHealth * 100)
                return formatDouble(percent, isInt)
            }
        }

        // 5. 玩家等级 (Level)
        if (key.equals("level", true)) {
            return player.level.toString()
        }

        // 6. 属性值查询
        // 格式: %ac_min_<属性ID>%
        if (key.startsWith("min_", true)) {
            val attrId = key.substring(4)
            return formatDouble(AttributeManager.getData(player).get(attrId)[0], isInt)
        }

        // 格式: %ac_max_<属性ID>%
        if (key.startsWith("max_", true)) {
            val attrId = key.substring(4)
            return formatDouble(AttributeManager.getData(player).get(attrId)[1], isInt)
        }

        // 7. 属性值 (自动显示范围或单值)
        // 格式: %ac_<属性ID>%
        val vals = AttributeManager.getData(player).get(key)
        return if (vals[0] == vals[1]) {
            formatDouble(vals[0], isInt)
        } else {
            "${formatDouble(vals[0], isInt)}-${formatDouble(vals[1], isInt)}"
        }
    }

    /**
     * 格式化数值输出
     * @param value 数值
     * @param toInt 是否取整
     */
    private fun formatDouble(value: Double, toInt: Boolean): String {
        if (toInt) return value.toInt().toString()
        return if (value % 1 == 0.0) {
            value.toInt().toString()
        } else {
            String.format("%.1f", value)
        }
    }
}