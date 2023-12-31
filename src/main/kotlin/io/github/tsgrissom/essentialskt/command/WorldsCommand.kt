package io.github.tsgrissom.essentialskt.command

import io.github.tsgrissom.essentialskt.EssentialsKTPlugin
import io.github.tsgrissom.essentialskt.config.ChatColorKey
import io.github.tsgrissom.essentialskt.gui.ListWorldsGui
import io.github.tsgrissom.pluginapi.command.CommandBase
import io.github.tsgrissom.pluginapi.command.CommandContext
import io.github.tsgrissom.pluginapi.command.flag.CommandFlagParser
import io.github.tsgrissom.pluginapi.command.flag.ValidCommandFlag
import io.github.tsgrissom.pluginapi.extension.bukkit.lacksPermission
import io.github.tsgrissom.pluginapi.extension.bukkit.sendChatComponents
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

class WorldsCommand : CommandBase() {

    private fun getPlugin() =
        EssentialsKTPlugin.instance ?: error("plugin instance is null")
    private fun getConfig() = getPlugin().getConfigManager()

    companion object {
        const val PERM = "essentials.world"
    }

    private fun generateWorldTextComponent(w: World) : TextComponent {
        val ccVal = getConfig().getBungeeChatColor(ChatColorKey.Value)

        val comp = TextComponent(w.name)
        comp.color = ccVal

        return comp
    }

    private fun getWorldsAsComponents() : Array<BaseComponent> {
        val conf = getConfig()
        val ccSec = conf.getBungeeChatColor(ChatColorKey.Primary)
        val ccTert = conf.getBungeeChatColor(ChatColorKey.Tertiary)

        val text = TextComponent("Worlds")
        text.color = ccSec;

        val delimiter = TextComponent(": ")
        delimiter.color = ccTert;

        text.addExtra(delimiter)

        val worlds = Bukkit.getWorlds()

        for ((i, world) in worlds.withIndex()) {
            text.addExtra(generateWorldTextComponent(world))
            if (i != (worlds.size - 1)) {
                val entryDelimiter = TextComponent(", ")
                entryDelimiter.color = ccSec
                text.addExtra(entryDelimiter)
            }
        }

        return ComponentBuilder(text).create()
    }

    private fun getWorldsAsPlainText() : String {
        var text = "Worlds: "
        val worlds = Bukkit.getWorlds()

        for ((i, world) in worlds.withIndex()) {
            text += world.name
            if (i != (worlds.size - 1))
                text += ", "
        }

        return text
    }

    override fun execute(context: CommandContext) {
        val args = context.args
        val sender = context.sender
        val flagGui = ValidCommandFlag.FLAG_GRAPHICAL
        val flags = CommandFlagParser(args, flagGui)

        if (sender.lacksPermission(PERM))
            return context.sendNoPermission(sender, PERM)

        if (sender is ConsoleCommandSender) {
            return sender.sendMessage(getWorldsAsPlainText())
        }

        if (sender is Player) {
            if (flags.wasPassed(flagGui))
                return ListWorldsGui().show(sender)

            return sender.sendChatComponents(getWorldsAsComponents())
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ) : MutableList<String> {
        val tab = mutableListOf<String>()

        if (sender.lacksPermission(PERM))
            return tab

        tab.addAll(listOf("--gui"))

        return tab.sorted().toMutableList()
    }
}