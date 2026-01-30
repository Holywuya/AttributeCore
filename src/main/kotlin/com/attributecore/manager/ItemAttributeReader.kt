package com.attributecore.manager

import com.attributecore.data.AttributeData
import com.attributecore.data.SubAttribute
import com.attributecore.util.DebugLogger
import org.bukkit.inventory.ItemStack
import taboolib.module.nms.getItemTag

object ItemAttributeReader {
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
                val doubleValue = when (value) {
                    is Number -> value.toDouble()
                    is String -> value.toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }
                data.add(key, doubleValue)
            }
        }
    }
}
