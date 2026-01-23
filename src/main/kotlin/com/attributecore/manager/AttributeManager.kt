package com.attributecore.manager


import com.attributecore.data.AttributeData
import com.attributecore.data.sub.SubAttribute
import com.attributecore.data.sub.subAttribute.AttributeDamage
import com.attributecore.util.ConfigLoader
import org.bukkit.entity.LivingEntity
import taboolib.module.nms.getItemTag
import taboolib.platform.util.isAir

import java.util.concurrent.CopyOnWriteArrayList

import kotlin.collections.iterator

object AttributeManager {
    val attributes = CopyOnWriteArrayList<SubAttribute>()
    private val NUMBER_PATTERN = Regex("""(\d+(?:\.\d+)?)""")

    fun init() {
        attributes.add(AttributeDamage())
        attributes.sort()
    }

    fun getEntityData(entity: LivingEntity): AttributeData {
        val data = AttributeData(entity.uniqueId)
        val equipment = entity.equipment ?: return data
        val enabledNames = ConfigLoader.attributes.getStringList("names")

        // 遍历所有装备位置
        listOfNotNull(
            equipment.helmet, equipment.chestplate, equipment.leggings, equipment.boots,
            equipment.itemInMainHand, equipment.itemInOffHand
        ).filter { !it.isAir() }.forEach { item ->
            val tag = item.getItemTag()
            val coreNode = tag["AttributeCore"]?.asCompound() ?: return@forEach

            for ((key, valueTag) in coreNode) {
                if (!enabledNames.contains(key)) continue
                // 解析 NBT 字符串中的数字 (例如 "100-200")
                val nums = NUMBER_PATTERN.findAll(valueTag.asString()).map { it.value.toDouble() }.toList()
                if (nums.isEmpty()) continue

                if (nums.size >= 2) {
                    data.add(key, nums[0])            // 最小值
                    data.add("$key.max", nums[1])     // 最大值
                } else {
                    data.add(key, nums[0])            // 单数值
                }
            }
        }
        return data
    }
}