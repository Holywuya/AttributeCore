package com.attributecore.command

import com.attributecore.data.SubAttribute
import com.attributecore.manager.AttributeManager
import com.attributecore.script.JsAttribute
import com.attributecore.script.JsAttributeLoader
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.submit

@CommandHeader(
    name = "attributecore",
    aliases = ["attrcore", "ac"],
    permission = "attributecore.admin"
)
object AttributeCoreCommand {

    @CommandBody(permission = "attributecore.reload")
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            submit(async = true) {
                JsAttributeLoader.reload()
                submit {
                    sender.sendMessage("§a[AttributeCore] JS 属性已重载")
                }
            }
        }
    }

    @CommandBody(permission = "attributecore.info")
    val info = subCommand {
        execute<Player> { sender, _, _ ->
            val data = AttributeManager.getEntityData(sender)
            sender.sendMessage("§6===== 你的属性 =====")
            data.getNonZeroAttributes().forEach { (name, value) ->
                sender.sendMessage("§e$name: §f$value")
            }
            sender.sendMessage("§6战斗力: §f${data.calculateCombatPower()}")
        }
    }

    @CommandBody(permission = "attributecore.list")
    val list = subCommand {
        execute<CommandSender> { sender, _, _ ->
            val allAttrs = SubAttribute.getAttributes()
            val jsAttrs = allAttrs.filterIsInstance<JsAttribute>()
            val kotlinAttrs = allAttrs.filter { it !is JsAttribute }

            sender.sendMessage("§6===== 属性列表 =====")
            
            if (kotlinAttrs.isNotEmpty()) {
                sender.sendMessage("§e[Kotlin 核心属性]")
                kotlinAttrs.sortedBy { it.priority }.forEach { attr ->
                    val types = attr.types.joinToString(", ") { it.name }
                    sender.sendMessage("§7  ${attr.name} §8(优先级: ${attr.priority}, 类型: $types)")
                }
            }
            
            if (jsAttrs.isNotEmpty()) {
                sender.sendMessage("§e[JS 自定义属性]")
                jsAttrs.sortedBy { it.priority }.forEach { attr ->
                    val types = attr.types.joinToString(", ") { it.name }
                    sender.sendMessage("§7  ${attr.name} §8(优先级: ${attr.priority}, 类型: $types)")
                }
            }
            
            sender.sendMessage("§6共 ${allAttrs.size} 个属性 (Kotlin: ${kotlinAttrs.size}, JS: ${jsAttrs.size})")
        }
    }

    @CommandBody
    val help = subCommand {
        execute<CommandSender> { sender, _, _ ->
            sender.sendMessage("§6===== AttributeCore 帮助 =====")
            sender.sendMessage("§e/ac reload §7- 重载 JS 属性")
            sender.sendMessage("§e/ac info §7- 查看自己的属性")
            sender.sendMessage("§e/ac list §7- 列出所有属性")
            sender.sendMessage("§e/ac help §7- 显示此帮助")
        }
    }
}
