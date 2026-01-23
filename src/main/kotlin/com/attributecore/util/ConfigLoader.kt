package com.attributecore.util

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.info
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration

object ConfigLoader {

    /**
     * config.yml
     * 存放通用设置，比如: 是否开启全息显示、默认属性、消息提示等
     * autoReload = true: 当你在文件夹里修改文件保存后，插件会自动检测变化
     */
    @Config(value = "config.yml", autoReload = true)
    lateinit var conf: Configuration
        private set

    /**
     * attributes.yml
     * 存放我们在上一步定义的 "正则模板" 和 "属性映射表"
     */
    @Config(value = "attributes.yml", autoReload = true)
    lateinit var attributes: Configuration
        private set

    /**
     * 核心重载逻辑
     * @Awake(LifeCycle.ENABLE): 插件启动时自动执行一次
     */
    @Awake(LifeCycle.ENABLE)
    fun reload() {
        // 1. 确保文件内容是最新的 (从磁盘读取)
        conf.reload()
        attributes.reload()
        info("&8[&aAttributeCore&8] &7配置文件与属性规则已加载.")
    }
}