package io.github.tsgrissom.essentialskt.command

import io.github.tsgrissom.essentialskt.EssentialsKTPlugin
import io.github.tsgrissom.essentialskt.enum.ChatColorKey
import io.github.tsgrissom.essentialskt.gui.ConfigureChatColorGui
import io.github.tsgrissom.pluginapi.command.CommandBase
import io.github.tsgrissom.pluginapi.command.CommandContext
import io.github.tsgrissom.pluginapi.command.flag.CommandFlagParser
import io.github.tsgrissom.pluginapi.command.flag.ValidCommandFlag
import io.github.tsgrissom.pluginapi.command.help.CommandHelpGenerator
import io.github.tsgrissom.pluginapi.command.help.SubcommandHelp
import io.github.tsgrissom.pluginapi.extension.capitalizeEachWordAllCaps
import io.github.tsgrissom.pluginapi.extension.equalsIc
import io.github.tsgrissom.pluginapi.extension.lacksPermission
import io.github.tsgrissom.pluginapi.extension.sendChatComponents
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil

class EssKtCommand : CommandBase() {

    private fun getPlugin() : EssentialsKTPlugin =
        EssentialsKTPlugin.instance ?: error("Plugin not instantiated!")
    private fun getConfig() =
        getPlugin().getConfigManager()

    companion object {
        const val PERM = "essentialskt.essentialskt"
    }

    private fun getHelpAsComponents(context: CommandContext) : Array<BaseComponent> {
        val help = CommandHelpGenerator(context)

        help.withSubcommand(
            SubcommandHelp
                .compose("version")
                .withAliases("v")
                .withDescription("View plugin version")
        )

        return help.toComponents()
    }

    private fun handleSubcVersion(context: CommandContext) {
        val version = getPlugin().description.version
        context.sender.sendMessage("EssentialsKT version: $version")
    }

    private fun handleSubcReload(context: CommandContext) {
        val sender = context.sender
        val ccPrimary = getConfig().getChatColor(ChatColorKey.Primary)
        sender.sendMessage("${ccPrimary}In progress")
        // TODO Reload command
    }

    private fun handleSubcConfig(context: CommandContext) {
        val conf = getConfig()
        val ccErr = conf.getChatColor(ChatColorKey.Error)
        val ccErrDetl = conf.getChatColor(ChatColorKey.ErrorDetail)
        val ccPrim = conf.getChatColor(ChatColorKey.Primary)
        val ccSec = conf.getChatColor(ChatColorKey.Secondary)
        val ccVal = conf.getChatColor(ChatColorKey.Value)
        val ccReset = ChatColor.RESET

        val args = context.args
        val sender = context.sender
        val len = args.size

        fun listColorKeys() {
            val colors = ChatColorKey.entries
            var list = "Configurable Colors: "

            for ((i, color) in colors.withIndex()) {
                val cc = getConfig().getChatColor(color)
                val name = "$cc${color.name}${ccReset}"
                list += name
                if (i != (colors.size - 1)) {
                    list += ", "
                }
            }

            sender.sendMessage(list)
        }

        fun listConfiguredColors() {
            val colors = ChatColorKey.entries
            sender.sendMessage("Color Configuration:")

            for (color in colors) {
                val key = color.name
                val value = getConfig().getChatColor(color)
                sender.sendMessage(" - $value$key Color")
            }
        }

        fun listChatColors() {
            sender.sendMessage("Valid Chat Colors:")

            for (c in ChatColor.entries.filter { !it.isFormat && it != ChatColor.RESET }) {
                val cc = c.toString()
                val name = c.name.capitalizeEachWordAllCaps()
                sender.sendMessage(" - $cc$name$ccReset = \"${name.replace(" ", "_")}\"")
            }
        }

        if (len == 1) {
            sender.sendMessage("${ccSec}Options: ${ccVal}colors")

            return
        }

        val arg1 = args[1]

        if (arg1.equalsIc("colors", "color")) {
            if (len == 2)
                return listConfiguredColors()

            val arg2 = args[2]

            if (arg2.equalsIc("alter", "change", "set", "configure")) {
                if (len == 3) {
                    sender.sendMessage("${ccErr}Usage: ${ccErrDetl}/esskt conf color set <Key>")
                    listColorKeys()
                    return
                }

                val arg3 = args[3]
                val textUnknown = "${ccErr}Unknown color key: ${ccErrDetl}\"$arg3\"${ccErr}. Do ${ccErrDetl}/esskt conf colors ${ccErr}to view valid keys."
                val configurableColor = conf.fetchColorByKey(arg3)
                    ?: return sender.sendMessage(textUnknown) // Ensure color key is valid
                val key = configurableColor.name

                if (len == 4) { // Ex: "/esskt conf color set Detail"
                    // TODO Open gui

                    if (sender is Player) {
                        ConfigureChatColorGui(configurableColor).show(sender)
                    } else if (sender is ConsoleCommandSender) {
                        sender.sendMessage("${ccErr}Console cannot open GUIs. Use ${ccErrDetl}/esskt conf color set <Key> <Color> ${ccErr}to alter configurable chat colors from console.")
                    }

                    return
                }

                val arg4 = args[4]

                // TODO Permissions

                val new: ChatColor = ChatColor.entries.firstOrNull { it.name.equalsIc(arg4) }
                    ?: return sender.sendMessage("${ccErr}Unknown chat color ${ccErrDetl}\"$arg4\"")

                if (new.isFormat || new == ChatColor.RESET)
                    return sender.sendMessage("${ccErr}You cannot use a formatting code for a configurable color.")

                val fc = getPlugin().config
                val path = "Colors.$key"

                fc.set(path, new.name)
                getPlugin().saveConfig()

                return sender.sendMessage("Color \"$key\" has been updated to: $new\"Example\"")
            } else if (arg2.equalsIc("list", "ls")) {
                listConfiguredColors()
            } else if (arg2.equalsIc("listvalid", "listcolors", "listcolor")) {
                listChatColors()
            } else if (arg2.equalsIc("reset", "resetall")) {
                val flagConfirm = ValidCommandFlag("Confirm")
                val flags = CommandFlagParser(args, flagConfirm)

                if (flags.wasPassed(flagConfirm)) {
                    val fc = getPlugin().config
                    var alteredCount = 0

                    for (key in ChatColorKey.entries) {
                        val path = "Colors.${key.name}"
                        val def = key.defaultValue
                        val current = fc.getString(path)

                        if (current == null || def != ChatColor.valueOf(current)) {
                            fc.set(path, key.defaultValue.name)
                            alteredCount++
                        }
                    }

                    if (alteredCount > 0) {
                        getPlugin().saveConfig()
                        sender.sendMessage("${ccPrim}All ${ccVal}$alteredCount ${ccPrim}customized chat colors were reset to their default values.")
                    } else {
                        sender.sendMessage("${ccErr}There was nothing to change. All colors match their default values.")
                    }

                    return
                }

                sender.sendMessage("${ccErr}Are you sure you want to reset the \"Colors\" section of the EssentialsKT configuration? This operation is irreversible.")
                sender.sendMessage("${ccErr}Execute the command again with the ${ccErrDetl}--confirm ${ccErr}flag if you are sure.")
            } else {
                sender.sendMessage("${ccErr}Unknown ${ccErrDetl}/esskt conf color ${ccErr}sub-command ${ccErrDetl}\"$arg2\"")
            }
        } else {
            sender.sendMessage("${ccErr}Unknown ${ccErrDetl}/esskt ${ccErr}sub-command ${ccErrDetl}\"$arg1\"")
        }
    }

    override fun execute(context: CommandContext) {
        val sender = context.sender
        val conf = getConfig()
        val ccErr = conf.getChatColor(ChatColorKey.Error)
        val ccErrDetl = conf.getChatColor(ChatColorKey.ErrorDetail)

        if (context.lacksPermission(PERM))
            return context.sendNoPermission(sender, PERM)

        val args = context.args
        val len = context.args.size

        if (len == 0 || (len == 1 && args[0].equalsIc(KEYS_SUBC_HELP)))
            return sender.sendChatComponents(getHelpAsComponents(context))

        val sub = args[0]

        when (sub.lowercase()) {
            "conf", "config", "configure" -> handleSubcConfig(context)
            "reload", "refresh" -> handleSubcReload(context)
            "version", "v" -> handleSubcVersion(context)
            else -> {
                sender.sendMessage("${ccErr}Unknown subcommand: ${ccErrDetl}\"$sub\" ${ccErr}Do ${ccErrDetl}/esskt ${ccErr}for help.")
            }
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ) : MutableList<String> {
        val tab = mutableListOf<String>()
        fun tabSorted() =
            tab.sorted().toMutableList()

        val suggestArg0 = mutableListOf("conf", "reload", "version")
        val suggestConfigAreas = mutableListOf("color")
        val suggestConfigColorSubc = listOf("set", "list", "listcolors", "reset")
        val validSubcConfigureAliases = listOf("conf", "config", "configure")
        val validConfigColorAliases = listOf("colors", "color")
        val validSetColorSubc = listOf("alter", "change", "set", "configure")

        if (sender.lacksPermission(PERM))
            return tab

        val len = args.size

        if (len == 1) {
            StringUtil.copyPartialMatches(args[0], suggestArg0, tab)
            return tabSorted()
        }

        val arg0 = args[0]

        if (len == 2) {
            if (arg0.equalsIc(validSubcConfigureAliases)) {
                StringUtil.copyPartialMatches(args[1], suggestConfigAreas, tab)
            }
            return tabSorted()
        }

        val arg1 = args[1]

        if (len == 3) {
            // /esskt conf colors set

            if (arg0.equalsIc(validSubcConfigureAliases) && arg1.equalsIc(validConfigColorAliases)) {
                StringUtil.copyPartialMatches(args[2], suggestConfigColorSubc, tab)
            }
            return tabSorted()
        }

        val arg2 = args[2]
        val colorKeys = ChatColorKey.entries
            .map { it.name }
            .toList()

        if (len == 4) {
            if (arg0.equalsIc(validSubcConfigureAliases) &&
                arg1.equalsIc(validConfigColorAliases) &&
                arg2.equalsIc(validSetColorSubc)) { // esskt conf colors set <partial>
                StringUtil.copyPartialMatches(args[3], colorKeys, tab)
            }
            return tabSorted()
        }

        val arg3 = args[3]
        fun String.isValidColorKey() =
            getConfig().fetchColorByKey(this) != null
        val allChatColors = ChatColor.entries
            .filter { !it.isFormat && it != ChatColor.RESET }
            .map { it.name }
            .toList()

        if (len == 5) {
            if (arg0.equalsIc(validSubcConfigureAliases) &&
                arg1.equalsIc(validConfigColorAliases) &&
                arg2.equalsIc(validSetColorSubc) &&
                arg3.isValidColorKey()) {
                StringUtil.copyPartialMatches(args[4], allChatColors, tab)
            }
        }

        return tabSorted()
    }
}