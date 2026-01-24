package com.attributecore

import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info

object AttributeCore : Plugin() {

    override fun onEnable() {
        info("§a[AttributeCore] 插件正在启用...")

        // 初始化属性管理器
        com.attributecore.manager.AttributeManager.init()

        info("§a[AttributeCore] 插件启用完成！")
    }

    override fun onDisable() {
        info("§c[AttributeCore] 插件已禁用！")
    }
}
