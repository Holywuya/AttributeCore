package com.attributecore.manager

import com.attributecore.AttributeCore
import com.attributecore.data.SubAttribute
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning

object AttributeRegistry {
    private var initialized = false
    private val pendingAttributes = mutableListOf<SubAttribute>()
    
    fun register(attribute: SubAttribute) {
        if (initialized) {
            doRegister(attribute)
        } else {
            pendingAttributes.add(attribute)
        }
    }
    
    @Awake(LifeCycle.ENABLE)
    fun initialize() {
        if (initialized) return
        initialized = true
        
        loadPrioritiesFromConfig()
        
        pendingAttributes.forEach { doRegister(it) }
        pendingAttributes.clear()
        
        syncConfigPriorities()
        
        info("[AttributeRegistry] Loaded ${SubAttribute.getAttributes().size} attributes")
    }
    
    private fun loadPrioritiesFromConfig() {
        val config = AttributeCore.config
        val priorityList = config.getStringList("attribute-priority")
        
        priorityList.forEachIndexed { index, name ->
            val attrName = name.split("#").first()
            pendingAttributes.find { it.name == attrName }?.let {
                it.priority = index
            }
        }
    }
    
    private fun doRegister(attribute: SubAttribute) {
        if (attribute.priority < 0) {
            attribute.priority = getNextPriority()
        }
        
        SubAttribute.registerInternal(attribute)
    }
    
    private fun getNextPriority(): Int {
        val maxPriority = SubAttribute.getAttributes().maxOfOrNull { it.priority } ?: -1
        val pendingMax = pendingAttributes.filter { it.priority >= 0 }.maxOfOrNull { it.priority } ?: -1
        return maxOf(maxPriority, pendingMax) + 1
    }
    
    private fun syncConfigPriorities() {
        val config = AttributeCore.config
        val currentList = config.getStringList("attribute-priority").toMutableList()
        val registeredNames = SubAttribute.getAttributes().map { it.name }.toSet()
        
        var modified = false
        
        SubAttribute.getAttributes().sortedBy { it.priority }.forEach { attr ->
            if (!currentList.any { it.split("#").first() == attr.name }) {
                currentList.add(attr.name)
                modified = true
                info("[AttributeRegistry] Added '${attr.name}' to attribute-priority")
            }
        }
        
        val toRemove = currentList.filter { entry ->
            val name = entry.split("#").first()
            name !in registeredNames
        }
        if (toRemove.isNotEmpty()) {
            currentList.removeAll(toRemove.toSet())
            modified = true
            warning("[AttributeRegistry] Removed unregistered attributes: ${toRemove.joinToString()}")
        }
        
        if (modified) {
            config["attribute-priority"] = currentList
            config.saveToFile()
        }
        
        currentList.forEachIndexed { index, entry ->
            val name = entry.split("#").first()
            SubAttribute.getByName(name)?.priority = index
        }
        
        SubAttribute.resort()
    }
    
    fun reload() {
        val config = AttributeCore.config
        config.reload()
        
        val priorityList = config.getStringList("attribute-priority")
        priorityList.forEachIndexed { index, entry ->
            val name = entry.split("#").first()
            SubAttribute.getByName(name)?.priority = index
        }
        
        SubAttribute.resort()
        info("[AttributeRegistry] Reloaded attribute priorities")
    }
}
