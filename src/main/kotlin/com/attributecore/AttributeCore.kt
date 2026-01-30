package com.attributecore

import com.attributecore.attribute.*
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration

object AttributeCore : Plugin() {
    @Config
    lateinit var config: Configuration
        private set

    override fun onEnable() {
        info("AttributeCore 已启动 - 版本 1.0.0.1")
        info("基于 SX-Attribute 3.x 架构重构")

        AttackDamage()
        Defense()
        CritChance()
        CritDamage()

        info("已加载 4 个核心属性")
    }

    override fun onDisable() {
        info("AttributeCore 已关闭")
    }
}
