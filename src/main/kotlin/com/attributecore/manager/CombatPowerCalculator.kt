package com.attributecore.manager

import com.attributecore.data.attribute.BaseAttribute
import org.bukkit.entity.LivingEntity

object CombatPowerCalculator {

    fun calculate(entity: LivingEntity, attributes: List<BaseAttribute>): Double {
        val data = AttributeManager.getData(entity)
        var totalCp = 0.0
        attributes.forEach { attr ->
            val vals = data.get(attr.key)
            if (vals[0] != 0.0 || vals[1] != 0.0) {
                totalCp += (vals[0] + vals[1]) / 2.0 * attr.combatPower
            }
        }
        return totalCp
    }
}