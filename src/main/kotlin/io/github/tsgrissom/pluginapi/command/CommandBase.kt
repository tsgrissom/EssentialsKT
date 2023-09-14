package io.github.tsgrissom.pluginapi.command

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

abstract class CommandBase : CommandExecutor, TabCompleter {

    fun getOnlinePlayerNamesToMutableList() : MutableList<String> =
        Bukkit.getOnlinePlayers()
            .map { it.name }
            .toMutableList()

    fun getWorldNamesToMutableList() : MutableList<String> =
        Bukkit.getWorlds()
            .map { it.name }
            .toMutableList()

    abstract fun execute(context: CommandContext)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        execute(CommandContext(sender, command, label, args))
        return true
    }
}