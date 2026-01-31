package com.attributecore

import com.attributecore.attribute.*
import com.attributecore.hook.mythicmobs.MythicMobsHook
import com.attributecore.manager.ItemAttributeReader
import com.attributecore.script.JsAttributeLoader
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration

object AttributeCore : Plugin() {
    @Config
    lateinit var config: Configuration
        private set

    override fun onEnable() {
        info("AttributeCore 已启动 - 版本 1.9.0.1")
        info("基于 SX-Attribute 3.x 架构重构")
        info("支持 JavaScript 自定义属性")
        info("MythicMobs 兼容层: TabooLib UM")

        AttackDamage()
        Defense()
        CritChance()
        CritDamage()
        
        MaxHealth()
        HealthRegen()
        MovementSpeed()
        AttackSpeed()
        Luck()
        Strength()
        Agility()
        Armor()
        ArmorPenetration()
        
        ItemAttributeReader.refreshNbtNameMapping()

        val coreCount = com.attributecore.data.SubAttribute.getAttributes().size
        info("已加载 $coreCount 个核心属性")

        val jsCount = JsAttributeLoader.getJsAttributes().size
        info("已加载 $jsCount 个 JS 自定义属性")
        
        val total = com.attributecore.data.SubAttribute.getAttributes().size
        info("属性总数: $total")
        
        MythicMobsHook.setup()
    }

    override fun onDisable() {
        MythicMobsHook.shutdown()
        info("AttributeCore 已关闭")
    }
}
