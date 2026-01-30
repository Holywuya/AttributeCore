package com.attributecore.data.attribute.sub.defence

import com.attributecore.data.attribute.AttributeType
import com.attributecore.data.attribute.SubAttribute
import com.attributecore.data.eventdata.EventData
import com.attributecore.data.eventdata.sub.DamageData
import org.bukkit.Particle
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.LivingEntity

class Block : SubAttribute("Block", 2, AttributeType.DEFENCE) {

    override fun defaultConfig(config: YamlConfiguration): YamlConfiguration {
        config.set("BlockRate.DiscernName", "格挡率")
        config.set("BlockRate.CombatPower", 8)
        config.set("BlockReduction.DiscernName", "格挡减免")
        config.set("BlockReduction.CombatPower", 3)
        config.set("Message.Block", "&e&l格挡!")
        return config
    }

    override fun eventMethod(values: DoubleArray, eventData: EventData) {
        if (eventData is DamageData) {
            val blockRate = values[0]
            val blockReduction = values[1] / 100
            
            if (probability(blockRate)) {
                eventData.sendHolo(getString("Message.Block"))
                val currentDamage = eventData.getDamage()
                eventData.setDamage(currentDamage * (1 - blockReduction))
                
                val defender = eventData.defender
                defender.world.spawnParticle(Particle.BLOCK_CRACK, defender.location.add(0.0, 1.0, 0.0), 10, defender.location.block.type)
            }
        }
    }

    override fun getPlaceholder(values: DoubleArray, entity: LivingEntity, placeholder: String): Any? {
        return when (placeholder) {
            "BlockRate" -> values[0]
            "BlockReduction" -> values[1]
            "Block" -> "${getDf().format(values[0])}% / ${getDf().format(values[1])}%"
            else -> null
        }
    }

    override fun getPlaceholders(): List<String> = listOf("BlockRate", "BlockReduction", "Block")

    override fun loadAttribute(values: DoubleArray, lore: String) {
        when {
            lore.contains(getString("BlockRate.DiscernName")) -> values[0] += getNumber(lore)
            lore.contains(getString("BlockReduction.DiscernName")) -> values[1] += getNumber(lore)
        }
    }

    override fun correct(values: DoubleArray) {
        values[0] = values[0].coerceIn(0.0, 100.0)
        values[1] = values[1].coerceIn(0.0, 100.0)
    }

    override fun calculationCombatPower(values: DoubleArray): Double {
        return values[0] * getInt("BlockRate.CombatPower") + values[1] * getInt("BlockReduction.CombatPower")
    }
}