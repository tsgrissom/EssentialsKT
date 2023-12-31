package io.github.tsgrissom.essentialskt.command

import io.github.tsgrissom.essentialskt.EssentialsKTPlugin
import io.github.tsgrissom.essentialskt.config.ChatColorKey
import io.github.tsgrissom.pluginapi.command.CommandBase
import io.github.tsgrissom.pluginapi.command.CommandContext
import io.github.tsgrissom.pluginapi.extension.bukkit.lacksPermission
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil

class DayCommand
    : QuickTimeCommand(TimeCommand.TIME_DAY, "Day")
class NoonCommand
    : QuickTimeCommand(TimeCommand.TIME_NOON, "Noon")
class DuskCommand
    : QuickTimeCommand(TimeCommand.TIME_SUNSET, "Dusk")
class SunsetCommand
    : QuickTimeCommand(TimeCommand.TIME_SUNSET, "Sunset")
class NightCommand
    : QuickTimeCommand(TimeCommand.TIME_NIGHT, "Night")
class MidnightCommand
    : QuickTimeCommand(TimeCommand.TIME_MIDNIGHT, "Midnight")
class DawnCommand
    : QuickTimeCommand(TimeCommand.TIME_SUNRISE, "Dawn")
class SunriseCommand
    : QuickTimeCommand(TimeCommand.TIME_SUNRISE, "Sunrise")

open class QuickTimeCommand(
    private val time: Long,
    private val timeName: String
) : CommandBase() {

    private fun getPlugin() =
        EssentialsKTPlugin.instance ?: error("plugin instance is null")
    private fun getConfig() = getPlugin().getConfigManager()

    private fun getTimeSetMessage(w: World, tn: String) : String {
        val conf = getConfig()
        val ccPrim = conf.getChatColor(ChatColorKey.Primary)
        val ccDetl = conf.getChatColor(ChatColorKey.Detail)

        return "${ccPrim}You set world ${ccDetl}${w.name}'s ${ccPrim}time to ${ccDetl}$tn${ccPrim}."
    }

    override fun execute(context: CommandContext) {
        val conf = getConfig()
        val ccErr = conf.getChatColor(ChatColorKey.Error)
        val ccErrDetl = conf.getChatColor(ChatColorKey.ErrorDetail)

        val args = context.args
        val sender = context.sender

        if (sender.lacksPermission(TimeCommand.PERM_SET))
            return context.sendNoPermission(sender, TimeCommand.PERM_SET)

        var world = Bukkit.getWorlds()[0]

        if (args.isNotEmpty()) {
            val sub = args[0]
            world = Bukkit.getWorld(sub)
                ?: return sender.sendMessage("${ccErr}Could not find world ${ccErrDetl}\"$sub\"${ccErr}.")
        }

        if (TimeCommand.lacksPermissionToSetWorldTime(sender, world))
            return context.sendNoPermission(sender, TimeCommand.getTimeSetPerWorldPermission(world))

        val setMessage = getTimeSetMessage(world, timeName)

        world.time = time
        sender.sendMessage(setMessage)
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ):  MutableList<String> {
        val tab = mutableListOf<String>()

        if (sender.lacksPermission(TimeCommand.PERM_SET))
            return tab

        if (args.size == 1)
            StringUtil.copyPartialMatches(args[0], getWorldNamesToMutableList(), tab)

        return tab.sorted().toMutableList()
    }
}