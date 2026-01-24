package com.attributecore.event

import com.attributecore.data.AttributeType
import com.attributecore.data.DamageData
import com.attributecore.data.attribute.BaseAttribute
import org.bukkit.entity.LivingEntity
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.File

/**
 * 属性配置加载器
 * 已集成标签 (Tags) 过滤系统：实现物理防御仅抵挡物理伤害等功能
 */
object AttributeLoader {

    private const val RESOURCE_ATTRIBUTES_PATH = "attributes"

    private val dataAttributesFolder: File
        get() = File(getDataFolder(), RESOURCE_ATTRIBUTES_PATH)

    /**
     * 加载属性的主入口
     */
    fun loadAttributesFromFolder(): List<BaseAttribute> {
        val attributes = mutableListOf<BaseAttribute>()
        val files = dataAttributesFolder.listFiles { _, name -> name.endsWith(".yml") }
        if (files.isNullOrEmpty()) return emptyList()

        files.forEach { file ->
            try {
                val config = Configuration.loadFromFile(file, Type.YAML)
                config.getKeys(false).forEach { key ->
                    try {
                        val section = config.getConfigurationSection(key)
                        if (section != null) {
                            val attribute = loadAttributeFromSection(key, section)
                            attributes.add(attribute)
                            console().sendMessage("§a[AttributeLoader] §f  └─ 加载属性: §e${attribute.getDisplayName()} §7($key)")
                        }
                    } catch (e: Exception) {
                        console().sendMessage("§c[AttributeLoader] §f  └─ 解析属性 '$key' 失败: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                console().sendMessage("§c[AttributeLoader] §f读取文件 '${file.name}' 失败: ${e.message}")
            }
        }

        console().sendMessage("§a[AttributeLoader] §f共加载了 §b${attributes.size} §f个属性配置")
        return attributes
    }

    /**
     * 解析配置节并创建属性实例
     */

    private fun loadAttributeFromSection(key: String, section: ConfigurationSection): BaseAttribute {
        val type = try { AttributeType.valueOf(section.getString("type", "OTHER")!!.uppercase()) } catch (e: Exception) { AttributeType.OTHER }
        val priority = section.getInt("priority", 0)
        val display = section.getString("display", key) ?: key
        val names = section.getStringList("names").takeIf { it.isNotEmpty() } ?: listOf(key)
        val behavior = section.getString("behavior", "default") ?: "default"
        val attributeTags = section.getStringList("tags").map { it.uppercase() }

        return object : BaseAttribute(key, names, type, priority, attributeTags) {
            override fun getDisplayName() = display.replace("&", "§")

            override fun onAttack(d: DamageData, v: Double, ev: Double) {
                // ✅ 使用 this.tags 注入到 DamageData 的上下文标签中
                this.tags.forEach { d.addTag(it) }

                // ✅ 将 this.tags 传给行为处理器，用于识别元素反应
                AttributeBehaviors.handleAttack(behavior, d, v, this.tags)
            }

            override fun onDefend(d: DamageData, v: Double, ev: Double) {
                // ✅ 使用 this.tags 进行防御匹配逻辑
                if (this.tags.isNotEmpty() && this.tags.none { d.hasTag(it) }) return

                AttributeBehaviors.handleDefend(behavior, d, v, this.tags)
            }

            override fun onUpdate(e: LivingEntity, v: Double) {
                AttributeBehaviors.handleUpdate(e, key, behavior, v)
            }
        }
    }

    private fun handleBehavior(behavior: String, damageData: DamageData, value: Double) {
        when (behavior) {
            "add_damage" -> damageData.addDamage(value)
            "multiply_damage" -> {
                val bonus = damageData.getFinalDamage() * (value / 100.0)
                damageData.addDamage(bonus)
            }
            "crit" -> {
                if (Math.random() * 100 < value) {
                    damageData.addDamage(damageData.getFinalDamage())
                }
            }
        }
    }
}