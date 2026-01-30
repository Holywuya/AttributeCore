package com.attributecore.util

import com.attributecore.AttributeCore
import taboolib.common.platform.function.info

object DebugLogger {
    private val config get() = AttributeCore.config

    fun logAttributeLoading(message: String) {
        if (config.getBoolean("debug.log-attribute-loading", false)) {
            info("[属性加载] $message")
        }
    }

    fun logDamageCalculation(message: String) {
        if (config.getBoolean("debug.log-damage-calculation", false)) {
            info("[伤害计算] $message")
        }
    }

    fun logCombatPower(message: String) {
        if (config.getBoolean("debug.log-combat-power", false)) {
            info("[战斗力] $message")
        }
    }

    fun logRegexMatch(message: String) {
        if (config.getBoolean("debug.log-attribute-loading", false)) {
            info("[正则匹配] $message")
        }
    }

    fun logEquipmentUpdate(message: String) {
        if (config.getBoolean("debug.log-attribute-loading", false)) {
            info("[装备更新] $message")
        }
    }

    fun isAttributeLoadingEnabled(): Boolean {
        return config.getBoolean("debug.log-attribute-loading", false)
    }

    fun isDamageCalculationEnabled(): Boolean {
        return config.getBoolean("debug.log-damage-calculation", false)
    }

    fun isCombatPowerEnabled(): Boolean {
        return config.getBoolean("debug.log-combat-power", false)
    }
}
