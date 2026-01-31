package com.attributecore.hook.mythicmobs

import ink.ptms.um.Mythic
import org.bukkit.entity.LivingEntity
import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning

/**
 * MythicMobs 插件集成 (使用 TabooLib UM 兼容工具)
 * 
 * UM (Universal-Mythic) 自动处理 MM4 和 MM5 的 API 差异
 * 
 * 功能:
 * - 监听 MythicMobs 生成事件，为怪物应用属性
 * - 支持 MythicMobs 4.x 和 5.x (通过 UM 兼容层)
 * 
 * MythicMobs 配置格式:
 * ```yaml
 * ExampleMob:
 *   Type: ZOMBIE
 *   Display: '&c示例怪物'
 *   Health: 100
 *   Damage: 10
 *   Options:
 *     MovementSpeed: 0.2
 *   # AttributeCore 属性配置
 *   AttributeCore:
 *     攻击力: 50
 *     防御力: 30
 *     暴击率: 25%
 *     火元素伤害: 20
 *     元素类型: "FIRE"  # 武器元素类型
 * ```
 */
object MythicMobsHook {
    
    private var enabled = false
    
    fun setup() {
        if (!Mythic.isLoaded()) {
            info("MythicMobs 未检测到，跳过集成")
            return
        }
        
        try {
            val isLegacy = Mythic.API.isLegacy
            val version = if (isLegacy) "4.x (Legacy)" else "5.x+"
            
            MythicMobsListener.register()
            enabled = true
            
            info("MythicMobs $version 集成已启用 (via UM)")
        } catch (e: Exception) {
            warning("MythicMobs 集成初始化失败: ${e.message}")
            e.printStackTrace()
        }
    }
    
    fun shutdown() {
        MythicMobsListener.unregister()
        enabled = false
    }
    
    /**
     * 检查 MythicMobs 集成是否启用
     */
    fun isEnabled(): Boolean = enabled
    
    /**
     * 检查 MythicMobs 是否已加载
     */
    fun isLoaded(): Boolean = Mythic.isLoaded()
    
    /**
     * 检查是否为 Legacy 版本 (MM4)
     */
    fun isLegacy(): Boolean {
        if (!Mythic.isLoaded()) return false
        return Mythic.API.isLegacy
    }
    
    /**
     * 检查实体是否为 MythicMob
     */
    fun isMythicMob(entity: LivingEntity): Boolean {
        if (!enabled) return false
        return Mythic.API.getMob(entity) != null
    }
    
    /**
     * 获取 MythicMob 的内部名称
     */
    fun getMobType(entity: LivingEntity): String? {
        if (!enabled) return null
        return Mythic.API.getMob(entity)?.id
    }
    
    /**
     * 获取 MythicMob 的等级
     */
    fun getMobLevel(entity: LivingEntity): Double {
        if (!enabled) return 1.0
        return Mythic.API.getMob(entity)?.level ?: 1.0
    }
    
    /**
     * 获取 MythicMob 的显示名称
     */
    fun getMobDisplayName(entity: LivingEntity): String? {
        if (!enabled) return null
        return Mythic.API.getMob(entity)?.displayName
    }
    
    /**
     * 获取 MythicMob 的阵营
     */
    fun getMobFaction(entity: LivingEntity): String? {
        if (!enabled) return null
        return Mythic.API.getMob(entity)?.faction
    }
    
    /**
     * 获取 MythicMob 的姿态
     */
    fun getMobStance(entity: LivingEntity): String? {
        if (!enabled) return null
        return Mythic.API.getMob(entity)?.stance
    }
    
    fun refreshEntityAttributes(entity: LivingEntity) {
        if (!enabled) return
        MythicMobsListener.refreshEntityAttributes(entity)
    }
    
    fun clearCache() {
        MythicMobsListener.clearCache()
    }
    
    /**
     * 获取所有已注册的 MythicMob 类型 ID
     */
    fun getMobTypeIds(): List<String> {
        if (!Mythic.isLoaded()) return emptyList()
        return Mythic.API.getMobIDList()
    }
    
    /**
     * 检查 MythicMob 类型是否存在
     */
    fun hasMobType(mobId: String): Boolean {
        if (!Mythic.isLoaded()) return false
        return Mythic.API.getMobType(mobId) != null
    }
    
    /**
     * 获取 MythicItem ID (如果物品是 MM 物品)
     */
    fun getMythicItemId(item: org.bukkit.inventory.ItemStack): String? {
        if (!Mythic.isLoaded()) return null
        return Mythic.API.getItemId(item)
    }
    
    /**
     * 生成 MythicItem
     */
    fun getMythicItemStack(itemId: String, amount: Int = 1): org.bukkit.inventory.ItemStack? {
        if (!Mythic.isLoaded()) return null
        val item = Mythic.API.getItem(itemId) ?: return null
        return item.generateItemStack(amount)
    }
}
