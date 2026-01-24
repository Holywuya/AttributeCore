package com.attributecore.util

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * 装备使用条件检查器
 * 检查 职业、等级、权限 等
 */
object ConditionChecker {

    /**
     * 检查实体是否满足物品的使用条件
     * @return true = 满足条件/可以使用; false = 不满足
     */
    fun check(entity: LivingEntity, item: ItemStack): Boolean {
        // 非玩家实体（如怪物）默认忽略条件，直接生效
        if (entity !is Player) return true

        if (entity.isOp) return true

        val lore = item.itemMeta?.lore ?: return true

        lore.forEach { line ->
            // 1. 等级限制 (格式: "等级限制: 10" 或 "Level: 10")
            if (line.contains("等级限制") || line.contains("Level")) {
                val levelStr = line.replace("\\D".toRegex(), "") // 移除非数字字符
                val level = levelStr.toIntOrNull() ?: 0
                if (entity.level < level) {
                    return false
                }
            }

            // 2. 权限限制 (格式: "需要权限: vip.use" 或 "Perm: vip.use")
            if (line.contains("需要权限") || line.contains("Perm")) {
                val split = line.split(":")
                if (split.size > 1) {
                    val perm = split[1].trim()
                    if (perm.isNotEmpty() && !entity.hasPermission(perm)) {
                        return false
                    }
                }
            }

            // 3. 职业限制 (需要对接你的职业插件，此处为示例)
            // if (line.contains("职业需求")) ...
        }

        return true
    }
}