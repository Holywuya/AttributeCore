package com.attributecore.manager

import com.attributecore.data.Elements
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.module.nms.getItemTag

/**
 * 武器元素伤害类型读取器
 * 
 * 从武器NBT中读取元素伤害类型，用于确定武器造成的伤害元素
 * 如果武器没有定义元素类型，默认为物理伤害
 * 
 * NBT格式示例:
 * ```
 * AttributeCore:
 *   元素类型: "FIRE"     # 或 "火", "fire" 等
 * ```
 * 
 * 支持的元素类型:
 * - PHYSICAL / 物理 (默认)
 * - FIRE / 火
 * - WATER / 水
 * - ICE / 冰
 * - ELECTRO / 雷
 * - WIND / 风
 * - 以及任何自定义元素
 */
object WeaponElementReader {
    
    // NBT 键名
    private const val NBT_ROOT = "AttributeCore"
    private const val NBT_ELEMENT_KEY = "元素类型"
    private const val NBT_ELEMENT_KEY_ALT = "ElementType"
    
    // 元素名称映射 (中文 -> 标准名)
    private val elementNameMapping = mapOf(
        "物理" to Elements.PHYSICAL,
        "火" to Elements.FIRE,
        "水" to Elements.WATER,
        "冰" to Elements.ICE,
        "雷" to Elements.ELECTRO,
        "风" to Elements.WIND
    )
    
    /**
     * 从物品NBT读取元素类型
     * 
     * @param item 要读取的物品
     * @return 元素类型字符串，默认返回 PHYSICAL
     */
    fun getWeaponElement(item: ItemStack?): String {
        if (item == null || item.type.isAir) {
            return Elements.PHYSICAL
        }
        
        return try {
            val nbt = item.getItemTag()
            val attributeSection = nbt[NBT_ROOT]
            
            if (attributeSection is Map<*, *>) {
                @Suppress("UNCHECKED_CAST")
                val attrs = attributeSection as? Map<String, Any> ?: return Elements.PHYSICAL
                
                // 尝试读取 "元素类型" 或 "ElementType"
                val elementValue = attrs[NBT_ELEMENT_KEY] ?: attrs[NBT_ELEMENT_KEY_ALT]
                
                if (elementValue != null) {
                    normalizeElementName(elementValue.toString())
                } else {
                    Elements.PHYSICAL
                }
            } else {
                Elements.PHYSICAL
            }
        } catch (e: Exception) {
            Elements.PHYSICAL
        }
    }
    
    /**
     * 获取实体主手武器的元素类型
     * 
     * @param entity 实体
     * @return 元素类型
     */
    fun getMainHandElement(entity: LivingEntity): String {
        val equipment = entity.equipment ?: return Elements.PHYSICAL
        return getWeaponElement(equipment.itemInMainHand)
    }
    
    /**
     * 获取实体副手武器的元素类型
     * 
     * @param entity 实体
     * @return 元素类型
     */
    fun getOffHandElement(entity: LivingEntity): String {
        val equipment = entity.equipment ?: return Elements.PHYSICAL
        return getWeaponElement(equipment.itemInOffHand)
    }
    
    /**
     * 获取玩家当前使用的武器元素类型
     * 优先检查主手，如果主手为空气则检查副手
     * 
     * @param entity 实体
     * @return 元素类型
     */
    fun getActiveWeaponElement(entity: LivingEntity): String {
        val equipment = entity.equipment ?: return Elements.PHYSICAL
        
        val mainHand = equipment.itemInMainHand
        if (!mainHand.type.isAir) {
            val element = getWeaponElement(mainHand)
            if (element != Elements.PHYSICAL) {
                return element
            }
        }
        
        // 如果主手是物理或空气，检查副手
        val offHand = equipment.itemInOffHand
        if (!offHand.type.isAir) {
            return getWeaponElement(offHand)
        }
        
        return Elements.PHYSICAL
    }
    
    /**
     * 检查武器是否有元素附魔（非物理元素）
     * 
     * @param item 物品
     * @return 是否有元素
     */
    fun hasElementalEnchant(item: ItemStack?): Boolean {
        val element = getWeaponElement(item)
        return !Elements.isPhysical(element)
    }
    
    /**
     * 检查实体是否装备了元素武器
     * 
     * @param entity 实体
     * @return 是否装备元素武器
     */
    fun hasElementalWeapon(entity: LivingEntity): Boolean {
        val element = getActiveWeaponElement(entity)
        return !Elements.isPhysical(element)
    }
    
    /**
     * 标准化元素名称
     * 将中文名或各种格式转换为标准的大写英文名
     * 
     * @param name 输入的元素名
     * @return 标准化后的元素名
     */
    private fun normalizeElementName(name: String): String {
        val trimmed = name.trim()
        
        // 首先检查中文名映射
        elementNameMapping[trimmed]?.let { return it }
        
        // 使用 Elements 的标准化方法
        return Elements.normalize(trimmed)
    }
    
    /**
     * 获取所有支持的元素名称（用于自动补全等）
     */
    fun getSupportedElements(): List<String> {
        return listOf(
            Elements.PHYSICAL,
            Elements.FIRE,
            Elements.WATER,
            Elements.ICE,
            Elements.ELECTRO,
            Elements.WIND
        )
    }
}
