package com.attributecore.script

import com.attributecore.AttributeCore
import com.attributecore.data.AttributeType
import com.attributecore.data.SubAttribute
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning
import taboolib.common5.scriptEngineFactory
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.script.Invocable
import javax.script.ScriptEngine

object JsAttributeLoader {
    private val jsAttributes = ConcurrentHashMap<String, JsAttribute>()
    private var initialized = false

    fun getAttributesDir(): File = File(getDataFolder(), "attributes")

    @Awake(LifeCycle.ENABLE)
    fun init() {
        if (initialized) return
        initialized = true
        
        try {
            scriptEngineFactory.scriptEngine.eval("1+1")
        } catch (e: Exception) {
            warning("JavaScript 引擎不可用，JS 属性系统已禁用: ${e.message}")
            return
        }

        releaseDefaultAttributes()
        loadAttributes()
    }

    private fun releaseDefaultAttributes() {
        val attributesDir = getAttributesDir()
        if (!attributesDir.exists()) {
            attributesDir.mkdirs()
        }

        val defaultAttributes = listOf(
            "lifesteal.js",
            "dodge.js",
            "thorns.js",
            "execute.js"
        )

        defaultAttributes.forEach { fileName ->
            val targetFile = File(attributesDir, fileName)
            if (!targetFile.exists()) {
                try {
                    val inputStream = AttributeCore::class.java.classLoader
                        .getResourceAsStream("attributes/$fileName")
                    if (inputStream != null) {
                        targetFile.writeBytes(inputStream.readBytes())
                        info("已释放默认属性脚本: $fileName")
                    }
                } catch (e: Exception) {
                    warning("无法释放默认属性脚本 $fileName: ${e.message}")
                }
            }
        }
    }

    fun loadAttributes() {
        jsAttributes.clear()
        val attributesDir = getAttributesDir()

        if (!attributesDir.exists() || !attributesDir.isDirectory) {
            info("属性脚本目录不存在: ${attributesDir.path}")
            return
        }

        attributesDir.listFiles { file -> file.extension == "js" }?.forEach { jsFile ->
            loadAttribute(jsFile)
        }

        info("已加载 ${jsAttributes.size} 个 JS 属性")
    }

    private fun loadAttribute(jsFile: File) {
        try {
            val scriptContent = jsFile.readText(Charsets.UTF_8)
            val scriptName = jsFile.nameWithoutExtension

            val engine: ScriptEngine = scriptEngineFactory.scriptEngine
            injectBindings(engine)
            engine.eval(scriptContent)

            val invocable = engine as? Invocable
            if (invocable == null) {
                warning("脚本引擎不支持 Invocable: $scriptName")
                return
            }

            val jsAttr = JsAttribute(engine, invocable, scriptName)
            jsAttributes[jsAttr.name] = jsAttr

            SubAttribute.register(jsAttr)

            val typeStr = jsAttr.types.joinToString(", ") { it.name }
            info("已加载 JS 属性: ${jsAttr.name} (优先级: ${jsAttr.priority}, 类型: $typeStr)")

        } catch (e: Exception) {
            warning("加载 JS 属性失败 ${jsFile.name}: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun injectBindings(engine: ScriptEngine) {
        engine.put("AttributeType", AttributeType::class.java)
        engine.put("Java", JavaHelper)
    }

    fun reload() {
        jsAttributes.values.forEach { attr ->
            val attrs = SubAttribute.getAttributes().toMutableList()
            attrs.removeIf { it.name == attr.name }
        }

        loadAttributes()
        SubAttribute.resort()
        info("已重载 JS 属性系统")
    }

    fun getJsAttribute(name: String): JsAttribute? = jsAttributes[name]

    fun getJsAttributes(): List<JsAttribute> = jsAttributes.values.toList()

    fun getJsAttributesByType(type: AttributeType): List<JsAttribute> {
        return jsAttributes.values.filter { it.containsType(type) }
    }

    object JavaHelper {
        fun type(className: String): Class<*>? {
            return try {
                Class.forName(className)
            } catch (e: Exception) {
                null
            }
        }
    }
}
