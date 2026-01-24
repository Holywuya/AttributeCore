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
import java.nio.charset.StandardCharsets

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

        prepareFolderAndResources()

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
                            if (attribute != null) {
                                attributes.add(attribute)
                                console().sendMessage("§a[AttributeLoader] §f  └─ 加载属性: §e${attribute.getDisplayName()} §7($key)")
                            }
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
    private fun loadAttributeFromSection(key: String, section: ConfigurationSection): BaseAttribute? {
        val typeStr = section.getString("type", "OTHER")!!.uppercase()
        val type = try { AttributeType.valueOf(typeStr) } catch (e: Exception) { AttributeType.OTHER }
        val priority = section.getInt("priority", 0)
        val display = section.getString("display", key) ?: key
        val names = section.getStringList("names").takeIf { it.isNotEmpty() } ?: listOf(key)
        val behavior = section.getString("behavior", "default") ?: "default"
        val attributeTags = section.getStringList("tags").map { it.uppercase() }

        return object : BaseAttribute(key, names, type, priority, attributeTags) {
            override fun getDisplayName(): String = display.replace("&", "§")

            override fun onAttack(damageData: DamageData, value: Double, extraValue: Double) {
                // 1. 注入标签
                this.tags.forEach { damageData.addTag(it) }

                // 2. 托管给行为处理器
                AttributeBehaviors.handleAttack(behavior, damageData, value)
            }

            override fun onDefend(damageData: DamageData, value: Double, extraValue: Double) {
                // 1. 标签校验
                if (this.tags.isNotEmpty()) {
                    if (this.tags.none { damageData.hasTag(it) }) return
                }

                // 2. 托管给行为处理器
                AttributeBehaviors.handleDefend(behavior, damageData, value)
            }

            override fun onUpdate(entity: LivingEntity, value: Double) {
                // 最大生命值处理
                if (key == "max_health" || behavior == "add_health") {
                    val base = 20.0 // 默认血量，或者从 entity.getAttribute(GENERIC_MAX_HEALTH).baseValue 获取
                    val finalVal = base + value

                    val attrInstance = entity.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH)
                    if (attrInstance != null && attrInstance.baseValue != finalVal) {
                        attrInstance.baseValue = finalVal
                        // 可选：如果是回血，可以在这里做
                    }
                }

                // 移动速度处理
                if (key == "move_speed") {
                    // Bukkit 默认 walkSpeed 是 0.2
                    // 假设 value 是百分比，比如 10 代表增加 10%
                    val defaultSpeed = 0.2f
                    val newSpeed = (defaultSpeed * (1 + value / 100.0)).toFloat().coerceIn(0.0f, 1.0f)
                    if (entity is org.bukkit.entity.Player) {
                        entity.walkSpeed = newSpeed
                    }
                }
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

    private fun prepareFolderAndResources() {
        if (!dataAttributesFolder.exists()) dataAttributesFolder.mkdirs()
        val ymlFiles = dataAttributesFolder.listFiles { _, name -> name.endsWith(".yml") }
        if (ymlFiles.isNullOrEmpty()) createExampleConfigs()
    }

    /**
     * 生成包含 Tags 的示例配置
     */
    private fun createExampleConfigs() {
        val file = File(dataAttributesFolder, "example_combat.yml")
        if (!file.exists()) {
            file.writeText("""
# ==========================================
#         定向伤害与防御配置示例
# ==========================================

# 物理攻击：带有 PHYSICAL 标签
physical_attack:
  type: ATTACK
  display: "&f物理攻击"
  priority: 10
  behavior: "add_damage"
  tags: 
    - "PHYSICAL"
  names:
    - "物理攻击"

# 物理防御：只减少带有 PHYSICAL 标签的伤害
physical_defense:
  type: DEFENSE
  display: "&f物理防御"
  priority: 10
  behavior: "reduce_damage"
  tags: 
    - "PHYSICAL"
  names:
    - "物理防御"

# 火焰攻击：带有 FIRE 标签
fire_attack:
  type: ATTACK
  display: "&c火焰攻击"
  priority: 10
  behavior: "add_damage"
  tags: 
    - "FIRE"
  names:
    - "火焰攻击"

# 火焰抗性：只减少带有 FIRE 标签的伤害
fire_resistance:
  type: DEFENSE
  display: "&e火焰抗性"
  priority: 10
  behavior: "reduce_damage"
  tags: 
    - "FIRE"
  names:
    - "火焰抗性"

# 全域防御：没有标签，对所有伤害生效
global_defense:
  type: DEFENSE
  display: "&7全域防御"
  priority: 5
  behavior: "reduce_damage"
  names:
    - "全域防御"
            """.trimIndent(), StandardCharsets.UTF_8)
        }
    }

    fun reloadAttributes(): List<BaseAttribute> {
        console().sendMessage("§e[AttributeLoader] §f正在重载属性配置...")
        return loadAttributesFromFolder()
    }
}