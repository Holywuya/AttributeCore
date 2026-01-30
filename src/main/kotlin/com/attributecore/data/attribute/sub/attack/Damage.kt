package com.attributecore.data.attribute.sub.attack

import com.attributecore.data.attribute.AttributeType
import com.attributecore.data.attribute.SubAttribute
import com.attributecore.data.eventdata.EventData
import com.attributecore.data.eventdata.sub.DamageData
import com.attributecore.data.eventdata.sub.UpdateData
import org.bukkit.attribute.Attribute
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent

class Damage : SubAttribute("Damage", 6, AttributeType.ATTACK, AttributeType.UPDATE) {

    companion object {
        const val TYPE_DEFAULT = 0
        const val TYPE_PVP = 1
        const val TYPE_PVE = 2
    }

    override fun defaultConfig(config: YamlConfiguration): YamlConfiguration {
        config.set("Damage.DiscernName", "攻击力")
        config.set("Damage.CombatPower", 1)
        config.set("PVPDamage.DiscernName", "PVP攻击力")
        config.set("PVPDamage.CombatPower", 1)
        config.set("PVEDamage.DiscernName", "PVE攻击力")
        config.set("PVEDamage.CombatPower", 1)
        config.set("Message.Holo", "&c&o伤害: &b&o{0}")
        return config
    }

    override fun eventMethod(values: DoubleArray, eventData: EventData) {
        when (eventData) {
            is DamageData -> {
                val damageData = eventData
                val event = damageData.event
                
                val defaultDamage = getAttribute(values, TYPE_DEFAULT)
                damageData.addDamage(defaultDamage)
                
                val targetType = if (event.entity is Player) TYPE_PVP else TYPE_PVE
                damageData.addDamage(getAttribute(values, targetType))
            }
            is UpdateData -> {
                if (eventData.getEntity() is Player) {
                    val player = eventData.getEntity() as Player
                    player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.baseValue = values[0].coerceAtLeast(1.0)
                }
            }
        }
    }

    override fun getPlaceholder(values: DoubleArray, entity: LivingEntity, placeholder: String): Any? {
        return when (placeholder) {
            "MinDamage" -> values[0]
            "MaxDamage" -> values[1]
            "Damage" -> if (values[0] == values[1]) values[0] else "${getDf().format(values[0])} - ${getDf().format(values[1])}"
            "PvpMinDamage" -> values[2]
            "PvpMaxDamage" -> values[3]
            "PvpDamage" -> if (values[2] == values[3]) values[2] else "${getDf().format(values[2])} - ${getDf().format(values[3])}"
            "PveMinDamage" -> values[4]
            "PveMaxDamage" -> values[5]
            "PveDamage" -> if (values[4] == values[5]) values[4] else "${getDf().format(values[4])} - ${getDf().format(values[5])}"
            else -> null
        }
    }

    override fun getPlaceholders(): List<String> = listOf(
        "MinDamage", "MaxDamage", "Damage",
        "PvpMinDamage", "PvpMaxDamage", "PvpDamage",
        "PveMinDamage", "PveMaxDamage", "PveDamage"
    )

    override fun loadAttribute(values: DoubleArray, lore: String) {
        val loreSplit = lore.split("-")
        when {
            lore.contains(getString("PVEDamage.DiscernName")) -> {
                values[4] += getNumber(loreSplit[0])
                values[5] += getNumber(loreSplit.getOrElse(1) { loreSplit[0] })
            }
            lore.contains(getString("PVPDamage.DiscernName")) -> {
                values[2] += getNumber(loreSplit[0])
                values[3] += getNumber(loreSplit.getOrElse(1) { loreSplit[0] })
            }
            lore.contains(getString("Damage.DiscernName")) -> {
                values[0] += getNumber(loreSplit[0])
                values[1] += getNumber(loreSplit.getOrElse(1) { loreSplit[0] })
            }
        }
    }

    override fun correct(values: DoubleArray) {
        values[0] = values[0].coerceIn(1.0, 2048.0)
        values[1] = values[1].coerceAtLeast(values[0]).coerceAtMost(2048.0)
        values[2] = values[2].coerceAtLeast(0.0)
        values[3] = values[3].coerceAtLeast(values[2])
        values[4] = values[4].coerceAtLeast(0.0)
        values[5] = values[5].coerceAtLeast(values[4])
    }

    override fun calculationCombatPower(values: DoubleArray): Double {
        var cp = 0.0
        cp += (values[0] + values[1]) / 2 * getInt("Damage.CombatPower")
        cp += (values[2] + values[3]) / 2 * getInt("PVPDamage.CombatPower")
        cp += (values[4] + values[5]) / 2 * getInt("PVEDamage.CombatPower")
        return cp
    }

    private fun getAttribute(values: DoubleArray, type: Int): Double {
        val min = values[type * 2]
        val max = values[type * 2 + 1]
        return if (max > min) min + java.util.Random().nextDouble() * (max - min) else min
    }
}