package com.attributecore.event

import com.attributecore.data.DamageData
import com.attributecore.manager.ReactionManager
import com.attributecore.manager.ShieldManager
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.console
import taboolib.module.kether.KetherShell
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom

// ✅ 1. 将 typealias 移动到类外面，并增加 attrTags 参数用于识别当前属性的标签
private typealias AttackHandler = (DamageData, Double, List<String>) -> Unit
private typealias DefendHandler = (DamageData, Double, List<String>) -> Unit
private typealias UpdateHandler = (LivingEntity, Double) -> Unit

object AttributeBehaviors {

    private val attackBehaviors = ConcurrentHashMap<String, AttackHandler>()
    private val defendBehaviors = ConcurrentHashMap<String, DefendHandler>()
    private val updateBehaviors = ConcurrentHashMap<String, UpdateHandler>()

    fun init() {
        registerDefaults()
    }

    private fun registerDefaults() {
        // --- 注册原生攻击行为 (使用 _ 忽略不需要的标签参数) ---
        registerAttack("add_damage") { data, value, _ -> data.addDamage(value) }

        registerAttack("multiply_damage") { data, value, _ ->
            data.setDamageMultiplier(1.0 + (value / 100.0))
        }

        // --- Attack Behaviors ---

        // 暴击率 (改为调用 rollCrit)
        registerAttack("crit") { d, v, _ ->
            d.rollCrit(v)
        }

        // 暴击伤害
        registerAttack("crit_damage") { d, v, _ ->
            d.addCritDamage(v)
        }

        // --- Defend Behaviors ---

        // 抗暴率 (降低被暴击的几率)
        registerDefend("crit_resistance") { d, v, _ ->
            d.addCritResistance(v)
        }

        // 暴击减伤 (降低暴击触发时的伤害)
        registerDefend("crit_resilience") { d, v, _ ->
            d.addCritResilience(v)
        }

        registerAttack("penetrate_fixed") { data, value, _ ->
            data.addFixedPenetration(value)
        }

        registerAttack("penetrate_percent") { data, value, _ ->
            data.addPercentPenetration(value)
        }

        registerAttack("vampire") { data, value, _ ->
            val heal = data.getFinalDamage() * (value / 100.0)
            val p = data.attacker
            val maxHealthAttr = p.getAttribute(Attribute.GENERIC_MAX_HEALTH)
            if (maxHealthAttr != null) {
                p.health = (p.health + heal).coerceAtMost(maxHealthAttr.value)
            }
        }


        // --- 注册原生防御行为 ---
        registerDefend("defend") { data, value, _ -> data.addDefenseScore(value) }
        registerDefend("armor") { data, value, _ -> data.addDefenseScore(value) }
        registerDefend("reduce_percent") { data, value, _ -> data.addDirectReductionPercent(value) }
        registerDefend("dodge") { data, value, _ ->
            if (ThreadLocalRandom.current().nextDouble(100.0) < value) {
                data.setDamageMultiplier(0.0)
            }
        }

        // --- 注册原生更新行为 ---
        registerUpdate("max_health") { entity, value ->
            val attr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)
            if (attr != null) attr.baseValue = (20.0 + value).coerceAtLeast(1.0)
        }
        registerUpdate("move_speed") { entity, value ->
            if (entity is Player) entity.walkSpeed = (0.2f * (1 + value / 100.0)).toFloat().coerceIn(0.0f, 1.0f)
        }
        registerUpdate("max_shield") { entity, value -> ShieldManager.setMaxShield(entity.uniqueId, value) }
    }

    fun registerAttack(name: String, handler: AttackHandler) {
        attackBehaviors[name.lowercase()] = handler
    }

    fun registerDefend(name: String, handler: DefendHandler) {
        defendBehaviors[name.lowercase()] = handler
    }

    fun registerUpdate(name: String, handler: UpdateHandler) {
        updateBehaviors[name.lowercase()] = handler
    }

    // ================= 执行入口 =================

    fun handleAttack(behavior: String, damageData: DamageData, value: Double, attrTags: List<String>) {
        val key = behavior.lowercase()
        val handler = attackBehaviors[key]
        if (handler != null) {
            handler(damageData, value, attrTags)
        } else {
            val script = BehaviorLoader.scriptCache[key]
            if (script != null) {
                runKetherScript(script, damageData.attacker, mapOf(
                    "damage_data" to damageData,
                    "value" to value,
                    "attacker" to damageData.attacker,
                    "defender" to damageData.defender,
                    "tags" to attrTags
                ))
            }
        }
    }

    fun handleDefend(behavior: String, damageData: DamageData, value: Double, attrTags: List<String>) {
        val key = behavior.lowercase()
        val handler = defendBehaviors[key]
        if (handler != null) {
            handler(damageData, value, attrTags)
        } else {
            val script = BehaviorLoader.scriptCache[key]
            if (script != null) {
                runKetherScript(script, damageData.defender, mapOf(
                    "damage_data" to damageData,
                    "value" to value,
                    "attacker" to damageData.attacker,
                    "defender" to damageData.defender,
                    "tags" to attrTags
                ))
            }
        }
    }

    fun handleUpdate(entity: LivingEntity, key: String, behavior: String, value: Double) {
        if (updateBehaviors.containsKey(key.lowercase())) {
            updateBehaviors[key.lowercase()]?.invoke(entity, value)
            return
        }

        val handler = updateBehaviors[behavior.lowercase()]
        if (handler != null) {
            handler(entity, value)
        } else {
            val script = BehaviorLoader.scriptCache[behavior.lowercase()]
            if (script != null) {
                runKetherScript(script, entity, mapOf(
                    "entity" to entity,
                    "value" to value
                ))
            }
        }
    }

    private fun runKetherScript(scriptContent: String, sender: LivingEntity, vars: Map<String, Any>) {
        try {
            KetherShell.eval(
                scriptContent,
                sender = if (sender is Player) adaptPlayer(sender) else null,
                namespace = listOf("attributecore")
            ) {
                vars.forEach { (k, v) -> set(k, v) }
            }
        } catch (e: Exception) {
            console().sendMessage("§c[AttributeCore] 行为脚本执行出错: ${e.message}")
        }
    }
}