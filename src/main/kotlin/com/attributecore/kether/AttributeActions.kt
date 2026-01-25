package com.attributecore.kether

import com.attributecore.data.DamageData
import com.attributecore.manager.ShieldManager
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import taboolib.common5.Coerce
import taboolib.module.kether.*

/**
 * 属性系统 Kether 语句扩展
 * 简化版：移除了所有复杂的 Token 预读逻辑，确保编译通过
 */
object AttributeActions {

    @KetherParser(["ac"], namespace = "attributecore")
    fun parser() = scriptParser {
        it.switch {
            // === ac damage [add/mult] <value> ===
            case("damage") {
                it.switch {
                    case("add") {
                        val amount = it.nextAction<Double>()
                        actionNow {
                            val damageData = variables().get<Any>("damage_data").orElse(null) as? DamageData
                            run(amount).thenAccept { value ->
                                damageData?.addDamage(Coerce.toDouble(value))
                            }
                        }
                    }
                    case("mult") {
                        val amount = it.nextAction<Double>()
                        actionNow {
                            val damageData = variables().get<Any>("damage_data").orElse(null) as? DamageData
                            run(amount).thenAccept { value ->
                                damageData?.setDamageMultiplier(Coerce.toDouble(value))
                            }
                        }
                    }
                }
            }

            // === ac shield add <value> ===
            case("shield") {
                it.switch {
                    case("add") {
                        val amount = it.nextAction<Double>()
                        actionNow {
                            val damageData = variables().get<Any>("damage_data").orElse(null) as? DamageData
                            run(amount).thenAccept { value ->
                                // 默认尝试给防御者加盾，否则给脚本发送者
                                val player = (damageData?.defender as? Player) ?: script().sender?.castSafely<Player>()
                                player?.let { p ->
                                    ShieldManager.modifyShield(p.uniqueId, Coerce.toDouble(value))
                                }
                            }
                        }
                    }
                }
            }

            // === ac sound <name> ===
            case("sound") {
                val soundName = it.nextAction<String>()
                actionNow {
                    run(soundName).thenAccept { name ->
                        val player = script().sender?.castSafely<Player>()
                        player?.playSound(player.location, name.toString(), 1f, 1f)
                    }
                }
            }

            // === ac tell <msg> ===
            // 简化版：直接发给 sender
            case("tell") {
                val msg = it.nextAction<String>()
                actionNow {
                    run(msg).thenAccept { text ->
                        val content = text?.toString()?.replace("&", "§") ?: return@thenAccept
                        script().sender?.sendMessage(content)
                    }
                }
            }

            // === ac heal <target> <value> ===
            case("heal") {
                val targetAction = it.nextAction<LivingEntity>()
                val amountAction = it.nextAction<Double>()
                actionNow {
                    run(targetAction).thenCombine(run(amountAction)) { targetObj, amountObj ->
                        val target = targetObj as? LivingEntity ?: return@thenCombine
                        val amount = Coerce.toDouble(amountObj)
                        val maxHealth = target.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
                        target.health = (target.health + amount).coerceAtMost(maxHealth)
                    }
                }
            }
        }
    }
}