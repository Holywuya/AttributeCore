package com.attributecore.command

import com.attributecore.manager.AttributeManager
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand

/**
 * AttributeCore 主命令系统
 * 参考 TabooLib 命令文档[48][54]
 */
@CommandHeader(
    name = "attribute",
    aliases = ["ac", "attributes", "属性"],
    description = "属性系统核心命令",
    permission = "attribute.use",
    newParser = true  // 启用新一代命令解析器[54]
)
object AttributeCommand {

    @CommandBody
    val mainCommand = mainCommand {
        execute<Player> { sender, _, _ ->
            sender.sendMessage("§d[§9AttributeCore§d] §f属性系统")
            sendHelp(sender)
        }
    }

    /** 帮助命令[67][69] */
    @CommandBody
    val help = subCommand {
        execute<Player> { sender, _, _ ->
            sendHelp(sender)
        }
    }

    /** 重载命令[62][65][67] */
    @CommandBody
    val reload = subCommand {
        execute<Player> { _, _, _ ->
            AttributeManager.reloadAttributes()
        }
    }

    /** 查看属性命令[63] */
    @CommandBody
    val stats = subCommand {
        dynamic(optional = true) {  // 动态玩家参数
            suggestion<Player> { sender, _ ->
                // 建议所有在线玩家[55]
                org.bukkit.Bukkit.getOnlinePlayers().map { it.name }
            }
            execute<Player> { sender, context, argument ->
                val targetName = context.argument(-1)
                val target = if (targetName.isEmpty()) {
                    sender
                } else {
                    org.bukkit.Bukkit.getPlayer(targetName) ?: run {
                        sender.sendMessage("§c玩家不在线: $targetName")
                        return@execute
                    }
                }

                val data = AttributeManager.getData(target)
                sender.sendMessage("§a--- ${target.name} 的属性 ---")
                if (data.values.isEmpty()) {
                    sender.sendMessage("§7暂无属性")
                } else {
                    data.values.forEach { (key, value) ->
                        sender.sendMessage("§7$key: §f$value[0]")
                    }
                }
            }
        }
        // 不指定参数时查看自己
        execute<Player> { sender, _, _ ->
            val data = AttributeManager.getData(sender)
            sender.sendMessage("§a--- 你的属性 ---")
            data.values.forEach { (k, v) ->
                sender.sendMessage("§7$k: §f$v")
            }
        }
    }

    /** Debug模式[67][71] */
    @CommandBody
    val debug = subCommand {
        execute<Player> { sender, _, _ ->
            // 切换调试模式
        }
    }

    /** 清空缓存命令[68][70] */
    @CommandBody
    val clear = subCommand {
        execute<Player> { sender, _, _ ->
            AttributeManager.clearCache()
        }
    }

    private fun sendHelp(player: Player) {
        player.sendMessage("§f&m─§6&m──────────────────────────────§6&f&m─")
        player.sendMessage(" §9指令列表:")
        player.sendMessage(" §d- §e/ac help §5—— §a显示帮助")
        player.sendMessage(" §d- §e/ac reload §5—— §a重载配置")
        player.sendMessage(" §d- §e/ac stats [玩家] §5—— §a查看属性")
        player.sendMessage(" §d- §e/ac clear §5—— §a清空缓存")
        player.sendMessage(" §d- §e/ac debug §5—— §a调试模式")
        player.sendMessage("§f&m─§6&m──────────────────────────────§6&f&m─")
    }
}
