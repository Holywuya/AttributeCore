package com.attributecore.script

import com.attributecore.AttributeCore
import com.attributecore.api.AttributeCoreAPI
import com.attributecore.api.ElementAPI
import com.attributecore.data.Element
import org.bukkit.Bukkit
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning
import taboolib.common5.compileJS
import taboolib.common5.scriptEngine
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.script.CompiledScript
import javax.script.Invocable
import javax.script.ScriptEngine
import javax.script.SimpleBindings

object ScriptManager {
    private val scripts = ConcurrentHashMap<String, LoadedScript>()
    private val compiledCache = ConcurrentHashMap<String, CompiledScript>()

    data class LoadedScript(
        val name: String,
        val engine: ScriptEngine,
        val invocable: Invocable,
        val phases: Set<ScriptPhase>,
        val settings: Map<String, Any?>? = null,
        val compiled: CompiledScript? = null
    )

    @Awake(LifeCycle.LOAD)
    fun init() {
        try {
            scriptEngine.eval("1+1")
        } catch (e: Exception) {
            warning("JavaScript 引擎不可用，脚本系统已禁用: ${e.message}")
            return
        }

        releaseDefaultScripts()
        loadScripts()
    }

    private fun releaseDefaultScripts() {
        val scriptsDir = getScriptsDir()
        if (!scriptsDir.exists()) {
            scriptsDir.mkdirs()
        }

        val defaultScripts = listOf(
            "example_attack_damage.js",
            "example_defense.js",
            "example_crit_chance.js",
            "example_crit_damage.js",
            "example_lifesteal.js",
            "example_fire_damage.js",
            "example_armor_penetration.js",
            "example_damage_reduction.js",
            "example_health_regen.js",
            "example_advanced_conditional.js",
            "example_advanced_chain_reaction.js"
        )
        
        defaultScripts.forEach { scriptName ->
            val targetFile = File(scriptsDir, scriptName)
            if (!targetFile.exists()) {
                try {
                    val inputStream = AttributeCore::class.java.classLoader
                        .getResourceAsStream("scripts/$scriptName")
                    if (inputStream != null) {
                        targetFile.writeBytes(inputStream.readBytes())
                        info("已释放默认脚本: $scriptName")
                    }
                } catch (e: Exception) {
                    warning("无法释放默认脚本 $scriptName: ${e.message}")
                }
            }
        }
    }

    fun loadScripts() {
        scripts.clear()
        compiledCache.clear()
        val scriptsDir = getScriptsDir()
        
        if (!scriptsDir.exists() || !scriptsDir.isDirectory) {
            return
        }

        scriptsDir.listFiles { file -> file.extension == "js" }?.forEach { jsFile ->
            loadScript(jsFile)
        }

        info("已加载 ${scripts.size} 个脚本")
    }

    private fun loadScript(jsFile: File) {
        try {
            val scriptContent = jsFile.readText(Charsets.UTF_8)
            val scriptName = jsFile.nameWithoutExtension

            val compiled = scriptContent.compileJS()
            if (compiled != null) {
                compiledCache[scriptName] = compiled
            }

            val engine = taboolib.common5.scriptEngineFactory.scriptEngine
            
            injectBindings(engine)
            engine.eval(scriptContent)

            val invocable = engine as Invocable

            @Suppress("UNCHECKED_CAST")
            val phasesObj = engine.get("phases")
            val phases = when (phasesObj) {
                is Collection<*> -> phasesObj.mapNotNull { 
                    when (it) {
                        is ScriptPhase -> it
                        is String -> ScriptPhase.entries.find { p -> p.name == it }
                        else -> null
                    }
                }.toSet()
                else -> ScriptPhase.entries.toSet()
            }

            val settings = extractSettings(invocable, scriptName)
            scripts[scriptName] = LoadedScript(scriptName, engine, invocable, phases, settings, compiled)
            
            val attrInfo = settings?.get("key")?.let { " (属性: $it)" } ?: ""
            info("已加载脚本: $scriptName$attrInfo")

        } catch (e: Exception) {
            warning("加载脚本失败 ${jsFile.name}: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun injectBindings(engine: ScriptEngine) {
        engine.put("Bukkit", Bukkit::class.java)
        engine.put("Element", Element::class.java)
        engine.put("ScriptPhase", ScriptPhase::class.java)
        engine.put("AttributeCore", AttributeCore)
        engine.put("ScriptManager", this)
        engine.put("API", ScriptAPI)
        engine.put("AttributeCoreAPI", AttributeCoreAPI)
        engine.put("ElementAPI", ElementAPI)
    }

    fun createBindings(context: ScriptContext? = null): SimpleBindings {
        return SimpleBindings().apply {
            put("Bukkit", Bukkit::class.java)
            put("Element", Element::class.java)
            put("API", ScriptAPI)
            put("AttributeCoreAPI", AttributeCoreAPI)
            put("ElementAPI", ElementAPI)
            
            if (context != null) {
                put("context", context)
                put("attacker", context.attacker)
                put("victim", context.victim)
                put("attackerData", context.attackerData)
                put("victimData", context.victimData)
                put("damageBucket", context.damageBucket)
                put("triggerElement", context.triggerElement)
                put("auraElement", context.auraElement)
            }
        }
    }

    fun executePhase(phase: ScriptPhase, context: ScriptContext) {
        scripts.values
            .filter { it.phases.contains(phase) }
            .forEach { script ->
                try {
                    val methodName = "on${phase.name.lowercase().replaceFirstChar { it.uppercase() }}"
                    script.invocable.invokeFunction(methodName, context)
                } catch (e: NoSuchMethodException) {
                    // 方法不存在，跳过
                } catch (e: Exception) {
                    warning("脚本 ${script.name} 执行 $phase 阶段失败: ${e.message}")
                }
            }
    }

    fun executeExpression(expression: String, variables: Map<String, Any?> = emptyMap()): Any? {
        return try {
            val bindings = SimpleBindings(variables).apply {
                put("API", ScriptAPI)
                put("Math", java.lang.Math::class.java)
            }
            scriptEngine.eval(expression, bindings)
        } catch (e: Exception) {
            warning("表达式执行失败: $expression - ${e.message}")
            null
        }
    }

    fun executeCompiledScript(scriptName: String, variables: Map<String, Any?> = emptyMap()): Any? {
        val compiled = compiledCache[scriptName] ?: return null
        return try {
            val bindings = createBindings().apply { putAll(variables) }
            compiled.eval(bindings)
        } catch (e: Exception) {
            warning("编译脚本执行失败: $scriptName - ${e.message}")
            null
        }
    }

    fun invokeFunction(scriptName: String, functionName: String, vararg args: Any?): Any? {
        val script = scripts[scriptName] ?: return null
        return try {
            script.invocable.invokeFunction(functionName, *args)
        } catch (e: Exception) {
            null
        }
    }

    fun getScriptsDir(): File = File(getDataFolder(), "scripts")

    fun reload() {
        scripts.values.forEach { script ->
            try {
                script.invocable.invokeFunction("onDisable")
            } catch (_: Exception) {}
        }
        
        loadScripts()
        
        scripts.values.forEach { script ->
            try {
                script.invocable.invokeFunction("onEnable")
            } catch (_: Exception) {}
        }
    }

    fun getLoadedScripts(): List<String> = scripts.keys.toList()

    private fun extractSettings(invocable: Invocable, scriptName: String): Map<String, Any?>? {
        return try {
            val result = invocable.invokeFunction("getSettings")
            @Suppress("UNCHECKED_CAST")
            when (result) {
                is Map<*, *> -> result as Map<String, Any?>
                else -> {
                    val clazz = result?.javaClass ?: return null
                    if (clazz.name.contains("ScriptObjectMirror")) {
                        val keysMethod = clazz.getMethod("keySet")
                        val getMethod = clazz.getMethod("get", Any::class.java)
                        @Suppress("UNCHECKED_CAST")
                        val keys = keysMethod.invoke(result) as Set<String>
                        keys.associateWith { getMethod.invoke(result, it) }
                    } else null
                }
            }
        } catch (e: NoSuchMethodException) {
            null
        } catch (e: Exception) {
            warning("脚本 $scriptName 的 getSettings() 执行失败: ${e.message}")
            null
        }
    }

    fun getAttributeScripts(): List<LoadedScript> {
        return scripts.values.filter { it.settings != null }
    }

    fun getScript(name: String): LoadedScript? = scripts[name]

    fun invokeAttack(scriptName: String, vararg args: Any?): Any? {
        return invokeFunction(scriptName, "runAttack", *args)
    }

    fun invokeDefence(scriptName: String, vararg args: Any?): Any? {
        return invokeFunction(scriptName, "runDefence", *args)
    }
}
