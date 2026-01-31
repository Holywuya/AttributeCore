package com.attributecore.hook

import com.attributecore.data.SubAttribute
import com.attributecore.manager.AttributeManager
import org.bukkit.entity.Player
import taboolib.common.platform.function.info
import taboolib.platform.compat.PlaceholderExpansion

/**
 * PlaceholderAPI 集成
 * 自动注册所有属性的占位符，无需在 JS 中手动定义
 * 
 * 占位符格式:
 * - %attributecore_<属性名>% - 获取属性固定值
 * - %attributecore_<属性名>_percent% - 获取属性百分比加成
 * - %attributecore_<属性名>_final% - 获取属性最终值 (固定值 * (1 + 百分比/100))
 * - %attributecore_combat_power% - 获取战斗力
 * - %attributecore_list% - 列出所有非零属性
 * 
 * 示例:
 * - %attributecore_attack_damage% -> 100.0
 * - %attributecore_life_steal% -> 10.0
 * - %attributecore_defense_percent% -> 20.0
 * - %attributecore_combat_power% -> 1500.0
 */
object PlaceholderHook : PlaceholderExpansion {

    override val identifier: String = "attributecore"

    override val autoReload: Boolean = true

    override fun onPlaceholderRequest(player: Player?, args: String): String {
        if (player == null) return ""

        val data = AttributeManager.getEntityData(player)

        return when (args.lowercase()) {
            "combat_power", "cp" -> String.format("%.2f", data.calculateCombatPower())
            "list" -> data.getNonZeroAttributes().entries.joinToString(", ") { "${it.key}: ${it.value}" }
            else -> parseAttributePlaceholder(args, data)
        }
    }

    private fun parseAttributePlaceholder(args: String, data: com.attributecore.data.AttributeData): String {
        val basePlaceholder = when {
            args.endsWith("_percent") -> args.removeSuffix("_percent")
            args.endsWith("_final") -> args.removeSuffix("_final")
            else -> args
        }
        
        val attr = findAttributeByPlaceholder(basePlaceholder)
        val attrName = attr?.name ?: basePlaceholder
        
        return when {
            args.endsWith("_percent") -> {
                String.format("%.2f", data.getPercent(attrName))
            }
            args.endsWith("_final") -> {
                String.format("%.2f", data.getFinal(attrName))
            }
            else -> {
                String.format("%.2f", data[attrName])
            }
        }
    }

    private fun findAttributeByPlaceholder(placeholder: String): SubAttribute? {
        return SubAttribute.getAttributes().find { attr ->
            attr.placeholder.equals(placeholder, ignoreCase = true) ||
            attr.name.equals(placeholder, ignoreCase = true)
        }
    }
}
