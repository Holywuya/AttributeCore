package com.attributecore.manager

import com.attributecore.event.CoreConfig
import com.attributecore.data.AttributeData
import com.attributecore.data.AttributeType
import com.attributecore.data.attribute.BaseAttribute
import com.attributecore.event.ReactionLoader
import com.attributecore.util.ConditionChecker
import org.bukkit.Bukkit
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import taboolib.common.platform.function.info
import taboolib.common.platform.function.submit
import taboolib.platform.util.isAir
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object AttributeManager {

    private val attributes = mutableListOf<BaseAttribute>()
    private val apiAttributeCache = ConcurrentHashMap<UUID, ConcurrentHashMap<String, MutableMap<String, Double>>>()

    fun init() {
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

        Bukkit.getOnlinePlayers().forEach { player ->
            update(player)
        }

        info("§a[AttributeManager] 属性重载完成")
    }

    private fun reloadAttributesInternal() {
        attributes.clear()
        attributes.addAll(ScriptManager.loadAttributes())
        ItemAttributeParser.updateLoreMap(attributes)
    }

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
        apiAttributeCache.remove(uuid)
        EntityDataManager.removeData(uuid)
        ShieldManager.removeData(uuid)
    }

    fun getAttributes() = attributes.sortedBy { it.priority }

    fun getData(entity: LivingEntity): AttributeData = EntityDataManager.getData(entity)

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

            val compiledMap = ItemAttributeParser.parseItemAttributes(item, attributes)
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

    fun getCombatPower(entity: LivingEntity): Double {
        return CombatPowerCalculator.calculate(entity, attributes)
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
        submit(period = 6000, async = false) {
            val toRemove = mutableListOf<UUID>()
            EntityDataManager.getLoadedUUIDs().forEach { uuid ->
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