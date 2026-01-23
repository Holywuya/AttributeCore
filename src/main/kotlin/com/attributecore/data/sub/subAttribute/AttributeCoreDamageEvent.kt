package com.attributecore.data.sub.subAttribute

import com.attributecore.data.DamageData
import com.attributecore.data.sub.SubAttribute
import taboolib.common.util.random

/**
 * 基础攻击力实现
 * 优先级设为 0 (最先计算)
 */
class AttributeDamage : SubAttribute(priority = 0) {
    override fun getName(): String = "物理伤害"

    override fun onAttack(data: DamageData) {
        val min = data.attackData.get("物理伤害")
        val max = data.attackData.get("物理伤害.max")

        if (min <= 0) return

        // 如果没有 max, 则取固定值 min
        val finalDamage = if (max > min) random(min, max) else min
        data.addDamage("Default", finalDamage)
    }

    override fun onDefend(data: DamageData) {

    }
}