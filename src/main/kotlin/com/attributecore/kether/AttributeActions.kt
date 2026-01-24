package com.attributecore.kether

import com.attributecore.data.DamageData
import com.attributecore.manager.ShieldManager
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import taboolib.common5.Coerce
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * 属性系统 Kether 语句扩展
 * 命名空间: attributecore
 */
object AttributeActions {

    /**
     * 注册 ac 语句
     * 用法示例:
     * ac damage add &value
     * ac damage mult 1.5
     * ac shield add 100
     */
    @KetherParser(["ac"], namespace = "attributecore")
    fun parser() = scriptParser {
        it.switch {
            case("damage") {
                it.switch {
                    case("add") {
                        val amount = it.nextAction<Double>()
                        actionNow {
                            val damageData = variables().get<Any>("damage_data").orElse(null) as? DamageData
                            run(amount).thenAccept { value ->
                                // ✅ 使用 Coerce 转换为 Double
                                damageData?.addDamage(Coerce.toDouble(value))
                            }
                        }
                    }
                    case("mult") {
                        val amount = it.nextAction<Double>()
                        actionNow {
                            val damageData = variables().get<Any>("damage_data").orElse(null) as? DamageData
                            run(amount).thenAccept { value ->
                                // ✅ 使用 Coerce 转换为 Double
                                damageData?.setDamageMultiplier(Coerce.toDouble(value))
                            }
                        }
                    }
                }
            }

            case("shield") {
                it.switch {
                    case("add") {
                        val amount = it.nextAction<Double>()
                        actionNow {
                            val damageData = variables().get<Any>("damage_data").orElse(null) as? DamageData
                            run(amount).thenAccept { value ->
                                val player = (damageData?.defender as? Player)
                                    ?: script().sender?.castSafely<Player>()

                                player?.let { p ->
                                    // ✅ 使用 Coerce 转换为 Double
                                    ShieldManager.modifyShield(p.uniqueId, Coerce.toDouble(value))
                                }
                            }
                        }
                    }
                }
            }

            case("sound") {
                val soundName = it.nextAction<String>()
                actionNow {
                    run(soundName).thenAccept { name ->
                        val player = script().sender?.castSafely<Player>()
                        // ✅ 将 Any? 转换为 String
                        val sName = name?.toString() ?: return@thenAccept
                        player?.playSound(player.location, sName, 1f, 1f)
                    }
                }
            }

            case("tell") {
                val msg = it.nextAction<String>()
                actionNow {
                    run(msg).thenAccept { text ->
                        // ✅ 先将 Any? 转换为 String，再调用 replace
                        val message = text?.toString() ?: return@thenAccept
                        script().sender?.sendMessage(message.replace("&", "§"))
                    }
                }
            }

            case("heal") {
                val targetAction = it.nextAction<LivingEntity>()
                val amountAction = it.nextAction<Double>()
                actionNow {
                    // 异步运行获取目标和数值
                    run(targetAction).thenCombine(run(amountAction)) { target, amount ->
                        if (target is LivingEntity) {
                            val maxHealth = target.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
                            target.health = (target.health + Coerce.toDouble(amount)).coerceAtMost(maxHealth)
                        }
                    }
                }
            }
        }
    }
}