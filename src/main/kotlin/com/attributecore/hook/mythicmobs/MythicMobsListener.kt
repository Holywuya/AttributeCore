package com.attributecore.hook.mythicmobs

import com.attributecore.data.AttributeData
import com.attributecore.manager.AttributeManager
import com.attributecore.util.DebugLogger
import ink.ptms.um.Mythic
import ink.ptms.um.Mob
import ink.ptms.um.event.MobSpawnEvent
import org.bukkit.entity.LivingEntity
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.info
import java.util.concurrent.ConcurrentHashMap

object MythicMobsListener {
    
    private val attributeCache = ConcurrentHashMap<String, Map<String, Double>>()
    private var registered = false
    
    fun register() {
        if (registered) return
        registered = true
        info("MythicMobs 监听器已注册 (via UM)")
    }
    
    fun unregister() {
        attributeCache.clear()
        registered = false
    }
    
    fun clearCache() {
        attributeCache.clear()
        info("MythicMobs 属性缓存已清除")
    }
    
    fun refreshEntityAttributes(entity: LivingEntity) {
        val mob = Mythic.API.getMob(entity) ?: return
        applyMobAttributes(entity, mob)
    }
    
    @SubscribeEvent
    fun onMythicMobSpawn(event: MobSpawnEvent) {
        val mob = event.mob ?: return
        val entity = mob.entity as? LivingEntity ?: return
        
        DebugLogger.logDamageCalculation("[MythicMobs] MythicMob 生成: ${mob.id}, 等级: ${mob.level}")
        applyMobAttributes(entity, mob)
    }
    
    private fun applyMobAttributes(entity: LivingEntity, mob: Mob) {
        val mobType = mob.id
        val mobLevel = mob.level
        val cacheKey = "$mobType:${mobLevel.toInt()}"
        
        val attributes = attributeCache.getOrPut(cacheKey) {
            parseMobAttributes(mob)
        }
        
        if (attributes.isEmpty()) {
            return
        }
        
        val entityData = AttributeManager.getEntityData(entity)
        applyAttributesToData(entityData, attributes, mobLevel)
        
        DebugLogger.logDamageCalculation("[MythicMobs] 已为 $mobType 应用 ${attributes.size} 个属性")
    }
    
    private fun parseMobAttributes(mob: Mob): Map<String, Double> {
        val config = mob.config
        val result = mutableMapOf<String, Double>()
        
        val attrSection = config.getConfigurationSection("AttributeCore")
        if (attrSection == null) {
            val attrList = config.getStringList("AttributeCore")
            if (attrList.isNotEmpty()) {
                for (line in attrList) {
                    parseAttributeLine(line)?.let { (name, value) ->
                        result[name] = value
                    }
                }
            }
            return result
        }
        
        for (key in attrSection.getKeys(false)) {
            if (key == "元素类型" || key == "ElementType") continue
            
            val rawValue = attrSection.getString(key) ?: continue
            parseAttributeValue(key, rawValue)?.let { (name, value) ->
                result[name] = value
            }
        }
        
        return result
    }
    
    private fun parseAttributeLine(line: String): Pair<String, Double>? {
        val parts = line.split(":", " ", limit = 2)
        if (parts.size < 2) return null
        
        val name = parts[0].trim()
        val rawValue = parts[1].trim()
        
        return parseAttributeValue(name, rawValue)
    }
    
    private fun parseAttributeValue(name: String, rawValue: String): Pair<String, Double>? {
        val isPercent = rawValue.endsWith("%")
        val numericValue = rawValue.replace("%", "").trim().toDoubleOrNull() ?: return null
        
        val attrName = if (isPercent) "${name}_percent" else name
        return attrName to numericValue
    }
    
    private fun applyAttributesToData(data: AttributeData, attributes: Map<String, Double>, mobLevel: Double) {
        for ((name, baseValue) in attributes) {
            val scaledValue = baseValue * (1 + (mobLevel - 1) * 0.1)
            
            if (name.endsWith("_percent")) {
                val actualName = name.removeSuffix("_percent")
                data.addPercent(actualName, scaledValue)
            } else {
                data.add(name, scaledValue)
            }
        }
    }
}
