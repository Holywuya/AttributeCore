package com.attributecore.manager

import com.attributecore.data.AttributeData
import com.attributecore.data.AttributeType
import com.attributecore.data.attribute.BaseAttribute
import com.attributecore.event.AttributeBehaviors
import com.attributecore.event.AttributeLoader
import com.attributecore.event.BehaviorLoader
import com.attributecore.util.* // ConditionChecker, NBTUtils
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import taboolib.common.platform.function.info
import taboolib.common.platform.function.submit
import taboolib.common5.LoreMap
import taboolib.module.nms.getItemTag
import taboolib.platform.util.isAir
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object AttributeManager {

    private val attributes = mutableListOf<BaseAttribute>()
    val loreMap = LoreMap<BaseAttribute>(true, true, true)
    private val entityDataCache = mutableMapOf<UUID, AttributeData>()

    // MythicMobs 或 API 赋予的基础属性缓存 (UUID -> {Key -> Value})
    private val apiAttributeCache = ConcurrentHashMap<UUID, MutableMap<String, Double>>()

    private const val NBT_ROOT = "AttributeCore"

    fun init() {
        // 1. 初始化行为系统
        AttributeBehaviors.init()
        BehaviorLoader.loadBehaviors()

        // 2. 加载属性配置
        reloadAttributesInternal()

        // 3. 初始化护盾系统
        ShieldManager.init()

        // 4. 启动定时任务
        startUpdateTask()
    }

    fun reloadAttributes() {
        info("§e[AttributeManager] §f正在重新加载...")
        BehaviorLoader.loadBehaviors()
        reloadAttributesInternal()
        ShieldManager.init() // 重启护盾任务
        info("§a[AttributeManager] 重载完成")
    }

    private fun reloadAttributesInternal() {
        attributes.clear()
        entityDataCache.clear()
        // 注意：通常不清理 apiAttributeCache 以保留 MM 怪属性

        attributes.addAll(AttributeLoader.loadAttributesFromFolder())

        loreMap.clear()
        attributes.forEach { attr -> attr.names.forEach { loreMap.put(it, attr) } }
    }

    // --- API 方法 ---

    fun setApiAttribute(uuid: UUID, key: String, value: Double) {
        val map = apiAttributeCache.getOrPut(uuid) { mutableMapOf() }
        val current = map.getOrDefault(key, 0.0)
        map[key] = current + value
    }

    fun removeData(uuid: UUID) {
        entityDataCache.remove(uuid)
        apiAttributeCache.remove(uuid)
        ShieldManager.removeData(uuid)
    }

    fun getAttributes() = attributes.sortedBy { it.priority }

    fun getData(entity: LivingEntity) = entityDataCache.getOrPut(entity.uniqueId) { AttributeData(entity) }

    // --- 核心更新逻辑 ---

    fun update(entity: LivingEntity) {
        val data = getData(entity)
        data.reset()

        // 1. 载入 API 基础属性 (视为固定值 min=max)
        apiAttributeCache[entity.uniqueId]?.forEach { (key, value) ->
            data.setValueRange(key, value, value)
        }

        // 2. 扫描装备
        val items = entity.equipment?.let {
            listOfNotNull(it.helmet, it.chestplate, it.leggings, it.boots, it.itemInMainHand, it.itemInOffHand)
        } ?: return

        items.forEach { item ->
            if (item.isAir()) return@forEach
            if (!ConditionChecker.check(entity, item)) return@forEach

            val tag = item.getItemTag()
            val itemLore = item.itemMeta?.lore ?: emptyList<String>()

            attributes.forEach { attr ->
                var minVal = tag.getDeepDouble("$NBT_ROOT.${attr.key}", 0.0)
                var maxVal = minVal

                if (minVal == 0.0 && itemLore.isNotEmpty()) {
                    for (line in itemLore) {
                        val matchedName = attr.names.firstOrNull { name ->
                            line.contains("$name:") || line.contains("$name：") || line.contains(name)
                        }

                        if (matchedName != null) {
                            val range = extractValueRange(line)
                            if (range[0] != 0.0 || range[1] != 0.0) {
                                minVal = range[0]
                                maxVal = range[1]
                                break
                            }
                        }
                    }
                }

                // C. 累加
                if (minVal != 0.0 || maxVal != 0.0) {
                    val current = data.get(attr.key)
                    data.setValueRange(attr.key, current[0] + minVal, current[1] + maxVal)
                }
            }
        }

        // 3. 应用 Update 类型属性 (如血量、护盾上限)
        attributes.forEach { attr ->
            if (attr.type == AttributeType.UPDATE) {
                val vals = data.get(attr.key)
                // Update 属性通常取基础值
                attr.onUpdate(entity, vals[0])
            }
        }
    }

    // 解析范围值 "10-20"
    fun extractValueRange(lore: String): DoubleArray {
        val rangeRegex = "(\\d+(\\.\\d+)?)\\s*-\\s*(\\d+(\\.\\d+)?)".toRegex()
        val rangeMatch = rangeRegex.find(lore)
        if (rangeMatch != null) {
            val v1 = rangeMatch.groupValues[1].toDoubleOrNull() ?: 0.0
            val v2 = rangeMatch.groupValues[3].toDoubleOrNull() ?: 0.0
            return doubleArrayOf(Math.min(v1, v2), Math.max(v1, v2))
        }
        val singleMatch = "\\d+(\\.\\d+)?".toRegex().find(lore)
        val v = singleMatch?.value?.toDoubleOrNull() ?: 0.0
        return doubleArrayOf(v, v)
    }

    private fun startUpdateTask() {
        submit(period = 100) { Bukkit.getOnlinePlayers().forEach { update(it) } }
    }
}