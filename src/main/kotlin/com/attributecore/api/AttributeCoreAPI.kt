package com.attributecore.api

import com.attributecore.data.AttributeData
import com.attributecore.manager.AttributeManager
import org.bukkit.entity.LivingEntity
import java.util.UUID

/**
 * 实体属性数据管理 API
 * 供其他插件调用，用于获取、添加、移除属性
 */
object AttributeCoreAPI {

    /**
     * 获取实体的当前最终属性数据 (包含装备和API加成)
     */
    @JvmStatic
    fun getEntityData(entity: LivingEntity): AttributeData {
        return AttributeManager.getData(entity)
    }

    /**
     * 获取指定插件/来源赋予实体的属性数据
     * @param source 来源标识 (建议使用插件主类或插件名)
     * @param uuid 实体的 UUID
     */
    @JvmStatic
    fun getEntityAPIData(source: Any, uuid: UUID): Map<String, Double>? {
        return AttributeManager.getApiData(uuid, getSourceName(source))
    }

    /**
     * 设置指定来源的实体属性数据
     * @param source 来源标识
     * @param uuid 实体的 UUID
     * @param key 属性ID
     * @param value 属性值
     */
    @JvmStatic
    fun setEntityAPIData(source: Any, uuid: UUID, key: String, value: Double) {
        AttributeManager.setApiAttribute(uuid, getSourceName(source), key, value)
    }

    /**
     * 移除指定来源的实体属性数据
     * @param source 来源标识
     * @param uuid 实体的 UUID
     */
    @JvmStatic
    fun removeEntityAPIData(source: Any, uuid: UUID) {
        AttributeManager.removeApiData(uuid, getSourceName(source))
    }

    /**
     * 移除指定插件/来源的所有数据
     * 通常在插件卸载 (onDisable) 时调用
     * @param source 来源标识
     */
    @JvmStatic
    fun removePluginAllEntityData(source: Any) {
        AttributeManager.clearSourceData(getSourceName(source))
    }

    /**
     * 强制刷新实体属性
     */
    @JvmStatic
    fun updateEntity(entity: LivingEntity) {
        AttributeManager.update(entity)
    }

    // 辅助：将对象转为 Source 字符串
    private fun getSourceName(source: Any): String {
        return when (source) {
            is String -> source
            is Class<*> -> source.simpleName
            else -> source.javaClass.simpleName
        }
    }
}