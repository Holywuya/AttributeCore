package com.attributecore.data.attribute

import com.attributecore.event.CoreConfig
import com.attributecore.api.ScriptEntity
import com.attributecore.api.ScriptHandle
import com.attributecore.data.AttributeType
import com.attributecore.data.DamageData
import org.bukkit.entity.LivingEntity
import taboolib.common.platform.function.console
import javax.script.Invocable

/**
 * 脚本属性
 * 由 JS 脚本动态定义，直接调用脚本函数执行逻辑
 */
class ScriptAttribute(
    val script: Invocable, // 脚本引擎实例
    key: String,
    names: List<String>,
    type: AttributeType,
    priority: Int,
    tags: List<String>,
    element: String?,
    combatPower: Double,
    // ✅ 新增：接收处理过颜色代码的显示名称
    private val displayName: String
) : BaseAttribute(key, names, type, priority, tags, element, combatPower) {

    override fun getDisplayName(): String {
        return displayName
    }

    override fun onAttack(damageData: DamageData, value: Double, extraValue: Double) {
        // 1. 自动注入元素标签和反应逻辑
        this.tags.forEach { damageData.addTag(it) }
        this.element?.let { el ->
            com.attributecore.manager.ReactionManager.handleElement(damageData.defender, el, damageData)
        }

        // 2. 调用脚本函数 runAttack
        try {
            script.invokeFunction(
                "runAttack",
                this, // attr
                ScriptEntity(damageData.attacker), // attacker (包装)
                ScriptEntity(damageData.defender), // entity (包装)
                ScriptHandle(damageData, value)    // handle
            )
        } catch (e: NoSuchMethodException) {
            // 脚本没写 runAttack，属于正常情况，忽略
        } catch (e: Exception) {
            console().sendMessage("§c[AttributeCore] 属性脚本 $key 执行 runAttack 时出错: ${e.message}")
            if (CoreConfig.debug) e.printStackTrace()
        }
    }

    override fun onDefend(damageData: DamageData, value: Double, extraValue: Double) {
        if (this.tags.isNotEmpty() && this.tags.none { damageData.hasTag(it) }) return

        try {
            script.invokeFunction(
                "runDefend",
                this,
                ScriptEntity(damageData.attacker),
                ScriptEntity(damageData.defender),
                ScriptHandle(damageData, value)
            )
        } catch (e: NoSuchMethodException) {
            // 忽略
        } catch (e: Exception) {
            console().sendMessage("§c[AttributeCore] 属性脚本 $key 执行 runDefend 时出错: ${e.message}")
            if (CoreConfig.debug) e.printStackTrace()
        }
    }

    override fun onUpdate(entity: LivingEntity, value: Double) {
        try {
            script.invokeFunction(
                "runUpdate",
                this,
                ScriptEntity(entity),
                value,
                ScriptHandle(null, value)
            )
        } catch (e: NoSuchMethodException) {
            // 忽略
        } catch (e: Exception) {
            console().sendMessage("§c[AttributeCore] 属性脚本 $key 执行 runUpdate 时出错: ${e.message}")
            if (CoreConfig.debug) e.printStackTrace()
        }
    }
}