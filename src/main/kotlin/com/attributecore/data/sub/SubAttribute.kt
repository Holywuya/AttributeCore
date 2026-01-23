package com.attributecore.data.sub

import com.attributecore.data.DamageData

/**
 * 属性行为基类
 * 所有的具体属性（攻击力、防御力、暴击等）都继承此类
 */
abstract class SubAttribute(val priority: Int = 0) : Comparable<SubAttribute> {
    abstract fun getName(): String
    open fun onAttack(data: DamageData) {}
    open fun onDefend(data: DamageData) {}

    override fun compareTo(other: SubAttribute): Int = this.priority.compareTo(other.priority)
}