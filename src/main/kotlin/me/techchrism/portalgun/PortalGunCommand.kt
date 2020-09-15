package me.techchrism.portalgun

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

class PortalGunCommand : CommandExecutor, TabExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(args.size == 1 && args[0] == "get")
        {
            if(sender !is Player)
            {
                sender.sendMessage(ChatColor.RED.toString() + "You must be a player to run this command!")
                return true
            }
            sender.inventory.addItem(PortalGun.generatePortalGun())
            sender.sendMessage(ChatColor.GREEN.toString() + "Added a Portal Gun to your inventory")
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        return if(args.size <= 1) {
            mutableListOf("get")
        } else {
            mutableListOf()
        }
    }
}