package com.attributecore.manager

import com.attributecore.data.AttributeData
import com.attributecore.data.attribute.BaseAttribute
import com.attributecore.event.AttributeLoader
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import taboolib.common.platform.function.info
import taboolib.common.platform.function.submit
import taboolib.common5.LoreMap
import java.util.UUID

object AttributeManager {

    private val attributes = mutableListOf<BaseAttribute>()
    val loreMap = LoreMap<BaseAttribute>(true, true, true)
    private val entityDataCache = mutableMapOf<UUID, AttributeData>()

    fun init() {
        // ✅ 自动加载：从 attributes 文件夹扫描并注册所有属性
        val loadedAttributes = AttributeLoader.loadAttributesFromFolder()
        attributes.addAll(loadedAttributes)

        // 初始化 Lore 映射
        refreshLoreMap()

        // 启动更新任务
        startUpdateTask()

        info("§a[AttributeManager] 已注册 ${attributes.size} 个属性")
    }

    /**
     * 刷新 Lore 映射
     * 参考 AttributeSystem 的注册机制[13]
     */
    private fun refreshLoreMap() {
        loreMap.clear()
        attributes.forEach { attr ->
            attr.names.forEach { name ->
                loreMap.put(name,attr)
            }
        }
    }

    /**
     * 重新加载所有属性
     * 参考 SXAttribute 的重载机制[7]
     */
    fun reloadAttributes() {
        info("§e[AttributeManager] §f正在重新加载属性...")

        // 清空现有属性
        attributes.clear()
        entityDataCache.clear()

        // 重新加载
        val loadedAttributes = AttributeLoader.loadAttributesFromFolder()
        attributes.addAll(loadedAttributes)


        // 重新初始化映射
        refreshLoreMap()

        info("§a[AttributeManager] 属性重载完成！共加载 ${attributes.size} 个属性")
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

    fun update(entity: LivingEntity) {
        val data = getData(entity)
        data.reset()

        val items = entity.equipment?.let {
            listOfNotNull(
                it.helmet,
                it.chestplate,
                it.leggings,
                it.boots,
                it.itemInMainHand,
                it.itemInOffHand
            )
        } ?: return

        items.forEach { item ->
            item.itemMeta?.lore?.forEach { loreLine ->
                val matched = loreMap[loreLine]
                if (matched != null) {
                    val value = extractValue(loreLine)
                    if (value > 0) {
                        data.setValueRange(matched.key, value, value)
                    }
                }
            }
        }
    }

    fun extractValue(lore: String): Double {
        val regex = "\\d+(\\.\\d+)?".toRegex()
        val match = regex.find(lore)
        return match?.value?.toDoubleOrNull() ?: 0.0
    }

    fun clearCache() {
        entityDataCache.clear()
        info("§a[AttributeManager] 实体属性数据缓存已清空")
    }

    private fun startUpdateTask() {
        submit(period = 20 * 5) {
            Bukkit.getOnlinePlayers().forEach { player ->
                update(player)
            }
        }
    }
}
