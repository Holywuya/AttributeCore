package com.attributecore.api

import com.attributecore.data.AuraInstance
import com.attributecore.data.Element
import com.attributecore.data.ElementalAura
import org.bukkit.entity.LivingEntity
import java.util.function.BiConsumer

object ElementAPI {
    
    fun applyAura(entity: LivingEntity, element: Element, gauge: Double = 1.0) {
        ElementalAura.applyAura(entity, element, gauge)
    }

    fun applyAura(entity: LivingEntity, elementName: String, gauge: Double = 1.0) {
        val element = Element.fromConfigKey(elementName) ?: return
        ElementalAura.applyAura(entity, element, gauge)
    }

    fun getAura(entity: LivingEntity): AuraInstance? {
        return ElementalAura.getAura(entity)
    }

    fun getAuras(entity: LivingEntity): List<AuraInstance> {
        return ElementalAura.getAuras(entity)
    }

    fun hasAura(entity: LivingEntity, element: Element): Boolean {
        return ElementalAura.hasAura(entity, element)
    }

    fun hasAura(entity: LivingEntity, elementName: String): Boolean {
        val element = Element.fromConfigKey(elementName) ?: return false
        return ElementalAura.hasAura(entity, element)
    }

    fun consumeAura(entity: LivingEntity, element: Element, amount: Double = 1.0): Boolean {
        return ElementalAura.consumeAura(entity, element, amount)
    }

    fun consumeAura(entity: LivingEntity, elementName: String, amount: Double = 1.0): Boolean {
        val element = Element.fromConfigKey(elementName) ?: return false
        return ElementalAura.consumeAura(entity, element, amount)
    }

    fun clearAura(entity: LivingEntity, element: Element? = null) {
        ElementalAura.clearAura(entity, element)
    }

    fun clearAura(entity: LivingEntity, elementName: String?) {
        val element = elementName?.let { Element.fromConfigKey(it) }
        ElementalAura.clearAura(entity, element)
    }

    fun getElement(name: String): Element? {
        return Element.fromConfigKey(name)
    }

    fun getElements(): List<Element> {
        return Element.entries.toList()
    }

    fun getReactiveElements(): List<Element> {
        return Element.reactiveElements()
    }

    fun getActiveAuraCount(): Int {
        return ElementalAura.getActiveAuraCount()
    }

    fun getAffectedEntityCount(): Int {
        return ElementalAura.getAffectedEntityCount()
    }

    fun triggerReaction(
        attacker: LivingEntity,
        victim: LivingEntity,
        triggerElement: Element,
        callback: BiConsumer<Element, Element>?
    ): Boolean {
        val existingAura = ElementalAura.getAura(victim) ?: return false
        
        if (existingAura.element == triggerElement || triggerElement == Element.PHYSICAL) {
            return false
        }

        callback?.accept(existingAura.element, triggerElement)
        ElementalAura.consumeAura(victim, existingAura.element)
        return true
    }
}
