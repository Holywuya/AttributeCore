package com.attributecore.api

import com.attributecore.data.AuraInstance
import com.attributecore.data.Elements
import com.attributecore.data.ElementalAura
import org.bukkit.entity.LivingEntity
import java.util.function.BiConsumer

object ElementAPI {
    
    @JvmStatic
    fun applyAura(entity: LivingEntity, element: String, gauge: Double = 1.0) {
        ElementalAura.applyAura(entity, element, gauge)
    }

    @JvmStatic
    fun getAura(entity: LivingEntity): AuraInstance? {
        return ElementalAura.getAura(entity)
    }

    @JvmStatic
    fun getAuras(entity: LivingEntity): List<AuraInstance> {
        return ElementalAura.getAuras(entity)
    }

    @JvmStatic
    fun hasAura(entity: LivingEntity, element: String): Boolean {
        return ElementalAura.hasAura(entity, element)
    }

    @JvmStatic
    fun consumeAura(entity: LivingEntity, element: String, amount: Double = 1.0): Boolean {
        return ElementalAura.consumeAura(entity, element, amount)
    }

    @JvmStatic
    fun clearAura(entity: LivingEntity, element: String? = null) {
        ElementalAura.clearAura(entity, element)
    }

    @JvmStatic
    fun getDisplayName(element: String): String {
        return Elements.getDisplayName(element)
    }

    @JvmStatic
    fun getColoredName(element: String): String {
        return Elements.getColoredName(element)
    }

    @JvmStatic
    fun isPhysical(element: String): Boolean {
        return Elements.isPhysical(element)
    }

    @JvmStatic
    fun isReactive(element: String): Boolean {
        return Elements.isReactive(element)
    }

    @JvmStatic
    fun getActiveAuraCount(): Int {
        return ElementalAura.getActiveAuraCount()
    }

    @JvmStatic
    fun getAffectedEntityCount(): Int {
        return ElementalAura.getAffectedEntityCount()
    }

    @JvmStatic
    fun triggerReaction(
        attacker: LivingEntity,
        victim: LivingEntity,
        triggerElement: String,
        callback: BiConsumer<String, String>?
    ): Boolean {
        val existingAura = ElementalAura.getAura(victim) ?: return false
        
        if (existingAura.element == Elements.normalize(triggerElement) || Elements.isPhysical(triggerElement)) {
            return false
        }

        callback?.accept(existingAura.element, triggerElement)
        ElementalAura.consumeAura(victim, existingAura.element)
        return true
    }
}
