package com.attributecore.util

import com.attributecore.data.DamageData

/**
 * 伤害上下文管理器 (ThreadLocal)
 * 作用：在整个伤害计算流程中（从 MM 技能触发到监听器结算），在当前线程传递数据规则和活跃的 DamageData。
 */
object DamageContext {

    /**
     * 伤害上下文数据容器
     * @param allowedAttributes 允许触发的属性白名单 (null 代表全属性)
     * @param baseValue 技能/DOT 赋予的基础伤害值
     * @param isClear 是否清除攻击者自身装备属性
     * @param isPure 是否为纯净伤害 (跳过所有属性计算，直接应用 baseValue)
     * @param activeData 当前正在进行计算的 DamageData 实例 (用于支持 attacker.addDamage 写法)
     */
    data class Context(
        val allowedAttributes: Set<String>?,
        val baseValue: Double,
        val isClear: Boolean,
        val isPure: Boolean = false,
        var activeData: DamageData? = null
    )

    private val currentContext = ThreadLocal<Context?>()

    /**
     * 设置伤害计算规则
     * 通常由 MythicMobs 技能 (ad机制) 或 ac dot 任务在执行 victim.damage() 之前调用
     */
    fun set(allowedAttributes: Set<String>?, baseValue: Double, isClear: Boolean, isPure: Boolean = false) {
        val existing = currentContext.get()
        if (existing != null) {
            // 如果当前线程已存在 Context，保留 activeData，仅更新规则部分
            currentContext.set(existing.copy(
                allowedAttributes = allowedAttributes,
                baseValue = baseValue,
                isClear = isClear,
                isPure = isPure
            ))
        } else {
            currentContext.set(Context(allowedAttributes, baseValue, isClear, isPure))
        }
    }

    /**
     * 关联当前活跃的 DamageData 实例
     * 由 DamageListener 在 onDamage 事件最开始调用。
     * 这使得后续所有“拟人化”扩展函数（如 attacker.addDamage）能够找到操作目标。
     */
    fun setActiveData(data: DamageData) {
        val ctx = currentContext.get() ?: Context(null, 0.0, false, false)
        ctx.activeData = data
        currentContext.set(ctx)
    }

    /**
     * 获取当前完整的上下文
     */
    fun get(): Context? = currentContext.get()

    /**
     * ✅ 快捷获取当前活跃的 DamageData
     * 用于 Kotlin 扩展函数或 JS 包装器
     */
    fun getActiveData(): DamageData? = currentContext.get()?.activeData

    /**
     * 清理当前线程的上下文
     * 必须在 DamageListener 的 finally 块中调用，防止内存泄漏和线程污染
     */
    fun clear() {
        currentContext.remove()
    }
}