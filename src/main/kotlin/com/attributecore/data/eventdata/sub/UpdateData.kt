package com.attributecore.data.eventdata.sub

import com.attributecore.data.eventdata.EventData
import org.bukkit.entity.LivingEntity

class UpdateData(
    private val entity: LivingEntity
) : EventData {

    override fun getEntity(): LivingEntity = entity
}