package com.attributecore.data.attribute

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class PreLoadItem(
    val type: EquipmentType,
    val item: org.bukkit.inventory.ItemStack
)

enum class EquipmentType {
    MAIN_HAND, OFF_HAND, HELMET, CHESTPLATE, LEGGINGS, BOOTS, RPG_INVENTORY, SLOT, EQUIPMENT
}

class SXAttributeData {

    companion object {
        fun createEmpty(): SXAttributeData {
            return SXAttributeData()
        }
    }

    var combatPower: Double = 0.0
        private set

    private val values: Array<DoubleArray>

    init {
        val attrs = SubAttribute.getAttributes()
        values = Array(attrs.size) { index ->
            val attr = attrs.getOrNull(index)
            DoubleArray(attr?.length ?: 1)
        }
    }

    constructor() {
        val size = SubAttribute.getAttributes().size
    }

    fun getValues(attributeName: String): DoubleArray {
        return getValues(SubAttribute.getSubAttribute(attributeName))
    }

    fun getValues(attribute: SubAttribute?): DoubleArray {
        return attribute?.let { values.getOrNull(it.priority) } ?: DoubleArray(12)
    }

    fun getAllValues(): Array<DoubleArray> = values

    fun isValid(): Boolean {
        return values.any { arr -> arr.any { it != 0.0 } }
    }

    fun isValid(attribute: SubAttribute?): Boolean {
        if (attribute == null) return false
        val arr = getValues(attribute)
        return arr.any { it != 0.0 }
    }

    fun add(other: SXAttributeData?): SXAttributeData {
        if (other != null && other.isValid()) {
            for (i in values.indices) {
                val otherValues = other.values.getOrNull(i) ?: continue
                for (j in values[i].indices) {
                    if (j < otherValues.size) {
                        values[i][j] += otherValues[j]
                    }
                }
            }
        }
        return this
    }

    fun take(other: SXAttributeData?): SXAttributeData {
        if (other != null && other.isValid()) {
            for (i in values.indices) {
                val otherValues = other.values.getOrNull(i) ?: continue
                for (j in values[i].indices) {
                    if (j < otherValues.size) {
                        values[i][j] -= otherValues[j]
                    }
                }
            }
        }
        return this
    }

    fun calculationCombatPower(): Double {
        this.combatPower = 0.0
        val attrs = SubAttribute.getAttributes()
        for (attr in attrs) {
            val attrValues = getValues(attr)
            this.combatPower += attr.calculationCombatPower(attrValues)
        }
        return this.combatPower
    }

    fun correct() {
        val attrs = SubAttribute.getAttributes()
        for (attr in attrs) {
            val attrValues = getValues(attr)
            attr.correct(attrValues)
        }
    }

    override fun toString(): String {
        val valid = isValid()
        return "SXAttributeData(combatPower=$combatPower, valid=$valid)"
    }
}