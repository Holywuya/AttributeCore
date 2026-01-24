package com.attributecore.event

import com.attributecore.data.DamageData
import java.util.concurrent.ThreadLocalRandom

object AttributeBehaviors {

    fun handleAttack(behavior: String, damageData: DamageData, value: Double) {
        val random = ThreadLocalRandom.current()
        when (behavior.lowercase()) {
            "add_damage" -> damageData.addDamage(value)

            "multiply_damage" -> damageData.setDamageMultiplier(1.0 + (value / 100.0))

            "crit" -> {
                if (random.nextDouble(100.0) < value) {
                    damageData.setDamageMultiplier(2.0)
                    damageData.attacker.sendMessage("§6§l暴击！")
                }
            }

            "penetrate_percent" -> damageData.addPercentPenetration(value)

            "vampire" -> {
                val heal = damageData.getFinalDamage() * (value / 100.0)
                damageData.attacker.health = (damageData.attacker.health + heal).coerceAtMost(damageData.attacker.maxHealth)
            }
        }
    }

    fun handleDefend(behavior: String, damageData: DamageData, value: Double) {
        val random = ThreadLocalRandom.current()
        when (behavior.lowercase()) {
            "defend", "armor" -> damageData.addDefenseScore(value)

            "reduce_percent", "resistance" -> damageData.addDirectReductionPercent(value)

            "dodge" -> {
                if (random.nextDouble(100.0) < value) {
                    damageData.setDamageMultiplier(0.0)
                    damageData.defender.sendMessage("§f§l闪避！")
                }
            }
        }
    }
}