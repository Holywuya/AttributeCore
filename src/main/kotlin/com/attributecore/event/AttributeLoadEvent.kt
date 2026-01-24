package com.attributecore.event

import com.attributecore.data.AttributeType
import com.attributecore.data.DamageData
import com.attributecore.data.attribute.BaseAttribute
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.File
import java.nio.charset.StandardCharsets

/**
 * 属性配置加载器
 * 修复了 ConfigurationSection 类型不匹配和资源释放的问题
 */
object AttributeLoader {

    /** 资源中的属性文件夹路径 */
    private const val RESOURCE_ATTRIBUTES_PATH = "attributes"

    /** 插件数据文件夹中的属性文件夹 */
    private val dataAttributesFolder: File
        get() = File(getDataFolder(), RESOURCE_ATTRIBUTES_PATH)

    /**
     * 加载属性的主入口
     */
    fun loadAttributesFromFolder(): List<BaseAttribute> {
        val attributes = mutableListOf<BaseAttribute>()

        // 1. 初始化文件夹 (如果没有文件，生成默认配置)
        prepareFolderAndResources()

        // 2. 扫描文件夹中的所有 .yml 文件
        val files = dataAttributesFolder.listFiles { _, name -> name.endsWith(".yml") }

        if (files.isNullOrEmpty()) {
            console().sendMessage("§e[AttributeLoader] §f未找到属性配置文件。")
            return emptyList()
        }

        files.forEach { file ->
            try {
                // 加载配置文件
                val config = Configuration.loadFromFile(file, Type.YAML)

                // 3. 遍历根节点的每一个 Key (每个 Key 就是一个属性 ID)
                config.getKeys(false).forEach { key ->
                    try {
                        // ✅ 获取属性配置节 (ConfigurationSection)
                        val section = config.getConfigurationSection(key)

                        if (section != null) {
                            // 调用修复后的方法
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
     * 准备文件夹并生成示例资源
     * (移除 runningResources 逻辑，改为检测文件夹为空则生成)
     */
    private fun prepareFolderAndResources() {
        if (!dataAttributesFolder.exists()) {
            dataAttributesFolder.mkdirs()
        }

        // 如果文件夹里没有 yml 文件，生成默认示例
        val ymlFiles = dataAttributesFolder.listFiles { _, name -> name.endsWith(".yml") }
        if (ymlFiles.isNullOrEmpty()) {
            createExampleConfigs()
        }
    }

    /**
     * ✅ 修复核心点：参数类型改为 ConfigurationSection
     * 这样既可以接收 Configuration 对象，也可以接收 getConfigurationSection 返回的对象
     */
    private fun loadAttributeFromSection(key: String, section: ConfigurationSection): BaseAttribute? {
        // 读取配置
        val typeStr = section.getString("type", "OTHER")!!.uppercase()
        val type = try { AttributeType.valueOf(typeStr) } catch (e: Exception) { AttributeType.OTHER }

        val priority = section.getInt("priority", 0)
        val display = section.getString("display", key) ?: key

        // 读取别名列表
        val names = section.getStringList("names").takeIf { it.isNotEmpty() } ?: listOf(key)

        // 行为标识符
        val behavior = section.getString("behavior", "default") ?: "default"

        // 创建匿名内部类实例
        return object : BaseAttribute(key, names, type, priority) {
            override fun getDisplayName(): String = display.replace("&", "§")

            override fun onAttack(damageData: DamageData, value: Double, extraValue: Double) {
                handleBehavior(behavior, damageData, value)
            }

            override fun onDefend(damageData: DamageData, value: Double, extraValue: Double) {
                if (behavior == "defend" || behavior == "reduce_damage") {
                    damageData.reduceDamage(value)
                }
            }
        }
    }

    /**
     * 简单的行为处理器
     */
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

    /**
     * 重新加载
     */
    fun reloadAttributes(): List<BaseAttribute> {
        console().sendMessage("§e[AttributeLoader] §f正在重载属性配置...")
        return loadAttributesFromFolder()
    }

    /**
     * 创建硬编码的示例配置
     */
    private fun createExampleConfigs() {
        val file = File(dataAttributesFolder, "example_attributes.yml")
        if (!file.exists()) {
            // 使用 UTF-8 写入防止乱码
            file.writeText("""
# ==========================================
#         AttributeCore 属性配置文件
# ==========================================
# 可以在一个文件中定义多个属性
# 根节点名称即为属性ID (key)

# --- 攻击力 ---
attack_damage:
  type: ATTACK
  display: "&c攻击力"
  priority: 10
  behavior: "add_damage"
  names:
    - "攻击力"
    - "物理攻击"
    - "Damage"

# --- 暴击率 ---
crit_rate:
  type: ATTACK
  display: "&6暴击率"
  priority: 5
  behavior: "crit"
  names:
    - "暴击率"
    - "Crit"

# --- 防御力 ---
defense:
  type: DEFENSE
  display: "&9防御力"
  priority: 10
  behavior: "reduce_damage"
  names:
    - "防御力"
    - "护甲"
    - "Defense"
            """.trimIndent(), StandardCharsets.UTF_8)
            console().sendMessage("§e[AttributeLoader] §f已生成默认示例文件: example_attributes.yml")
        }
    }
}