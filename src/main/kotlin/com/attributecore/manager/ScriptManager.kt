package com.attributecore.manager

import com.attributecore.api.JavaScriptAPI
import com.attributecore.api.ScriptEntity
import com.attributecore.api.ScriptHandle
import com.attributecore.data.AttributeType
import com.attributecore.data.DamageData
import com.attributecore.data.attribute.BaseAttribute
import com.attributecore.data.attribute.ScriptAttribute
import org.bukkit.entity.LivingEntity
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common.platform.function.submit
import taboolib.common5.Coerce
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.script.Invocable
import taboolib.common5.compileJS
import taboolib.common5.scriptEngine
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake

object ScriptManager {

    @Awake(LifeCycle.ENABLE)
    fun onEnable() {
        val folder = dataFolder
        if (!folder.exists()) {
            folder.mkdirs()
            try { releaseResourceFile("scripts/example_sword.js", false) } catch (e: Exception) {}
        }
    }

    @Awake(LifeCycle.DISABLE)
    fun onDisable() {
        scriptCache.clear()
    }

    private val dataFolder: File by lazy { File(getDataFolder(), "scripts") }

    private val scriptCache = ConcurrentHashMap<String, Invocable>()

    fun reload() {
        submit(async = true) {
            if (!dataFolder.exists()) {
                dataFolder.mkdirs()
                try { releaseResourceFile("scripts/example_sword.js", false) } catch (e: Exception) {}
            }
            console().sendMessage("§7[AttributeCore] §f正在重置脚本环境...")
        }
    }

    /**
     * 重载并加载所有脚本定义的属性
     */
    fun loadAttributes(): List<BaseAttribute> {
        scriptCache.clear()
        val list = mutableListOf<BaseAttribute>()

        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
            try { releaseResourceFile("scripts/example_sword.js", false) } catch (e: Exception) {}
        }

        console().sendMessage("§7[AttributeCore] §f正在加载脚本属性...")

        dataFolder.listFiles { _, n -> n.endsWith(".js") }?.forEach { file ->
            try {
                val scriptContent = file.readText()
                
                // 1. 注入环境 API
                scriptEngine.put("api", JavaScriptAPI)
                scriptEngine.put("Bukkit", org.bukkit.Bukkit::class.java)

                // 2. 预编译脚本以提高性能
                val compiledScript = scriptContent.compileJS()
                if (compiledScript != null) {
                    compiledScript.eval()
                } else {
                    // 如果编译失败，直接执行
                    scriptEngine.eval(scriptContent)
                }

                val inv = scriptEngine as Invocable
                val scriptId = file.nameWithoutExtension.lowercase()
                scriptCache[scriptId] = inv

                @Suppress("UNCHECKED_CAST")
                val settings = try {
                    inv.invokeFunction("getSettings") as? Map<String, Any>
                } catch (e: NoSuchMethodException) {
                    // 脚本没写配置函数，视为纯逻辑脚本 (如反应脚本)
                    null
                }

                if (settings != null) {
                    val attr = parseSettingsToAttribute(scriptId, settings, inv)
                    list.add(attr)
                    console().sendMessage("§7[AttributeCore] §a已注册脚本属性: §f${attr.key} §7(Script: ${file.name})")
                } else {
                    console().sendMessage("§7[AttributeCore] §e载入逻辑脚本: ${file.name}")
                }

            } catch (e: Exception) {
                console().sendMessage("§c[AttributeCore] 脚本 ${file.name} 加载失败: ${e.message}")
                ScriptErrorLogger.logError(file.name, e)
            }
        }
        return list
    }

    // ========================================================
    //               ✅ 核心修复：添加 invoke 方法
    // ========================================================

    private fun invokeScript(
        scriptId: String,
        functionName: String,
        suppressMissingFunction: Boolean = true,
        vararg args: Any
    ) {
        val inv = scriptCache[scriptId.lowercase()] ?: return
        try {
            inv.invokeFunction(functionName, *args)
        } catch (e: NoSuchMethodException) {
            if (!suppressMissingFunction) {
                console().sendMessage("§e[AttributeCore] 脚本 $scriptId 缺少函数: $functionName")
            }
        } catch (e: Exception) {
            handleScriptError(scriptId, functionName, e)
        }
    }

    private fun handleScriptError(scriptId: String, functionName: String, error: Exception) {
        console().sendMessage("§c[AttributeCore] 执行脚本 $scriptId ($functionName) 出错: ${error.message}")
        ScriptErrorLogger.logRuntimeError(scriptId, functionName, error)
    }

    fun invokeAttack(scriptId: String, attr: BaseAttribute, attacker: LivingEntity, victim: LivingEntity, data: DamageData, value: Double) {
        invokeScript(
            scriptId = scriptId,
            functionName = "runAttack",
            suppressMissingFunction = true,
            attr,
            ScriptEntity(attacker),
            ScriptEntity(victim),
            ScriptHandle(data, value)
        )
    }

    fun invokeDefend(scriptId: String, attr: BaseAttribute, attacker: LivingEntity, victim: LivingEntity, data: DamageData, value: Double) {
        invokeScript(
            scriptId = scriptId,
            functionName = "runDefend",
            suppressMissingFunction = true,
            attr,
            ScriptEntity(attacker),
            ScriptEntity(victim),
            ScriptHandle(data, value)
        )
    }

    fun invokeUpdate(scriptId: String, attr: BaseAttribute, entity: LivingEntity, value: Double) {
        invokeScript(
            scriptId = scriptId,
            functionName = "runUpdate",
            suppressMissingFunction = true,
            attr,
            ScriptEntity(entity),
            value,
            ScriptHandle(null, value)
        )
    }

    // ========================================================
    //                  内部辅助方法
    // ========================================================

    private fun parseSettingsToAttribute(scriptId: String, map: Map<String, Any>, inv: Invocable): ScriptAttribute {
        val id = map["id"]?.toString() ?: scriptId
        val type = try { AttributeType.valueOf(map["type"]?.toString()?.uppercase() ?: "OTHER") } catch (e: Exception) { AttributeType.OTHER }
        val names = extractList(map["names"]).ifEmpty { mutableListOf(id) }
        val priority = Coerce.toInteger(map["priority"] ?: 0)
        val cp = Coerce.toDouble(map["combatPower"] ?: 1.0)
        val element = map["element"]?.toString()?.uppercase()
        val tags = extractList(map["tags"]).map { it.uppercase() }.toMutableList()

        if (element != null) tags.add("ELEMENT_$element")

        val display = map["display"]?.toString()?.replace("&", "§") ?: id

        // ✅ 修正：直接实例化 ScriptAttribute，并将 display 传入构造函数
        // 不再需要 "object : ScriptAttribute" 这种写法
        return ScriptAttribute(
            script = inv,
            key = id,
            names = names,
            type = type,
            priority = priority,
            tags = tags,
            element = element,
            combatPower = cp,
            displayName = display // 传入显示名称
        )
    }

    private fun extractList(obj: Any?): MutableList<String> {
        val list = mutableListOf<String>()
        if (obj == null) return list
        if (obj is List<*>) obj.forEach { list.add(it.toString()) }
        else if (obj is Map<*, *>) obj.values.forEach { list.add(it.toString()) }
        else list.add(obj.toString())
        return list
    }
}