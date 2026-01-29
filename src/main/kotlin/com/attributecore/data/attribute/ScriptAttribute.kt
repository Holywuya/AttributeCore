package com.attributecore.data.attribute

import com.attributecore.event.CoreConfig
import com.attributecore.api.ScriptEntity
import com.attributecore.api.ScriptHandle
import com.attributecore.data.AttributeType
import com.attributecore.data.DamageData
import org.bukkit.entity.LivingEntity
import taboolib.common.platform.function.console
import taboolib.common.platform.function.submit
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
        this.tags.forEach { damageData.addTag(it) }
        this.element?.let { el ->
            com.attributecore.manager.ReactionManager.handleElement(damageData.defender, el, damageData)
        }
        
        console().sendMessage("§e[AC-DEBUG] §f调用脚本: key=$key, value=$value")
        console().sendMessage("§e[AC-DEBUG] §f参数: attr=$this, attacker=${damageData.attacker.name}, entity=${damageData.defender.name}")
        
        try {
            val result = script.invokeFunction(
                "runAttack",
                this,
                ScriptEntity(damageData.attacker),
                ScriptEntity(damageData.defender),
                ScriptHandle(damageData, value)
            )
            console().sendMessage("§a[AC-DEBUG] §f脚本执行成功: key=$key, 返回值=$result")
        } catch (e: NoSuchMethodException) {
            console().sendMessage("§c[AC-DEBUG] §f脚本 $key 没有 runAttack 函数")
        } catch (e: Exception) {
            console().sendMessage("§c[AttributeCore] 属性脚本 $key 执行 runAttack 时出错: ${e.message}")
            console().sendMessage("§c[AC-DEBUG] §f异常类型: ${e.javaClass.name}")
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