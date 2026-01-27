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
import taboolib.common5.Coerce
import java.io.File
import java.io.FileReader
import java.util.concurrent.ConcurrentHashMap
import javax.script.Invocable
import javax.script.ScriptEngineManager

object ScriptManager {

    private val manager = ScriptEngineManager()
    private val folder = File(getDataFolder(), "scripts")

    // 缓存 JS 引擎的可调用接口
    private val scriptCache = ConcurrentHashMap<String, Invocable>()

    fun init() {
        if (!folder.exists()) {
            folder.mkdirs()
            try { releaseResourceFile("scripts/example_attribute.js", false) } catch (e: Exception) {}
        }
    }

    fun reload() {
        // 确保文件夹存在
        if (!folder.exists()) init()
        // 这里可以做一些清理工作，但由于我们不缓存 Script 对象（都传给 Attribute 了），
        // 所以这里主要用于状态重置或日志输出
        console().sendMessage("§7[AttributeCore] §f正在重置脚本环境...")
    }

    /**
     * 重载并加载所有脚本定义的属性
     */
    fun loadAttributes(): List<BaseAttribute> {
        scriptCache.clear()
        val list = mutableListOf<BaseAttribute>()

        if (!folder.exists()) init()

        console().sendMessage("§7[AttributeCore] §f正在加载脚本属性...")

        folder.listFiles { _, n -> n.endsWith(".js") }?.forEach { file ->
            try {
                // 1. 获取引擎
                val engine = manager.getEngineByName("js")
                    ?: throw RuntimeException("当前环境不支持 JavaScript")

                // 2. 注入环境 API
                engine.put("api", JavaScriptAPI)
                engine.put("Bukkit", org.bukkit.Bukkit::class.java)
                // 魔法：把 api 方法提取到全局
                engine.eval("for (var m in api) { if (typeof api[m] === 'function') { this[m] = api[m].bind(api); } }")

                // 3. 执行脚本
                engine.eval(FileReader(file))

                val inv = engine as Invocable
                val scriptId = file.nameWithoutExtension.lowercase()
                scriptCache[scriptId] = inv

                // 4. 尝试获取属性配置 (getSettings)
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
                e.printStackTrace()
            }
        }
        return list
    }

    // ========================================================
    //               ✅ 核心修复：添加 invoke 方法
    // ========================================================

    /**
     * 执行攻击逻辑
     * ReactionManager 和 ScriptAttribute 会调用此方法
     */
    fun invokeAttack(scriptId: String, attr: BaseAttribute, attacker: LivingEntity, victim: LivingEntity, data: DamageData, value: Double) {
        val inv = scriptCache[scriptId.lowercase()] ?: return
        try {
            // 包装参数，实现拟人化 API
            inv.invokeFunction(
                "runAttack",
                attr,
                ScriptEntity(attacker),
                ScriptEntity(victim),
                ScriptHandle(data, value)
            )
        } catch (e: NoSuchMethodException) {
            // 忽略未定义函数的错误
        } catch (e: Exception) {
            console().sendMessage("§c[AttributeCore] 执行脚本 $scriptId (runAttack) 出错: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 执行防御逻辑
     */
    fun invokeDefend(scriptId: String, attr: BaseAttribute, attacker: LivingEntity, victim: LivingEntity, data: DamageData, value: Double) {
        val inv = scriptCache[scriptId.lowercase()] ?: return
        try {
            inv.invokeFunction(
                "runDefend",
                attr,
                ScriptEntity(attacker),
                ScriptEntity(victim), // 这里 victim 就是防御者自己
                ScriptHandle(data, value)
            )
        } catch (e: NoSuchMethodException) {
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 执行更新逻辑
     */
    fun invokeUpdate(scriptId: String, attr: BaseAttribute, entity: LivingEntity, value: Double) {
        val inv = scriptCache[scriptId.lowercase()] ?: return
        try {
            // Update 时没有 DamageData，传 null
            inv.invokeFunction(
                "runUpdate",
                attr,
                ScriptEntity(entity),
                value,
                ScriptHandle(null, value)
            )
        } catch (e: NoSuchMethodException) {
        } catch (e: Exception) {
            e.printStackTrace()
        }
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