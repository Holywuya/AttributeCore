package com.attributecore.listener

import com.attributecore.manager.AttributeManager
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent
import org.bukkit.event.entity.EntityDeathEvent
import taboolib.common.platform.event.OptionalEvent
import taboolib.common.platform.event.SubscribeEvent
import java.util.concurrent.ThreadLocalRandom

object MythicMobsListener {

    /**
     * 怪物生成：读取配置 -> 存入 Manager 内存
     */
    @SubscribeEvent(bind = "io.lumine.mythic.bukkit.events.MythicMobSpawnEvent")
    fun onSpawn(e: OptionalEvent) {
        val event = e.source as MythicMobSpawnEvent
        val entity = event.entity
        val config = event.mobType.config


        var attrList = config.getStringList("AttributeCore")
        if (attrList.isEmpty()) {
            attrList = config.getStringList("Attributes")
        }

        if (attrList.isEmpty()) return

        var hasAttribute = false

        attrList.forEach { line ->
            // 解析 "属性名: 数值" 或 "属性名: min-max"
            val split = line.split(":")
            if (split.size >= 2) {
                val name = split[0].trim()
                val valueStr = split[1].trim()
                val value = parseRandomValue(valueStr)

                if (value != 0.0) {
                    // 匹配属性
                    val attribute = AttributeManager.getAttributes().find { attr ->
                        attr.key == name || attr.names.contains(name)
                    }

                    if (attribute != null) {
                        // ✅ 调用 Manager 的 API 存入数据 (内存操作)
                        AttributeManager.setApiAttribute(entity.uniqueId, attribute.key, value)
                        hasAttribute = true
                    }
                }
            }
        }

        // 如果有属性，立即刷新一次以应用
        if (hasAttribute) {
            AttributeManager.update(entity as org.bukkit.entity.LivingEntity)
        }
    }

    /**
     * 怪物死亡：清理数据 (非常重要，防止内存泄漏)
     */
    @SubscribeEvent
    fun onDeath(e: EntityDeathEvent) {
        // 如果该实体有自定义属性数据，将其移除
        AttributeManager.removeData(e.entity.uniqueId)
    }

    /**
     * 范围随机解析工具
     */
    private fun parseRandomValue(str: String): Double {
        return try {
            if (str.contains("-")) {
                val range = str.split("-")
                val min = range[0].trim().toDouble()
                val max = range[1].trim().toDouble()
                if (max > min) ThreadLocalRandom.current().nextDouble(min, max) else min
            } else {
                str.toDouble()
            }
        } catch (e: Exception) {
            0.0
        }
    }
}