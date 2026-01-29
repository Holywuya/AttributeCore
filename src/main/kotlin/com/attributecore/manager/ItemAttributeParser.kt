package com.attributecore.manager

import com.attributecore.data.attribute.BaseAttribute
import com.attributecore.util.getDeepRange
import org.bukkit.inventory.ItemStack
import taboolib.common5.Coerce
import taboolib.common5.LoreMap
import taboolib.module.nms.getItemTag
import taboolib.platform.util.isAir
import java.util.concurrent.ConcurrentHashMap

private const val NBT_ROOT = "AttributeCore"

object ItemAttributeParser {

    private val loreMap = LoreMap<BaseAttribute>(true, true, true)
    
    private val itemAttributeCache = ConcurrentHashMap<Int, Map<String, DoubleArray>>()

    fun updateLoreMap(attributes: List<BaseAttribute>) {
        loreMap.clear()
        attributes.forEach { attr ->
            attr.names.forEach { name -> loreMap.put(name, attr) }
        }
    }

    fun parseItemAttributes(item: ItemStack, attributes: List<BaseAttribute>): Map<String, DoubleArray> {
        if (item.isAir()) return emptyMap()
        
        val hash = item.hashCode()
        return itemAttributeCache.getOrPut(hash) {
            parseItemAttributesInternal(item, attributes)
        }
    }

    private fun parseItemAttributesInternal(item: ItemStack, attributes: List<BaseAttribute>): Map<String, DoubleArray> {
        val resultMap = mutableMapOf<String, DoubleArray>()
        val tag = item.getItemTag()
        val itemLore = item.itemMeta?.lore ?: emptyList<String>()

        taboolib.common.platform.function.console().sendMessage("§e[AC-DEBUG] §f解析物品: ${item.type}, lore行数=${itemLore.size}")
        itemLore.forEachIndexed { index, line -> 
            taboolib.common.platform.function.console().sendMessage("§e[AC-DEBUG] §f  Lore[$index]: $line")
        }

        attributes.forEach { attr ->
            var range = tag.getDeepRange("$NBT_ROOT.${attr.key}")
            if (range[0] == 0.0 && range[1] == 0.0 && itemLore.isNotEmpty()) {
                for (line in itemLore) {
                    val matchedName = attr.names.firstOrNull { name ->
                        line.contains("$name:") || line.contains("$name：") || line.contains(name)
                    }
                    if (matchedName != null) {
                        taboolib.common.platform.function.console().sendMessage("§e[AC-DEBUG] §f  匹配属性: ${attr.key}, name=$matchedName, line=$line")
                        range = extractValueRangeFromLore(line)
                        taboolib.common.platform.function.console().sendMessage("§e[AC-DEBUG] §f  提取数值: [${range[0]}, ${range[1]}]")
                        if (range[0] != 0.0 || range[1] != 0.0) break
                    }
                }
            }
            if (range[0] != 0.0 || range[1] != 0.0) {
                resultMap[attr.key] = range
                taboolib.common.platform.function.console().sendMessage("§a[AC-DEBUG] §f添加属性: ${attr.key} = [${range[0]}, ${range[1]}]")
            }
        }
        return resultMap
    }

    fun extractValueRangeFromLore(lore: String): DoubleArray {
        val cleanLore = org.bukkit.ChatColor.stripColor(lore) ?: lore
        val rangeRegex = "(\\d+(\\.\\d+)?)\\s*-\\s*(\\d+(\\.\\d+)?)".toRegex()
        val rangeMatch = rangeRegex.find(cleanLore)
        if (rangeMatch != null) {
            val v1 = Coerce.toDouble(rangeMatch.groupValues[1])
            val v2 = Coerce.toDouble(rangeMatch.groupValues[3])
            return doubleArrayOf(Math.min(v1, v2), Math.max(v1, v2))
        }
        val singleMatch = "(\\d+(\\.\\d+)?)".toRegex().find(cleanLore)
        val v = Coerce.toDouble(singleMatch?.value)
        return doubleArrayOf(v, v)
    }

    fun clearCache() {
        itemAttributeCache.clear()
    }
}