package com.attributecore.manager

import com.attributecore.data.AttributeData
import com.attributecore.data.AttributeType
import com.attributecore.data.attribute.BaseAttribute
import com.attributecore.event.AttributeLoader
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import taboolib.common.platform.function.info
import taboolib.common.platform.function.submit
import taboolib.common5.LoreMap
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import com.attributecore.util.* // 你的工具类
import taboolib.module.nms.getItemTag
import taboolib.platform.util.isAir

object AttributeManager {

    private val attributes = mutableListOf<BaseAttribute>()
    val loreMap = LoreMap<BaseAttribute>(true, true, true)
    private val entityDataCache = mutableMapOf<UUID, AttributeData>()

    // API 基础属性缓存
    private val apiAttributeCache = ConcurrentHashMap<UUID, MutableMap<String, Double>>()

    private const val NBT_ROOT = "AttributeCore"

    fun init() {
        val loadedAttributes = AttributeLoader.loadAttributesFromFolder()
        attributes.addAll(loadedAttributes)
        refreshLoreMap()
        startUpdateTask()
        info("§a[AttributeManager] 已注册 ${attributes.size} 个属性")
    }

    private fun refreshLoreMap() {
        loreMap.clear()
        attributes.forEach { attr ->
            attr.names.forEach { name -> loreMap.put(name, attr) }
        }
    }

    fun reloadAttributes() {
        info("§e[AttributeManager] §f正在重新加载属性...")
        attributes.clear()
        entityDataCache.clear()
        // apiAttributeCache.clear() // 可选：是否清除 API 属性

        val loadedAttributes = AttributeLoader.loadAttributesFromFolder()
        attributes.addAll(loadedAttributes)
        refreshLoreMap()

        info("§a[AttributeManager] 属性重载完成！共加载 ${attributes.size} 个属性")
    }

    fun setApiAttribute(uuid: UUID, key: String, value: Double) {
        val map = apiAttributeCache.getOrPut(uuid) { mutableMapOf() }
        val current = map.getOrDefault(key, 0.0)
        map[key] = current + value
    }

    fun removeData(uuid: UUID) {
        entityDataCache.remove(uuid)
        apiAttributeCache.remove(uuid)
    }

    fun registerAttribute(attribute: BaseAttribute) {
        attributes.add(attribute)
    }

    fun getAttributes(): List<BaseAttribute> {
        return attributes.sortedBy { it.priority }
    }

    fun getData(entity: LivingEntity): AttributeData {
        return entityDataCache.getOrPut(entity.uniqueId) { AttributeData(entity) }
    }

    /**
     * 更新实体属性 (核心逻辑重构)
     */
    fun update(entity: LivingEntity) {
        val data = getData(entity)
        data.reset()

        // 1. 载入 API 基础属性 (视为固定值，min=max)
        val apiData = apiAttributeCache[entity.uniqueId]
        if (apiData != null) {
            apiData.forEach { (key, value) ->
                data.setValueRange(key, value, value)
            }
        }

        val items = entity.equipment?.let {
            listOfNotNull(
                it.helmet, it.chestplate, it.leggings, it.boots,
                it.itemInMainHand, it.itemInOffHand
            )
        } ?: return

        items.forEach { item ->
            // 检测空气
            if (item.isAir()) return@forEach

            // ✅ Feature 2: 检查装备使用条件 (等级/权限)
            if (!ConditionChecker.check(entity, item)) return@forEach

            // 缓存 Tag 和 Lore，避免重复调用
            val tag = item.getItemTag()
            val itemLore = item.itemMeta?.lore ?: emptyList<String>()

            attributes.forEach { attr ->
                // --- A. NBT 读取 (优先) ---
                // NBT 通常存储单一确定的数值 (即 min=max)
                var minVal = tag.getDeepDouble("$NBT_ROOT.${attr.key}", 0.0)
                var maxVal = minVal

                // --- B. Lore 扫描 (次之) ---
                if (minVal == 0.0 && itemLore.isNotEmpty()) {
                    for (loreLine in itemLore) {
                        // 使用 contains 模糊匹配属性名
                        if (attr.names.any { loreLine.contains(it) }) {
                            // ✅ Feature 1: 解析范围值 "10-20"
                            val range = extractValueRange(loreLine)
                            if (range[0] != 0.0 || range[1] != 0.0) {
                                minVal = range[0]
                                maxVal = range[1]
                                break // 找到即停止当前属性的扫描
                            }
                        }
                    }
                }

                // --- C. 累加逻辑 ---
                if (minVal != 0.0 || maxVal != 0.0) {
                    val current = data.get(attr.key) // 获取当前 [minTotal, maxTotal]
                    // 最小值加最小值，最大值加最大值
                    data.setValueRange(
                        attr.key,
                        current[0] + minVal,
                        current[1] + maxVal
                    )
                }
            }
        }

        // ✅ Feature 3: 处理 UPDATE 类型的属性回调 (如最大生命值、移动速度)
        // 这些属性需要在计算完总值后立即应用到实体上
        attributes.forEach { attr ->
            if (attr.type == AttributeType.UPDATE) {
                val vals = data.get(attr.key)
                // UPDATE 类型通常只看基础值(min)，或者是固定值
                attr.onUpdate(entity, vals[0])
            }
        }
    }

    /**
     * ✅ Feature 1: 解析 Lore 范围数值
     * 支持格式: "攻击力: 10" 或 "攻击力: 10-20"
     * @return DoubleArray(min, max)
     */
    fun extractValueRange(lore: String): DoubleArray {
        // 1. 尝试匹配 "数字 - 数字" 格式
        val rangeRegex = "(\\d+(\\.\\d+)?)\\s*-\\s*(\\d+(\\.\\d+)?)".toRegex()
        val rangeMatch = rangeRegex.find(lore)

        if (rangeMatch != null) {
            val v1 = rangeMatch.groupValues[1].toDoubleOrNull() ?: 0.0
            val v2 = rangeMatch.groupValues[3].toDoubleOrNull() ?: 0.0
            // 自动修正大小顺序，防止配置写反
            return doubleArrayOf(Math.min(v1, v2), Math.max(v1, v2))
        }

        // 2. 尝试匹配单个数字
        val singleRegex = "\\d+(\\.\\d+)?".toRegex()
        val singleMatch = singleRegex.find(lore)
        val valSingle = singleMatch?.value?.toDoubleOrNull() ?: 0.0

        return doubleArrayOf(valSingle, valSingle)
    }


    fun clearCache() {
        entityDataCache.clear()
        apiAttributeCache.clear()
        info("§a[AttributeManager] 实体属性数据缓存已清空")
    }

    private fun startUpdateTask() {
        submit(period = 20 * 5) {
            Bukkit.getOnlinePlayers().forEach { player ->
                update(player)
            }
        }
    }

    fun getAttributeKeys(): List<String> {
        return attributes.map { it.key }
    }
}