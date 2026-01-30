package com.attributecore.manager

import com.attributecore.data.AttributeData
import com.attributecore.data.SubAttribute
import org.bukkit.inventory.ItemStack
import taboolib.module.nms.getItemTag
import taboolib.common.platform.function.info

object ItemAttributeReader {
    fun parseAttributesFromLore(data: AttributeData, lore: String) {
        val cleanLore = lore.split("§X")[0]
        if (cleanLore.isEmpty()) return

        info("[Debug] 解析 Lore: $cleanLore")

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
