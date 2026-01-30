package com.attributecore.data.attribute

import com.attributecore.AttributeCore
import com.attributecore.data.eventdata.sub.DamageData
import com.attributecore.data.eventdata.sub.UpdateData
import com.attributecore.util.Config
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.inventory.EntityEquipment
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class SXAttributeManager : Listener {

    private val entityDataMap = ConcurrentHashMap<UUID, SXAttributeData>()
    private var defaultAttributeData: SXAttributeData? = null

    init {
        Bukkit.getPluginManager().registerEvents(this, AttributeCore.inst)
        
        val attrs = SubAttribute.getAttributes()
        attrs.sorted()
        for (i in attrs.indices) {
            attrs[i].setPriority(i).loadConfig().onEnable()
        }
        
        loadDefaultAttributeData()
        
        AttributeCore.logger.info("Loaded ${attrs.size} Attributes")
    }

    @EventHandler
    fun onPluginEnable(event: PluginEnableEvent) {
        SubAttribute.getAttributes().forEach { attr ->
            if (attr is Listener) {
                Bukkit.getPluginManager().registerEvents(attr, event.plugin)
            }
        }
    }

    fun onDisable() {
        SubAttribute.getAttributes().forEach { it.onDisable() }
    }

    fun onReload() {
        SubAttribute.getAttributes().forEach { 
            it.loadConfig().onReload() 
        }
        loadDefaultAttributeData()
    }

    fun loadEntityData(entity: LivingEntity) {
        val preItemList = mutableListOf<PreLoadItem>()
        val equipment = entity.equipment
        
        equipment?.let { eq ->
            eq.helmet?.takeIf { it.type != Material.AIR }?.let {
                preItemList.add(PreLoadItem(EquipmentType.HELMET, it))
            }
            eq.chestplate?.takeIf { it.type != Material.AIR }?.let {
                preItemList.add(PreLoadItem(EquipmentType.CHESTPLATE, it))
            }
            eq.leggings?.takeIf { it.type != Material.AIR }?.let {
                preItemList.add(PreLoadItem(EquipmentType.LEGGINGS, it))
            }
            eq.boots?.takeIf { it.type != Material.AIR }?.let {
                preItemList.add(PreLoadItem(EquipmentType.BOOTS, it))
            }
            eq.itemInMainHand.takeIf { it.type != Material.AIR }?.let {
                preItemList.add(PreLoadItem(EquipmentType.MAIN_HAND, it))
            }
            eq.itemInOffHand.takeIf { it.type != Material.AIR }?.let {
                preItemList.add(PreLoadItem(EquipmentType.OFF_HAND, it))
            }
        }
        
        val attributeData = loadItemData(entity, preItemList)
        
        if (attributeData.isValid()) {
            entityDataMap[entity.uniqueId] = attributeData
        } else {
            entityDataMap.remove(entity.uniqueId)
        }
    }

    fun loadItemData(entity: LivingEntity, preItemList: List<PreLoadItem>): SXAttributeData {
        val filteredItems = preItemList.filter { item ->
            checkConditions(entity, item)
        }
        
        val attributeData = SXAttributeData()
        
        filteredItems.forEach { item ->
            val lore = item.item.itemMeta?.lore ?: return@forEach
            attributeData.add(loadListData(lore))
        }
        
        return attributeData
    }

    fun loadListData(lore: List<String>): SXAttributeData {
        val sxData = SXAttributeData()
        lore.forEach { line ->
            val cleanLine = line.split("§X")[0].takeIf { it.isNotEmpty() } ?: return@forEach
            SubAttribute.getAttributes().forEach { attr ->
                attr.loadAttribute(sxData.getValues(attr), cleanLine)
            }
        }
        return sxData
    }

    fun getEntityData(entity: LivingEntity): SXAttributeData {
        val data = SXAttributeData()
        
        entityDataMap[entity.uniqueId]?.let { data.add(it) }
        defaultAttributeData?.let { data.add(it) }
        
        data.calculationCombatPower()
        data.correct()
        
        return data
    }

    fun clearEntityData(uuid: UUID) {
        entityDataMap.remove(uuid)
    }

    fun attributeUpdateEvent(entity: LivingEntity) {
        Bukkit.getScheduler().runTask(AttributeCore.inst, Runnable {
            val updateData = UpdateData(entity)
            val attributeData = getEntityData(entity)
            
            SubAttribute.getAttributes().forEach { attr ->
                if (attr.containsType(AttributeType.UPDATE)) {
                    attr.eventMethod(attributeData.getValues(attr), updateData)
                }
            }
        })
    }

    private fun checkConditions(entity: LivingEntity, item: PreLoadItem): Boolean {
        return true
    }

    private fun loadDefaultAttributeData() {
        defaultAttributeData = SXAttributeData()
        Config.defaultAttribute.forEach { line ->
            SubAttribute.getAttributes().forEach { attr ->
                attr.loadAttribute(defaultAttributeData!!.getValues(attr), line)
            }
        }
    }

    fun getEntityDataMap(): Map<UUID, SXAttributeData> = entityDataMap.toMap()
}