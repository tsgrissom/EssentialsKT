package io.github.tsgrissom.essentialskt.command

import io.github.tsgrissom.essentialskt.EssentialsKTPlugin
import io.github.tsgrissom.essentialskt.config.ChatColorKey
import io.github.tsgrissom.pluginapi.command.CommandBase
import io.github.tsgrissom.pluginapi.command.CommandContext
import io.github.tsgrissom.pluginapi.command.help.CommandUsageBuilder
import io.github.tsgrissom.pluginapi.command.help.SubcParameterBuilder
import io.github.tsgrissom.pluginapi.extension.kt.equalsIc
import io.github.tsgrissom.pluginapi.extension.bukkit.lacksPermission
import io.github.tsgrissom.pluginapi.extension.bukkit.sendChatComponents
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil

class ClearChatCommand : CommandBase() {

    // MARK: Dependency Injection
    private fun getPlugin() =
        EssentialsKTPlugin.instance ?: error("plugin instance is null")
    private fun getConfig() = getPlugin().getConfigManager()
    private fun getConfiguredRepeatCount() = getConfig().getClearChatRepeatBlankLineCount()

    // MARK: Static Constants
    companion object {
        const val PERM_SELF   = "essentialskt.clearchat"
        const val PERM_ALL    = "essentialskt.clearchat.all"
        const val PERM_OTHERS = "essentialskt.clearchat.others"
        const val PERM_EXEMPT = "essentialskt.clearchat.exemptall"
    }

    private fun performClearChat(target: Player, count: Int = getConfiguredRepeatCount()) =
        repeat(count) { target.sendMessage("") }

    override fun execute(context: CommandContext) {
        val args = context.args

        if (args.isEmpty())
            return handleEmptyArgs(context)

        val sub = args[0]

        if (sub.equalsIc("all"))
            return handleSubcAll(context)

        handleOneOrMoreArgs(context)
    }

    private fun handleEmptyArgs(context: CommandContext) {
        val sender = context.sender

        if (sender.lacksPermission(PERM_SELF))
            return context.sendNoPermission(sender, PERM_SELF)

        val usage = CommandUsageBuilder(context)
            .withConsoleParameter(
                SubcParameterBuilder("Target OR \"all\"", required=true)
            )
            .toComponents()

        if (sender is ConsoleCommandSender)
            return sender.sendChatComponents(usage)
        else if (sender !is Player)
            return

        performClearChat(sender)
    }

    private fun handleSubcAll(context: CommandContext) {
        val sender = context.sender
        val ccPrimary = getConfig().getChatColor(ChatColorKey.Primary)

        if (sender.lacksPermission(PERM_ALL))
            return context.sendNoPermission(sender, PERM_ALL)

        Bukkit.getOnlinePlayers()
            .filter { it.lacksPermission(PERM_EXEMPT) }
            .forEach { performClearChat(it, getConfiguredRepeatCount()) }
        sender.sendMessage("${ccPrimary}You cleared the chat messages of all players on the server.")
    }

    private fun handleOneOrMoreArgs(context: CommandContext) {
        val args = context.args
        val sender = context.sender
        val ccErr = getConfig().getChatColor(ChatColorKey.Error)
        val ccErrDetl = getConfig().getChatColor(ChatColorKey.ErrorDetail)
        val ccPrimary = getConfig().getChatColor(ChatColorKey.Primary)

        val clearedPlayers = mutableListOf<String>()

        for (i in 0..args.size) {
            val arg = args[i]
            val t: Player = Bukkit.getPlayer(arg)
                ?: return sender.sendMessage("${ccErr}Could not find player ${ccErrDetl}\"$arg\"${ccErr}.")
            val tn = t.name
            if (clearedPlayers.contains(tn)) {
                sender.sendMessage("${ccErr}You already cleared ${ccErrDetl}${tn}'s ${ccErr}chat.")
                continue
            }
            if (t == sender && sender.lacksPermission(PERM_SELF)) {
                context.sendNoPermission(sender, PERM_SELF)
                continue
            }
            if (t != sender && sender.lacksPermission(PERM_OTHERS)) {
                context.sendNoPermission(sender, PERM_OTHERS)
                continue
            }

            performClearChat(t)
            clearedPlayers.add(tn)
        }

        var self = false
        val howMany = clearedPlayers.size

        if (howMany == 0)
            return

        val who = if (howMany == 1) {
            val first = clearedPlayers[0]
            if (first == sender.name)
                self = true
            "${first}'s"
        } else {
            "$howMany players"
        }

        if (!self)
            sender.sendMessage("${ccPrimary}You cleared ${ccErrDetl}${who} ${ccPrimary}chat messages.")
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ) : MutableList<String> {
        val tab = mutableListOf<String>()
        val suggestPlayers =
            if (sender.hasPermission(PERM_ALL)) mutableListOf("all")
            else mutableListOf()

        if (sender.hasPermission(PERM_ALL))
            suggestPlayers.addAll(getOnlinePlayerNamesToMutableList())

        val len = args.size

        if (len > 0) {
            for (i in 0..len) {
                val arg = args[i]
                StringUtil.copyPartialMatches(arg, suggestPlayers, tab)
            }
        }

        return tab.sorted().toMutableList()
    }
}