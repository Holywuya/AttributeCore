package com.attributecore.command

import com.attributecore.manager.AttributeManager
import org.bukkit.command.CommandSender
import taboolib.common.platform.command.*

@CommandHeader(name = "ac", permission = "ac.admin")
object CommandMain {

    @CommandBody
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            AttributeManager.reloadAttributes()
            sender.sendMessage("§a[AttributeCore] 属性重载成功！")
        }
    }
}