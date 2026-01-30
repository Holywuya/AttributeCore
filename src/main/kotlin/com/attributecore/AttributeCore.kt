package com.attributecore

import com.attributecore.data.attribute.SXAttributeManager
import com.attributecore.data.attribute.SubAttribute
import com.attributecore.data.attribute.sub.attack.*
import com.attributecore.data.attribute.sub.defence.*
import com.attributecore.data.attribute.sub.update.*
import com.attributecore.listener.ListenerDamage
import com.attributecore.listener.ListenerUpdateAttribute
import com.attributecore.util.Config
import ink.ptms.um.Mythic
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info
import java.util.logging.Logger

object AttributeCore : Plugin() {

    lateinit var inst: JavaPlugin
        private set

    val logger: Logger by lazy { inst.logger }

    lateinit var attributeManager: SXAttributeManager
        private set

    var placeholderAPIEnabled = false
        private set
    var mythicMobsEnabled = false
        private set
    var vaultEnabled = false
        private set

    @Awake(LifeCycle.LOAD)
    override fun onLoad() {
        inst = Bukkit.getPluginManager().getPlugin("AttributeCore") as JavaPlugin
        
        Config.load()
        
        registerDefaultAttributes()
        
        info("§a[AttributeCore] §f已加载 ${SubAttribute.getAttributes().size} 个默认属性")
    }

    @Awake(LifeCycle.ENABLE)
    override fun onEnable() {
        info("§a[AttributeCore] §f正在启用...")

        attributeManager = SXAttributeManager()
        
        detectDependencies()
        
        Bukkit.getPluginManager().registerEvents(ListenerDamage(), inst)
        Bukkit.getPluginManager().registerEvents(ListenerUpdateAttribute(), inst)
        
        info("§a[AttributeCore] §f启用完成！")
        info("§a[AttributeCore] §f已注册属性: §e${SubAttribute.getAttributes().joinToString { it.name }}")
    }

    @Awake(LifeCycle.DISABLE)
    override fun onDisable() {
        info("§c[AttributeCore] §f正在禁用...")
        attributeManager.onDisable()
        SubAttribute.getAttributes().forEach { it.onDisable() }
        info("§c[AttributeCore] §f已禁用")
    }

    private fun registerDefaultAttributes() {
        Damage().registerAttribute()
        Crit().registerAttribute()
        HitRate().registerAttribute()
        LifeSteal().registerAttribute()
        Ignition().registerAttribute()
        Lightning().registerAttribute()
        RealDamage().registerAttribute()
        
        Defense().registerAttribute()
        Block().registerAttribute()
        Dodge().registerAttribute()
        Reflection().registerAttribute()
        Toughness().registerAttribute()
        
        Health().registerAttribute()
        HealthRegen().registerAttribute()
        WalkSpeed().registerAttribute()
    }

    private fun detectDependencies() {
        detectPlaceholderAPI()
        detectMythicMobs()
        detectVault()
    }

    private fun detectPlaceholderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderAPIEnabled = true
            info("§a[AttributeCore] §f检测到 PlaceholderAPI，已注册")
        }
    }

    private fun detectMythicMobs() {
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
            mythicMobsEnabled = true
            info("§a[AttributeCore] §f检测到 MythicMobs，已注册")
            if (Mythic.isLoaded()) {
                val api = Mythic.API
                if (api.isLegacy) {
                    info("§a[AttributeCore] §fMythicMobs 4.X 已注册")
                } else {
                    info("§a[AttributeCore] §fMythicMobs 5.X 已注册")
                }
            }
        }
    }

    private fun detectVault() {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            vaultEnabled = true
            info("§a[AttributeCore] §f检测到 Vault，已注册")
        }
    }
}