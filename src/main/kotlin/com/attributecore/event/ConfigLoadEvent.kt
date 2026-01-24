package com.attributecore.event

import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.Configuration

object CoreConfig {

    @Config("config.yml")
    lateinit var conf: Configuration
        private set

    @ConfigNode("prefix")
    var prefix = "&8[&bAttributeCore&8] &f"

    @ConfigNode("combat.armor_k_value")
    var armorK = 400.0

    @ConfigNode("shield.auto_regen")
    var shieldAutoRegen = true

    @ConfigNode("shield.regen_speed")
    var shieldRegenSpeed = 0.05

    @ConfigNode("update.refresh_interval")
    var refreshInterval = 20L

    @ConfigNode("debug")
    var debug = false
}