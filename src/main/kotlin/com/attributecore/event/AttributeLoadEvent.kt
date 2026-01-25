package com.attributecore.event

import com.attributecore.data.AttributeType
import com.attributecore.data.DamageData
import com.attributecore.data.attribute.BaseAttribute
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.File
import org.bukkit.entity.LivingEntity
import taboolib.common.io.runningResources

object AttributeLoader {
    private val folder = File(getDataFolder(), "attributes")

    fun loadAttributesFromFolder(): List<BaseAttribute> {
        val list = mutableListOf<BaseAttribute>()

        console().sendMessage("§7[AttributeCore] §f正在从 §eattributes §f文件夹加载属性...")

        // 1. 自动释放资源文件夹
        prepareResources()

        val files = folder.listFiles { _, n -> n.endsWith(".yml") }
        if (files.isNullOrEmpty()) {
            console().sendMessage("§7[AttributeCore] §e未在 attributes 文件夹下找到任何 .yml 配置文件")
            return emptyList()
        }

        console().sendMessage("§7[AttributeCore] §8找到 §7${files.size} §8个属性配置文件")

        files.forEach { f ->
            try {
                val cfg = Configuration.loadFromFile(f, Type.YAML)
                val keys = cfg.getKeys(false)

                keys.forEach { key ->
                    try {
                        val sec = cfg.getConfigurationSection(key)
                        if (sec != null) {
                            val attr = loadAttribute(key, sec)
                            list.add(attr)
                            // 打印每个属性的加载详细信息
                            console().sendMessage("§7[AttributeCore] §a已载入属性: §f${attr.getDisplayName()} §8($key) §7来自 §8${f.name}")
                        }
                    } catch (e: Exception) {
                        console().sendMessage("§c[AttributeCore] §f属性 §e$key §c解析失败 (文件: ${f.name}): ${e.localizedMessage}")
                    }
                }
            } catch (e: Exception) {
                console().sendMessage("§c[AttributeCore] §f读取文件 §e${f.name} §c时发生错误 (YAML格式错误?): ${e.localizedMessage}")
            }
        }

        console().sendMessage("§7[AttributeCore] §f加载完成，共计识别 §b${list.size} §f个有效属性")
        return list
    }

    /**
     * 使用 TabooLib 机制释放 attributes 文件夹下的所有默认资源
     */
    private fun prepareResources() {
        if (!folder.exists()) {
            folder.mkdirs()
            console().sendMessage("§7[AttributeCore] §8初始化 attributes 文件夹")
        }

        val resources = runningResources.keys.filter { it.startsWith("attributes/") && it.endsWith(".yml") }
        if (resources.isEmpty()) {
            resources.forEach { path ->
                // 获取文件名用于提示
                val fileName = path.substringAfterLast("/")
                if (!File(folder, fileName).exists()) {
                    releaseResourceFile(path, false)
                    console().sendMessage("§7[AttributeCore] §8释放默认属性资源: §f$fileName")
                }
            }
        }
    }

    private fun loadAttribute(key: String, section: ConfigurationSection): BaseAttribute {
        val type = try {
            AttributeType.valueOf(section.getString("type", "OTHER")!!.uppercase())
        } catch (e: Exception) {
            AttributeType.OTHER
        }

        val priority = section.getInt("priority", 0)
        val display = section.getString("display", key) ?: key
        val names = section.getStringList("names").takeIf { it.isNotEmpty() } ?: listOf(key)
        val behavior = section.getString("behavior", "default") ?: "default"
        val attributeTags = section.getStringList("tags").map { it.uppercase() }.toMutableList()
        val element = section.getString("elements")?.uppercase()

        // 如果定义了元素，自动给 tags 加上 ELEMENT_ 前缀，保证兼容性
        if (element != null) {
            attributeTags.add("ELEMENT_$element")
        }

        return object : BaseAttribute(key, names, type, priority, attributeTags) {
            override fun getDisplayName() = display.replace("&", "§")

            override fun onAttack(d: DamageData, v: Double, ev: Double) {
                this.tags.forEach { d.addTag(it) }
                AttributeBehaviors.handleAttack(behavior, d, v, this.tags)
            }

            override fun onDefend(d: DamageData, v: Double, ev: Double) {
                if (this.tags.isNotEmpty() && this.tags.none { d.hasTag(it) }) return
                AttributeBehaviors.handleDefend(behavior, d, v, this.tags)
            }

            override fun onUpdate(e: LivingEntity, v: Double) {
                AttributeBehaviors.handleUpdate(e, key, behavior, v)
            }
        }
    }
}