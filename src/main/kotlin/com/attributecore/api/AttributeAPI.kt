package com.attributecore.api

import com.attributecore.data.AttributeType
import com.attributecore.data.Elements
import com.attributecore.data.SubAttribute
import com.attributecore.script.JsAttribute
import com.attributecore.script.JsAttributeLoader

object AttributeAPI {

    @JvmStatic
    fun getAll(): List<SubAttribute> {
        return SubAttribute.getAttributes()
    }

    @JvmStatic
    fun getAllNames(): List<String> {
        return SubAttribute.getAttributes().map { it.name }
    }

    @JvmStatic
    fun getByName(name: String): SubAttribute? {
        return SubAttribute.getByName(name)
    }

    @JvmStatic
    fun getByType(type: AttributeType): List<SubAttribute> {
        return SubAttribute.getAttributes().filter { it.containsType(type) }
    }

    @JvmStatic
    fun getByType(typeName: String): List<SubAttribute> {
        val type = try {
            AttributeType.valueOf(typeName.uppercase().replaceFirstChar { it.titlecase() })
        } catch (e: Exception) {
            return emptyList()
        }
        return getByType(type)
    }

    @JvmStatic
    fun getAttackAttributes(): List<SubAttribute> {
        return getByType(AttributeType.Attack)
    }

    @JvmStatic
    fun getDefenceAttributes(): List<SubAttribute> {
        return getByType(AttributeType.Defence)
    }

    @JvmStatic
    fun exists(name: String): Boolean {
        return SubAttribute.getByName(name) != null
    }

    @JvmStatic
    fun getJsAttributes(): List<JsAttribute> {
        return JsAttributeLoader.getJsAttributes()
    }

    @JvmStatic
    fun getJsAttribute(name: String): JsAttribute? {
        return JsAttributeLoader.getJsAttribute(name)
    }

    @JvmStatic
    fun getJsAttributesByType(type: AttributeType): List<JsAttribute> {
        return JsAttributeLoader.getJsAttributesByType(type)
    }

    @JvmStatic
    fun getJsAttributesByElement(element: String): List<JsAttribute> {
        val normalized = Elements.normalize(element)
        return JsAttributeLoader.getJsAttributes().filter { it.element == normalized }
    }

    @JvmStatic
    fun getNbtName(attributeName: String): String? {
        return SubAttribute.getByName(attributeName)?.nbtName
    }

    @JvmStatic
    fun getPlaceholder(attributeName: String): String? {
        return SubAttribute.getByName(attributeName)?.placeholder
    }

    @JvmStatic
    fun getPriority(attributeName: String): Int {
        return SubAttribute.getByName(attributeName)?.priority ?: 100
    }

    @JvmStatic
    fun getCombatPowerWeight(attributeName: String): Double {
        return SubAttribute.getByName(attributeName)?.combatPowerWeight ?: 1.0
    }

    @JvmStatic
    fun getTypes(attributeName: String): List<AttributeType> {
        return SubAttribute.getByName(attributeName)?.types?.toList() ?: emptyList()
    }

    @JvmStatic
    fun getElement(attributeName: String): String? {
        val attr = SubAttribute.getByName(attributeName)
        return if (attr is JsAttribute) attr.element else null
    }

    @JvmStatic
    fun getAttributeCount(): Int {
        return SubAttribute.getAttributes().size
    }

    @JvmStatic
    fun getJsAttributeCount(): Int {
        return JsAttributeLoader.getJsAttributes().size
    }

    @JvmStatic
    fun reload() {
        JsAttributeLoader.reload()
    }

    @JvmStatic
    fun getNbtNameMapping(): Map<String, String> {
        return SubAttribute.getAttributes().associate { it.nbtName to it.name }
    }

    @JvmStatic
    fun getAttributeNameFromNbt(nbtName: String): String? {
        return SubAttribute.getAttributes().find { it.nbtName == nbtName }?.name
    }

    @JvmStatic
    fun getAllPlaceholders(): Map<String, String> {
        return SubAttribute.getAttributes().associate { it.name to it.placeholder }
    }
}
