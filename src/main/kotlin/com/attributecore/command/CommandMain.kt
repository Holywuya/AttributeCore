package com.attributecore.command

import com.attributecore.manager.AttributeManager
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.module.nms.getItemTag
import taboolib.platform.util.isAir

@CommandHeader(name = "ac", permission = "ac.admin")
object CommandMain {

    @CommandBody
    val main = mainCommand {
        execute<Player> { sender, _, _ ->
            val data = AttributeManager.getEntityData(sender)
            sender.sendMessage("§a--- 你的当前属性 ---")
            data.values.forEach { (k, v) -> sender.sendMessage("§7$k: §f$v") }
        }
    }

    // 设置 NBT 工具: /ac set <属性名> <字符串>
    @CommandBody
    val set = subCommand {
        dynamic { // 属性名
            dynamic { // 字符串
                execute<Player> { sender, context, argument ->
                    val item = sender.inventory.itemInMainHand
                    if (item.isAir()) return@execute
                    val name = context.argument(-1)
                    val tag = item.getItemTag()
                    tag.putDeep("AttributeCore.$name", argument)
                    tag.saveTo(item)
                    sender.sendMessage("§a已设置 NBT: $name = $argument")
                }
            }
        }
    }
}