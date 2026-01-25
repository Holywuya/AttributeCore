package com.attributecore.event

import taboolib.common.io.runningResources
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import taboolib.module.kether.parseKetherScript
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object BehaviorLoader {
    val scriptCache = ConcurrentHashMap<String, String>()
    private val folder = File(getDataFolder(), "behaviors")

    fun loadBehaviors() {
        scriptCache.clear()

        console().sendMessage("§7[AttributeCore] §f正在从 §ebehaviors §f文件夹加载并校验行为脚本...")

        prepareResources()

        val files = folder.listFiles { _, n -> n.endsWith(".yml") }
        if (files.isNullOrEmpty()) {
            console().sendMessage("§7[AttributeCore] §e未在 behaviors 文件夹下找到任何 .yml 脚本文件")
            return
        }

        // 定义命名空间，必须包含我们注册 ac 语句的 "attributecore"
        val namespaces = listOf("attributecore", "kether")

        files.forEach { f ->
            try {
                val cfg = Configuration.loadFromFile(f, Type.YAML)
                val keys = cfg.getKeys(false)

                keys.forEach { key ->
                    val script = cfg.getString(key)
                    if (!script.isNullOrBlank()) {
                        // ✅ 执行预解析校验
                        try {
                            // 模拟 KetherShell 的内部处理方式：
                            // 1. 如果不是以 def 开头，包裹在 main 结构中
                            val formattedScript = if (script.trim().startsWith("def ")) {
                                script
                            } else {
                                "def main = { $script }"
                            }

                            // 2. 调用 TabooLib 内置的扩展函数进行解析
                            // 如果语法错误或动作未注册，这里会直接抛出异常
                            formattedScript.parseKetherScript(namespaces)

                            // 解析成功，存入缓存
                            scriptCache[key.lowercase()] = script
                            console().sendMessage("§7[AttributeCore] §a已载入行为: §f$key §7来自 §8${f.name}")
                        } catch (e: Exception) {
                            // 解析失败，打印具体的 Kether 报错信息
                            console().sendMessage("§7[AttributeCore] §c校验失败! 忽略行为 §f$key §c(语法错误)")
                            console().sendMessage("§8└─ §4错误: ${e.localizedMessage}")

                            if (CoreConfig.debug) e.printStackTrace()
                        }
                    } else {
                        console().sendMessage("§7[AttributeCore] §e忽略行为 §f$key §e(脚本内容为空) §7来自 §8${f.name}")
                    }
                }
            } catch (e: Exception) {
                console().sendMessage("§c[AttributeCore] §f解析配置文件 §e${f.name} §c失败 (YAML格式问题): ${e.localizedMessage}")
            }
        }

        console().sendMessage("§7[AttributeCore] §f加载完成，共计识别 §b${scriptCache.size} §f个有效行为脚本")
    }

    private fun prepareResources() {
        if (!folder.exists()) {
            folder.mkdirs()
        }

        val resources = runningResources.keys.filter { it.startsWith("behaviors/") && it.endsWith(".yml") }
        if (resources.isNotEmpty()) {
            resources.forEach { path ->
                val fileName = path.substringAfterLast("/")
                if (!File(folder, fileName).exists()) {
                    releaseResourceFile(path, false)
                    console().sendMessage("§7[AttributeCore] §8释放默认行为资源: §f$fileName")
                }
            }
        }
    }
}