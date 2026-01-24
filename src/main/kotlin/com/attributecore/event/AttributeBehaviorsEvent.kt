package com.attributecore.event

import com.attributecore.data.DamageData

/**
 * 属性行为处理器
 * 扩展更多预定义的行为模式
 */
object AttributeBehaviors {

    /**
     * 处理属性行为
     */
    fun handleBehavior(behavior: String, damageData: DamageData, value: Double) {
        when (behavior) {
            // 加伤
            "add_damage" -> {
                damageData.addDamage(value)
            }

            // 伤害百分比增加
            "multiply_damage" -> {
                val current = damageData.getFinalDamage()
                damageData.addDamage(current * (value / 100.0))
            }

            // 暴击
            "crit" -> {
                if (Math.random() < (value / 100.0)) {
                    damageData.addDamage(damageData.getFinalDamage())
                }
            }

            // 穿透
            "penetrate" -> {
                // 实现穿透逻辑
            }
        }
    }
}
