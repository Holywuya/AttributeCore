package com.attributecore.data.eventdata

import org.bukkit.entity.LivingEntity

interface EventData {
    fun getEntity(): LivingEntity
}