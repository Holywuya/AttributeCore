package com.attributecore.listener

import com.attributecore.manager.AttributeManager
import ink.ptms.um.event.MobSpawnEvent
import org.bukkit.event.entity.EntityDeathEvent
import taboolib.common.platform.event.SubscribeEvent
import java.util.concurrent.ThreadLocalRandom

object MythicMobsListener {

    /**
     * 监听 MM 怪生成 (通过 UM 兼容层)
     */
    @SubscribeEvent
    fun onSpawn(e: MobSpawnEvent) {
        val mob = e.mob ?: return
        val entity = mob.entity as? org.bukkit.entity.LivingEntity ?: return
        val config = mob.type.config

        // 清理旧数据，防止重载或重复触发导致属性无限叠加
        AttributeManager.clearApiAttributes(entity.uniqueId)

        // 读取配置 (兼容 SX 写法)
        var list = config.getStringList("SX-Attribute")
        if (list.isEmpty()) list = config.getStringList("Attributes")

        if (list.isEmpty()) return

        var hasAttr = false
        list.forEach { line ->
            val split = line.split(":")
            if (split.size >= 2) {
                val name = split[0].trim()
                val valStr = split[1].trim()
                val value = parseRandom(valStr)

                if (value != 0.0) {
                    // 这里我们定义的变量名是 attr
                    val attr = AttributeManager.getAttributes().find { it.key == name || it.names.contains(name) }

                    if (attr != null) {
                        // ✅ 修复 2：将 attribute 改为上面定义的 attr
                        // 使用 "MythicMobs" 作为 Source 来源名，适配最新的多来源 API
                        AttributeManager.setApiAttribute(entity.uniqueId, "MythicMobs", attr.key, value)
                        hasAttr = true
                    }
                }
            }
        }

        if (hasAttr) {
            AttributeManager.update(entity)
        }
    }

    @SubscribeEvent
    fun onDeath(e: EntityDeathEvent) {
        AttributeManager.removeData(e.entity.uniqueId)
    }

    private fun parseRandom(str: String): Double {
        return try {
            if (str.contains("-")) {
                val arr = str.split("-")
                val min = taboolib.common5.Coerce.toDouble(arr[0].trim())
                val max = if (arr.size > 1) taboolib.common5.Coerce.toDouble(arr[1].trim()) else min
                if (max > min) ThreadLocalRandom.current().nextDouble(min, max) else min
            } else taboolib.common5.Coerce.toDouble(str)
        } catch (e: Exception) { 0.0 }
    }
}