package com.attributecore

import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info
import com.attributecore.manager.AttributeManager
object AttributeCore : Plugin() {

    override fun onEnable() {
        info("Successfully running AttributeCore!")
        AttributeManager.init()
    }
}