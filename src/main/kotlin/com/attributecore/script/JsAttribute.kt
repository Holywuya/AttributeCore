package com.attributecore.script

import com.attributecore.data.AttributeData
import com.attributecore.data.AttributeType
import com.attributecore.data.Element
import com.attributecore.data.SubAttribute
import com.attributecore.event.EventData
import com.attributecore.manager.AttributeManager
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import taboolib.common.platform.function.warning
import java.util.concurrent.ThreadLocalRandom
import java.util.regex.Pattern
import javax.script.Invocable
import javax.script.ScriptEngine

class JsAttribute(
    private val engine: ScriptEngine,
    private val invocable: Invocable,
    private val scriptName: String
) : SubAttribute(
    extractName(engine, scriptName),
    *extractTypes(engine)
) {
    private val jsPattern: Pattern?
    
    val element: Element
    
    override val nbtName: String

    init {
        priority = extractInt(engine, "priority", 100)
        combatPowerWeight = extractDouble(engine, "combatPower", 1.0)
        
        val patternStr = engine.get("pattern")?.toString()
        val patternSuffix = engine.get("patternSuffix")?.toString() ?: ""
        jsPattern = if (patternStr != null) {
            createPattern(patternStr, patternSuffix)
        } else {
            createPattern(name, "%")
        }
        
        nbtName = patternStr ?: name
        
        element = extractElement(engine)

        try {
            invocable.invokeFunction("onLoad", this)
        } catch (_: NoSuchMethodException) {
        } catch (e: Exception) {
            warning("[JsAttribute] $name onLoad() failed: ${e.message}")
        }
    }

    override val placeholder: String
        get() = engine.get("placeholder")?.toString() ?: name

    override fun loadAttribute(attributeData: AttributeData, lore: String) {
        jsPattern?.let { pattern ->
            matchValue(lore, pattern)?.let { value ->
                attributeData.add(name, value)
            }
        }
    }

    override fun eventMethod(attributeData: AttributeData, eventData: EventData) {
    }

    fun runAttack(attacker: LivingEntity, entity: LivingEntity, handle: AttributeHandle): Boolean {
        return try {
            val result = invocable.invokeFunction("runAttack", this, attacker, entity, handle)
            result as? Boolean ?: true
        } catch (_: NoSuchMethodException) {
            true
        } catch (e: Exception) {
            warning("[JsAttribute] $name runAttack() failed: ${e.message}")
            true
        }
    }

    fun runDefense(entity: LivingEntity, killer: LivingEntity?, handle: AttributeHandle): Boolean {
        return try {
            val result = invocable.invokeFunction("runDefense", this, entity, killer, handle)
            result as? Boolean ?: true
        } catch (_: NoSuchMethodException) {
            true
        } catch (e: Exception) {
            warning("[JsAttribute] $name runDefense() failed: ${e.message}")
            true
        }
    }

    fun runKiller(killer: LivingEntity, entity: LivingEntity, handle: AttributeHandle): Boolean {
        return try {
            val result = invocable.invokeFunction("runKiller", this, killer, entity, handle)
            result as? Boolean ?: true
        } catch (_: NoSuchMethodException) {
            true
        } catch (e: Exception) {
            warning("[JsAttribute] $name runKiller() failed: ${e.message}")
            true
        }
    }

    fun run(entity: LivingEntity, handle: AttributeHandle): Boolean {
        return try {
            val result = invocable.invokeFunction("run", this, entity, handle)
            result as? Boolean ?: true
        } catch (_: NoSuchMethodException) {
            true
        } catch (e: Exception) {
            warning("[JsAttribute] $name run() failed: ${e.message}")
            true
        }
    }

    fun runCustom(
        caster: LivingEntity?,
        target: LivingEntity?,
        params: Map<String, Any?>,
        source: String,
        handle: AttributeHandle
    ): Boolean {
        return try {
            val result = invocable.invokeFunction("runCustom", this, caster, target, params, source, handle)
            result as? Boolean ?: true
        } catch (_: NoSuchMethodException) {
            true
        } catch (e: Exception) {
            warning("[JsAttribute] $name runCustom() failed: ${e.message}")
            true
        }
    }

    override fun getPlaceholder(attributeData: AttributeData, player: Player, identifier: String): Any? {
        return try {
            invocable.invokeFunction("getPlaceholder", this, attributeData, player, identifier)
        } catch (_: NoSuchMethodException) {
            if (identifier == placeholder) attributeData[name] else null
        } catch (e: Exception) {
            null
        }
    }

    override fun getPlaceholders(): List<String> {
        return try {
            @Suppress("UNCHECKED_CAST")
            val result = invocable.invokeFunction("getPlaceholders", this)
            when (result) {
                is List<*> -> result.filterIsInstance<String>()
                is Array<*> -> result.filterIsInstance<String>()
                else -> listOf(placeholder)
            }
        } catch (_: NoSuchMethodException) {
            listOf(placeholder)
        } catch (e: Exception) {
            listOf(placeholder)
        }
    }

    fun getRandomValue(entity: LivingEntity, handle: AttributeHandle): Double {
        val data = AttributeManager.getEntityData(entity)
        return data[name]
    }
    
    fun getRandomValue(entity: LivingEntity, attrName: String, handle: AttributeHandle): Double {
        val data = AttributeManager.getEntityData(entity)
        return data[attrName]
    }

    fun getAttributeValue(entity: LivingEntity, handle: AttributeHandle): DoubleArray {
        val data = AttributeManager.getEntityData(entity)
        val value = data[name]
        return doubleArrayOf(value, value)
    }
    
    fun getAttributeValue(entity: LivingEntity, attrName: String, handle: AttributeHandle): DoubleArray {
        val data = AttributeManager.getEntityData(entity)
        val value = data[attrName]
        return doubleArrayOf(value, value)
    }

    fun getDamage(entity: LivingEntity, handle: AttributeHandle): Double = handle.getDamage()
    
    fun getDamage(entity: LivingEntity, element: String, handle: AttributeHandle): Double {
        val elem = Element.fromString(element) ?: Element.PHYSICAL
        return handle.getDamage(elem)
    }

    fun setDamage(entity: LivingEntity, value: Double, handle: AttributeHandle) {
        handle.setDamage(this.element, value)
    }
    
    fun setDamage(entity: LivingEntity, element: String, value: Double, handle: AttributeHandle) {
        val elem = Element.fromString(element) ?: Element.PHYSICAL
        handle.setDamage(elem, value)
    }

    fun addDamage(entity: LivingEntity, value: Double, handle: AttributeHandle) {
        handle.addDamage(this.element, value)
    }
    
    fun addDamage(entity: LivingEntity, element: String, value: Double, handle: AttributeHandle) {
        val elem = Element.fromString(element) ?: Element.PHYSICAL
        handle.addDamage(elem, value)
    }

    fun takeDamage(entity: LivingEntity, value: Double, handle: AttributeHandle) {
        handle.takeDamage(this.element, value)
    }
    
    fun takeDamage(entity: LivingEntity, element: String, value: Double, handle: AttributeHandle) {
        val elem = Element.fromString(element) ?: Element.PHYSICAL
        handle.takeDamage(elem, value)
    }
    
    fun addFinalDamage(entity: LivingEntity, value: Double, handle: AttributeHandle) {
        handle.addFinalDamage(value)
    }
    
    fun takeFinalDamage(entity: LivingEntity, value: Double, handle: AttributeHandle) {
        handle.takeFinalDamage(value)
    }
    
    fun setFinalDamage(entity: LivingEntity, value: Double, handle: AttributeHandle) {
        handle.setFinalDamage(value)
    }

    fun chance(percent: Double): Boolean = ThreadLocalRandom.current().nextDouble(100.0) < percent

    fun setCancelled(cancelled: Boolean, handle: AttributeHandle) = handle.setCancelled(cancelled)
    
    fun heal(entity: LivingEntity, amount: Double, handle: AttributeHandle) = handle.heal(entity, amount)
    
    fun getElement(): String = element.name
    
    fun getElementDisplayName(): String = element.displayName

    companion object {
        private fun extractName(engine: ScriptEngine, fallback: String): String {
            return engine.get("attributeName")?.toString() 
                ?: engine.get("name")?.toString() 
                ?: fallback
        }

        private fun extractTypes(engine: ScriptEngine): Array<AttributeType> {
            val typeObj = engine.get("attributeType") ?: return arrayOf(AttributeType.Other)
            return when (typeObj) {
                is String -> arrayOf(parseType(typeObj))
                is Collection<*> -> typeObj.mapNotNull { parseType(it?.toString()) }.toTypedArray()
                else -> arrayOf(AttributeType.Other)
            }
        }

        private fun parseType(name: String?): AttributeType {
            if (name == null) return AttributeType.Other
            return try {
                AttributeType.valueOf(name.uppercase().replaceFirstChar { it.titlecase() })
            } catch (e: Exception) {
                when (name.uppercase()) {
                    "ATTACK" -> AttributeType.Attack
                    "DEFENSE", "DEFENCE" -> AttributeType.Defence
                    "UPDATE" -> AttributeType.Update
                    "RUNTIME" -> AttributeType.Runtime
                    "KILLER" -> AttributeType.Killer
                    "CUSTOM" -> AttributeType.Custom
                    else -> AttributeType.Other
                }
            }
        }
        
        private fun extractElement(engine: ScriptEngine): Element {
            val elementStr = engine.get("element")?.toString() ?: return Element.PHYSICAL
            return Element.fromString(elementStr) ?: Element.PHYSICAL
        }

        private fun extractInt(engine: ScriptEngine, key: String, default: Int): Int {
            return when (val value = engine.get(key)) {
                is Number -> value.toInt()
                is String -> value.toIntOrNull() ?: default
                else -> default
            }
        }

        private fun extractDouble(engine: ScriptEngine, key: String, default: Double): Double {
            return when (val value = engine.get(key)) {
                is Number -> value.toDouble()
                is String -> value.toDoubleOrNull() ?: default
                else -> default
            }
        }
    }
}
