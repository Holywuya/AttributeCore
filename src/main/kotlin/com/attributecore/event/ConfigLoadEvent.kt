package com.attributecore.event

import taboolib.common.platform.function.console
import taboolib.module.chat.colored
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.Configuration

/**
 * 全局配置文件映射类
 * 映射 plugins/AttributeCore/config.yml
 */
object CoreConfig {

    @Config("config.yml")
    lateinit var conf: Configuration
        private set

    // ================= [ 基础设置 ] =================

    @ConfigNode("prefix")
    val prefix = "&8[&bAttributeCore&8] &f"
        get() = field.colored() // 自动转换颜色代码

    // ================= [ 战斗设置 ] =================

    /** 护甲公式中的 K 值: K / (Defense + K) */
    @ConfigNode("combat.armor_k_value")
    var armorK = 400.0

    /** 默认基础暴击倍率 (1.0 = 100%伤害, 2.0 = 200%伤害) */
    @ConfigNode("combat.default_crit_multiplier")
    var defaultCritMultiplier = 2.0

    @ConfigNode("combat_power.default_weight")
    var cpDefaultWeight = 1.0

    // ================= [ 护盾设置 ] =================

    @ConfigNode("shield.auto_regen")
    var shieldAutoRegen = true

    /** 护盾每秒恢复百分比 (0.05 = 5%) */
    @ConfigNode("shield.regen_speed")
    var shieldRegenSpeed = 0.05

    // ================= [ 元素系统 ] =================

    /** 元素附着持续时间 (单位: Tick) */
    @ConfigNode("elements.aura_duration")
    var auraDuration = 100L

    /** 元素反应内置冷却 (单位: 毫秒) */
    @ConfigNode("elements.reaction_icd")
    var reactionIcd = 500L

    // ================= [ 系统设置 ] =================

    /** 玩家属性全局刷新频率 (单位: Tick) */
    @ConfigNode("update.refresh_interval")
    var refreshInterval = 20L

    @ConfigNode("debug")
    var debug = false

}