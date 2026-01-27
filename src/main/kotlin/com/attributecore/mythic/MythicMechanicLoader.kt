package com.attributecore.mythic

import com.attributecore.util.DamageContext
import ink.ptms.um.event.MobSkillLoadEvent
import ink.ptms.um.skill.SkillMeta
import ink.ptms.um.skill.SkillResult
import ink.ptms.um.skill.type.EntityTargetSkill
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import taboolib.common.platform.event.SubscribeEvent

/**
 * 使用 Universal-Mythic 注册自定义技能
 * 适配 TabooLib UM 1.2.1+ 标准写法
 */
object MythicMechanicLoader {

    @SubscribeEvent
    fun onSkillLoad(event: MobSkillLoadEvent) {
        // 注册技能名: ad 或 attrdamage
        if (event.nameIs("ad", "attrdamage")) {

            // 使用官方推荐的匿名内部类写法
            event.register(object : EntityTargetSkill {

                // 1. 在类初始化时读取配置 (解析一次，多次执行)

                // 读取属性ID列表 (例如 attr=物理伤害,火元素攻击)
                val attributeKeys = event.config.getString(arrayOf("attr", "attribute"), "physical_damage")

                // 读取数值，支持 MM 占位符 (例如 v=<caster.level>*10)
                val valuePlaceholder = event.config.getPlaceholderDouble(arrayOf("val", "value", "v"), 0.0)

                // 读取是否清除原属性 (true 则只造成 val 设定的伤害)
                val clear = event.config.getBoolean(arrayOf("clear", "c"), false)

                // ✅ 新增：允许在 MM 配置中控制是否跳过所有属性计算 (默认为 false)
                val pure = event.config.getBoolean(arrayOf("pure", "p"), false)

                // 2. 实现技能释放逻辑
                override fun cast(meta: SkillMeta, entity: Entity): SkillResult {
                    // 获取施法者和受害者
                    val caster = meta.caster.entity as? LivingEntity ?: return SkillResult.INVALID_TARGET
                    val victim = entity as? LivingEntity ?: return SkillResult.INVALID_TARGET

                    // 解析数值 (UM 会自动处理占位符)
                    val finalValue = valuePlaceholder.get(meta.caster)

                    // 解析属性列表
                    val allowedAttrs = attributeKeys?.split(",")?.map { it.trim() }?.toSet()

                    try {
                        // ✅ 修复：正确调用 DamageContext.set 并闭合括号
                        // 参数: 允许的属性集, 基础值, 是否清除自身属性, 是否为纯净伤害(跳过计算)
                        DamageContext.set(
                            allowedAttributes = allowedAttrs,
                            baseValue = finalValue,
                            isClear = clear,
                            isPure = pure
                        )

                        // 发起伤害 (触发 EntityDamageByEntityEvent)
                        // 这里的 1.0 只是引子，DamageListener 会根据 Context 重新覆盖伤害
                        victim.damage(1.0, caster)

                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        return SkillResult.ERROR
                    } finally {
                        // 务必清理上下文，防止污染该线程后续的普通攻击
                        DamageContext.clear()
                    }

                    return SkillResult.SUCCESS
                }
            })
        }
    }
}