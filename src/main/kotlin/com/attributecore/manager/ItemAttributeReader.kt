package com.attributecore.manager

import com.attributecore.data.AttributeData
import com.attributecore.data.SubAttribute
import com.attributecore.util.DebugLogger
import org.bukkit.inventory.ItemStack
import taboolib.module.nms.getItemTag

object ItemAttributeReader {

    fun readItem(item: ItemStack?): AttributeData {
        val data = AttributeData()
        if (item == null || item.type.isAir) return data

        parseAttributesFromNBT(data, item)
        item.itemMeta?.lore?.forEach { lore ->
            parseAttributesFromLore(data, lore)
        }

        return data
    }
    
    fun parseAttributesFromLore(data: AttributeData, lore: String) {
        val cleanLore = lore.split("§X")[0]
        if (cleanLore.isEmpty()) return

        DebugLogger.logAttributeLoading("解析 Lore: $cleanLore")

        SubAttribute.getAttributes().forEach { attribute ->
            attribute.loadAttribute(data, cleanLore)
        }
    }

    fun parseAttributesFromNBT(data: AttributeData, item: ItemStack) {
        val nbt = item.getItemTag()
        val attributeSection = nbt["AttributeCore"] ?: return

        if (attributeSection is Map<*, *>) {
            @Suppress("UNCHECKED_CAST")
            val attrs = attributeSection as? Map<String, Any> ?: return

            attrs.forEach { (key, value) ->
                when {
                    key == "Percent" && value is Map<*, *> -> {
                        @Suppress("UNCHECKED_CAST")
                        (value as Map<String, Any>).forEach { (pKey, pValue) ->
                            val doubleValue = parseNumber(pValue)
                            data.addPercent(pKey, doubleValue)
                        }
                    }
                    key.endsWith("_percent") -> {
                        val baseKey = key.removeSuffix("_percent")
                        val doubleValue = parseNumber(value)
                        data.addPercent(baseKey, doubleValue)
                    }
                    else -> {
                        val doubleValue = parseNumber(value)
                        data.add(key, doubleValue)
                    }
                }
            }
        }
    }

    private fun parseNumber(value: Any): Double {
        return when (value) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
    }
}
