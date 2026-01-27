package com.attributecore.manager

import com.attributecore.event.CoreConfig
import com.attributecore.data.AttributeData
import com.attributecore.data.AttributeType
import com.attributecore.data.attribute.BaseAttribute
import com.attributecore.event.ReactionLoader
import com.attributecore.util.ConditionChecker
import com.attributecore.util.getDeepRange
import org.bukkit.Bukkit
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.info
import taboolib.common.platform.function.submit
import taboolib.common5.LoreMap
import taboolib.common5.Coerce
import taboolib.module.nms.getItemTag
import taboolib.platform.util.isAir
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object AttributeManager {

    private val attributes = mutableListOf<BaseAttribute>()
    val loreMap = LoreMap<BaseAttribute>(true, true, true)

    private val entityDataCache = mutableMapOf<UUID, AttributeData>()
    private val apiAttributeCache = ConcurrentHashMap<UUID, ConcurrentHashMap<String, MutableMap<String, Double>>>()
    private val itemAttributeCache = ConcurrentHashMap<Int, Map<String, DoubleArray>>()

    private const val NBT_ROOT = "AttributeCore"

    fun init() {
        // 1. 初始化脚本环境 (创建文件夹、释放示例)
        ScriptManager.init()

        // 2. 加载元素反应配置 (reactions.yml)
        ReactionLoader.load()

        // 3. 加载属性 (从 JS 脚本)
        reloadAttributesInternal()

        // 4. 初始化护盾
        ShieldManager.init()

        // 5. 启动任务
        startUpdateTask()
        startCleanupTask()

        info("§a[AttributeManager] 初始化完成，已注册 ${attributes.size} 个脚本属性")
    }

    fun reloadAttributes() {
        info("§e[AttributeManager] §f正在重新加载属性...")

        // 重载脚本管理器 (重新编译 JS)
        ScriptManager.reload()

        ReactionLoader.load()
        reloadAttributesInternal()
        ShieldManager.init()

        info("§a[AttributeManager] 属性重载完成")
    }

    private fun reloadAttributesInternal() {
        attributes.clear()
        entityDataCache.clear()
        itemAttributeCache.clear()


        attributes.addAll(ScriptManager.loadAttributes())

        // 重建 Lore 映射
        loreMap.clear()
        attributes.forEach { attr ->
            attr.names.forEach { name -> loreMap.put(name, attr) }
        }
    }

    // ================= [ 以下代码保持不变 ] =================

    fun setApiAttribute(uuid: UUID, source: String, key: String, value: Double) {
        val entityMap = apiAttributeCache.getOrPut(uuid) { ConcurrentHashMap() }
        val sourceMap = entityMap.getOrPut(source) { mutableMapOf() }
        sourceMap[key] = value
    }

    fun getApiData(uuid: UUID, source: String): Map<String, Double>? {
        return apiAttributeCache[uuid]?.get(source)
    }

    fun removeApiData(uuid: UUID, source: String) {
        apiAttributeCache[uuid]?.remove(source)
    }

    fun clearSourceData(source: String) {
        apiAttributeCache.values.forEach { it.remove(source) }
    }

    fun clearApiAttributes(uuid: UUID) {
        apiAttributeCache.remove(uuid)
    }

    fun removeData(uuid: UUID) {
        entityDataCache.remove(uuid)
        apiAttributeCache.remove(uuid)
        ShieldManager.removeData(uuid)
        ReactionManager.clear(uuid)
    }

    fun getAttributes() = attributes.sortedBy { it.priority }

    fun getData(entity: LivingEntity): AttributeData = entityDataCache.getOrPut(entity.uniqueId) { AttributeData(entity) }

    fun update(entity: LivingEntity) {
        val data = getData(entity)
        data.reset()

        // 1. API 属性
        apiAttributeCache[entity.uniqueId]?.values?.forEach { sourceMap ->
            sourceMap.forEach { (key, value) ->
                val current = data.get(key)
                data.setValueRange(key, current[0] + value, current[1] + value)
            }
        }

        // 2. 装备属性
        val items = entity.equipment?.let {
            listOfNotNull(it.helmet, it.chestplate, it.leggings, it.boots, it.itemInMainHand, it.itemInOffHand)
        } ?: return

        items.forEach { item ->
            if (item.isAir() || !ConditionChecker.check(entity, item)) return@forEach

            val compiledMap = getItemAttributes(item)
            compiledMap.forEach { (key, range) ->
                val current = data.get(key)
                data.setValueRange(key, current[0] + range[0], current[1] + range[1])
            }
        }

        // 3. Update 回调 (现在 ScriptAttribute 会自动处理这个)
        attributes.forEach { attr ->
            if (attr.type == AttributeType.UPDATE) {
                attr.onUpdate(entity, data.get(attr.key)[0])
            }
        }
    }

    private fun getItemAttributes(item: ItemStack): Map<String, DoubleArray> {
        val hash = item.hashCode()
        return itemAttributeCache.getOrPut(hash) {
            val resultMap = mutableMapOf<String, DoubleArray>()
            val tag = item.getItemTag()
            val itemLore = item.itemMeta?.lore ?: emptyList<String>()

            attributes.forEach { attr ->
                var range = tag.getDeepRange("$NBT_ROOT.${attr.key}")
                if (range[0] == 0.0 && range[1] == 0.0 && itemLore.isNotEmpty()) {
                    for (line in itemLore) {
                        val matchedName = attr.names.firstOrNull { name ->
                            line.contains("$name:") || line.contains("$name：") || line.contains(name)
                        }
                        if (matchedName != null) {
                            range = extractValueRange(line)
                            if (range[0] != 0.0 || range[1] != 0.0) break
                        }
                    }
                }
                if (range[0] != 0.0 || range[1] != 0.0) {
                    resultMap[attr.key] = range
                }
            }
            resultMap
        }
    }

    fun getCombatPower(entity: LivingEntity): Double {
        val data = getData(entity)
        var totalCp = 0.0
        attributes.forEach { attr ->
            val vals = data.get(attr.key)
            if (vals[0] != 0.0 || vals[1] != 0.0) {
                totalCp += (vals[0] + vals[1]) / 2.0 * attr.combatPower
            }
        }
        return totalCp
    }

    fun extractValueRange(lore: String): DoubleArray {
        val cleanLore = org.bukkit.ChatColor.stripColor(lore) ?: lore
        val rangeRegex = "(\\d+(\\.\\d+)?)\\s*-\\s*(\\d+(\\.\\d+)?)".toRegex()
        val rangeMatch = rangeRegex.find(cleanLore)
        if (rangeMatch != null) {
            val v1 = Coerce.toDouble(rangeMatch.groupValues[1])
            val v2 = Coerce.toDouble(rangeMatch.groupValues[3])
            return doubleArrayOf(Math.min(v1, v2), Math.max(v1, v2))
        }
        val singleMatch = "(\\d+(\\.\\d+)?)".toRegex().find(cleanLore)
        val v = Coerce.toDouble(singleMatch?.value)
        return doubleArrayOf(v, v)
    }

    private fun startUpdateTask() {
        submit(period = CoreConfig.refreshInterval) {
            Bukkit.getOnlinePlayers().forEach { player ->
                val data = getData(player)
                val regen = data.get("health_regen")[0]
                if (regen > 0 && !player.isDead) {
                    val maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
                    if (player.health < maxHealth) {
                        player.health = (player.health + regen).coerceAtMost(maxHealth)
                    }
                }
            }
        }
    }

    private fun startCleanupTask() {
        submit(period = 6000, async = true) {
            val toRemove = mutableListOf<UUID>()
            entityDataCache.keys.forEach { uuid ->
                val entity = Bukkit.getEntity(uuid)
                if (entity == null || !entity.isValid || entity.isDead) {
                    toRemove.add(uuid)
                }
            }
            toRemove.forEach { removeData(it) }
        }
    }

    fun getAttributeKeys(): List<String> = attributes.map { it.key }
}