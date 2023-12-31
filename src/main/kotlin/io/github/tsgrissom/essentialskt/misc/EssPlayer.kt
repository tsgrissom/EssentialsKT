package io.github.tsgrissom.essentialskt.misc

import com.earth2me.essentials.User
import io.github.tsgrissom.essentialskt.EssentialsKTPlugin
import io.github.tsgrissom.essentialskt.command.GameModeCommand
import io.github.tsgrissom.pluginapi.chat.ClickTextBuilder
import io.github.tsgrissom.pluginapi.extension.bukkit.appendc
import io.github.tsgrissom.pluginapi.extension.bukkit.getIPString
import io.github.tsgrissom.pluginapi.extension.bukkit.uniqueString
import io.github.tsgrissom.pluginapi.extension.kt.capitalizeAllCaps
import io.github.tsgrissom.pluginapi.extension.kt.fmtYesNo
import io.github.tsgrissom.pluginapi.extension.kt.roundToDigits
import net.md_5.bungee.api.ChatColor.*
import net.md_5.bungee.api.ChatColor.DARK_GRAY as D_GRAY
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.attribute.Attribute
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class EssPlayer(private val uuid: UUID) {

    private fun getPlugin() =
        EssentialsKTPlugin.instance ?: error("plugin instance is null")

    private val player: Player = Bukkit.getPlayer(uuid)
        ?: error("Player for UUID \"$uuid\" is offline!")

    constructor(player: Player) : this(player.uniqueId)
    constructor(username: String) : this(Bukkit.getPlayer(username)
        ?: error("Player for username \"$username\" is offline!"))

    fun getUniqueString() : String = uuid.toString()
    fun getIPString() : String = player.getIPString()
    fun getNickname() : String = player.displayName

    fun getEssentialsUser() : User = getPlugin().getEssentials().getUser(player)
    fun isAfk() : Boolean = getEssentialsUser().isAfk // FIXME I don't think Essentials API is working here

    fun getMaxHealth() : Double {
        val attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)
            ?: return 20.0
        return attr.value
    }

    fun getAvailableGameModes() : Set<GameMode> {
        val set = mutableSetOf<GameMode>()

        if (player.hasPermission(GameModeCommand.PERM_ADVENTURE))
            set.add(GameMode.ADVENTURE)
        if (player.hasPermission(GameModeCommand.PERM_CREATIVE))
            set.add(GameMode.CREATIVE)
        if (player.hasPermission(GameModeCommand.PERM_SPECTATOR))
            set.add(GameMode.SPECTATOR)
        if (player.hasPermission(GameModeCommand.PERM_SURVIVAL))
            set.add(GameMode.SURVIVAL)

        return set
    }

    fun getNextGameMode(sender: CommandSender, target: Player) : GameMode? {
        fun checkPermission(mode: GameMode) : Boolean {
            return sender.hasPermission("essentials.gamemode.${mode.name.lowercase()}")
        }

        return when (target.gameMode) {
            GameMode.ADVENTURE -> {
                if (checkPermission(GameMode.CREATIVE)) GameMode.CREATIVE
                else if (checkPermission(GameMode.SURVIVAL)) GameMode.SURVIVAL
                else GameMode.ADVENTURE
            }
            GameMode.CREATIVE -> {
                if (checkPermission(GameMode.SURVIVAL)) GameMode.SURVIVAL
                else if (checkPermission(GameMode.ADVENTURE)) GameMode.ADVENTURE
                else GameMode.CREATIVE
            }
            GameMode.SURVIVAL -> {
                if (checkPermission(GameMode.ADVENTURE)) GameMode.ADVENTURE
                else if (checkPermission(GameMode.CREATIVE)) GameMode.CREATIVE
                else GameMode.SURVIVAL
            }
            else -> null
        }
    }

    fun cycleGameMode(sender: CommandSender) {
        val target = this.player
        val gm = getNextGameMode(sender, target)
            ?: return sender.sendMessage("${RED}${target.name} ${DARK_RED}is in a gamemode which cannot be cycled")
        val mn = gm.name.capitalizeAllCaps()
        val tn = target.name

        if (gm == target.gameMode)
            return sender.sendMessage("${DARK_RED}You do not have permission to cycle to another gamemode")

        Bukkit.dispatchCommand(sender, "gm $mn $tn")
    }

    fun setGameMode(sender: CommandSender, mode: GameMode) {
        val target = this.player
        if (sender != target && target.hasPermission(GameModeCommand.PERM_DEFENSIVE)) {
            val s = sender.name
            val t = target.name
            Bukkit.getLogger().info(
                "$s attempted to set ${t}'s gamemode but the target had \"${GameModeCommand.PERM_DEFENSIVE}\""
            )
            sender.sendMessage("${DARK_RED}You are not able to set ${RED}${t}'s ${DARK_RED}gamemode")
            return
        }

        target.gameMode = mode
    }

    fun generateTemporaryAttributesList(
        withHeader: Boolean = true,
        linePrefix: String = " - ",
        excludeGamemode: Boolean = false,
        excludeCoordinates: Boolean = false,
        excludeWorld: Boolean = false,
        excludeAfk: Boolean = false,
        excludeFireTicks: Boolean = false,
        excludeHealth: Boolean = false,
        excludeHunger: Boolean = false,
        excludeOxygen: Boolean = false,
        excludeSpeed: Boolean = false,
        excludeFlight: Boolean = false,
        excludeSneaking: Boolean = false,
        excludeSprinting: Boolean = false,
        excludeExperience: Boolean = false
    ) : Array<BaseComponent> {
        val comp = ComponentBuilder()

        fun appendPrefix() {
            if (linePrefix.isNotEmpty())
                comp.appendc(linePrefix, D_GRAY)
        }

        if (withHeader)
            comp.appendc("/whois for ", GOLD)
                .appendc(player.name, YELLOW)
                .appendc(" : ", D_GRAY)
                .appendc("Temporary", GRAY)
                .append("\n")

        if (!excludeGamemode) {
            val gm = player.gameMode.name.capitalizeAllCaps()
            appendPrefix()
            comp.appendc("Gamemode: ", GRAY)
                .appendc(gm, AQUA)
                .append("\n")
        }

        if (!excludeWorld) {
            appendPrefix()
            comp.appendc("World: ", GRAY)
                .appendc(player.name, YELLOW)
                .append("\n")
        }

        if (!excludeCoordinates) {
            val loc = player.location
            val x = loc.x.roundToDigits(2)
            val y = loc.y.roundToDigits(2)
            val z = loc.z.roundToDigits(2)

            appendPrefix()
            comp.appendc("Location ", GRAY)
                .appendc("X", RED)
                .appendc("Y", GREEN)
                .appendc("Z", AQUA)
                .appendc(": ", GRAY)
                .appendc("$x ", RED)
                .appendc("$y ", GREEN)
                .appendc("$z", AQUA)
                .append("\n")
        }

        if (!excludeAfk) {
            appendPrefix()
            comp.appendc("Is AFK: ", GRAY)
                .append(isAfk().fmtYesNo(withColor=true))
        }

        if (!excludeHealth) {
            val health = player.health
            val attrMaxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)
            val maxHealth = attrMaxHealth?.value ?: 20.0

            appendPrefix()
            comp.appendc("Health: ", GRAY)
                .appendc("$health", YELLOW)
                .appendc(" / ", D_GRAY)
                .appendc("Max: ", GRAY)
                .appendc("$maxHealth", YELLOW)
                .append("\n")
        }

        if (!excludeHunger) {
            val foodLevel = player.foodLevel
            val hunger = 20 - foodLevel

            appendPrefix()
            comp.appendc("Food Level: ", GRAY)
                .appendc("$foodLevel", YELLOW)
                .appendc("/", D_GRAY)
                .appendc("20", YELLOW)
                .appendc(" + ", D_GRAY)
                .appendc("Hunger: ", GRAY)
                .appendc("$hunger", YELLOW)
                .append("\n")
        }

        if (!excludeOxygen) {
            appendPrefix()
            comp.appendc("Oxygen: ", GRAY)
                .appendc("${player.remainingAir}", YELLOW)
                .appendc("/", D_GRAY)
                .appendc("300", YELLOW)
                .append("\n")
        }

        if (!excludeFireTicks) {
            val ticks = player.fireTicks

            appendPrefix()
            comp.appendc("On Fire: ", GRAY)

            if (ticks > 0)
                comp.appendc("Yes", GREEN)
                    .appendc(" + ", D_GRAY)
                    .appendc("Ticks Left: ", GRAY)
                    .appendc("$ticks", YELLOW)
            else
                comp.appendc("No", RED)

            comp.append("\n")
        }

        if (!excludeSpeed) {
            val fly = "${player.flySpeed}"
            val walk = "${player.walkSpeed}"

            appendPrefix()
            comp.appendc("Flying Speed: ", GRAY)
                .appendc(fly, YELLOW)
                .appendc(" + ", D_GRAY)
                .appendc("Walking Speed: ", GRAY)
                .appendc(walk, YELLOW)
                .append("\n")
        }

        if (!excludeFlight) {
            val canFly = player.allowFlight.fmtYesNo(withColor=true)
            val isFlying = player.isFlying.fmtYesNo(withColor=true)

            appendPrefix()
            comp.appendc("Can Fly: ", GRAY)
                .append(canFly)
                .appendc(" + ", D_GRAY)
                .appendc("Is Flying: ", GRAY)
                .append(isFlying)
                .append("\n")
        }

        if (!excludeSneaking || !excludeSprinting) {
            val isSneaking = player.isSneaking.fmtYesNo(withColor=true)
            val isSprinting = player.isSprinting.fmtYesNo(withColor=true)

            appendPrefix()

            if (!excludeSneaking) {
                comp.appendc("Is Sneaking: ", GRAY)
                    .appendc(isSneaking, YELLOW)
            }

            if (!excludeSneaking && !excludeSprinting)
                comp.appendc(" + ", D_GRAY)

            if (!excludeSprinting) {
                comp.appendc("Is Sprinting: ", GRAY)
                    .appendc(isSprinting, YELLOW)
            }

            comp.append("\n")
        }

        if (!excludeExperience) {
            appendPrefix()
            comp.appendc("Level: ", GRAY)
                .appendc("${player.level}", YELLOW)
                .appendc(" + ", D_GRAY)
                .appendc("Exp: ", GRAY)
                .appendc("${player.exp}", YELLOW)
                .appendc(" + ", D_GRAY)
                .appendc("Total Exp: ", GRAY)
                .appendc("${player.totalExperience}", YELLOW)
        }

        return comp.create()
    }

    fun generateSemipermanentAttributesList(
        withHeader: Boolean = true,
        linePrefix: String = " - ",
        excludeNames: Boolean = false,
        excludeOperator: Boolean = false,
        excludeIp: Boolean = false
    ) : Array<BaseComponent> {
        val comp = ComponentBuilder()

        fun appendPrefix() {
            if (linePrefix.isNotEmpty())
                comp.appendc(linePrefix, D_GRAY)
        }

        if (withHeader)
            comp.appendc("/whois for ", GOLD)
                .appendc(player.name, YELLOW)
                .appendc(" : ", D_GRAY)
                .appendc("Semi-Permanent", GRAY)
                .append("\n")

        if (!excludeNames) {
            val dn = player.displayName
            val hasNoNickname = dn == player.name
            val nickname = if (hasNoNickname)
                "None"
            else
                dn
            val nicknameColor = if (hasNoNickname)
                RED
            else
                YELLOW

            appendPrefix()
            comp.appendc("Username: ", GRAY)
                .appendc(player.name, YELLOW)
                .appendc(" + ", D_GRAY)
                .appendc("Display Name: ", GRAY)
                .appendc(nickname, nicknameColor)
                .append("\n")
        }

        if (!excludeIp) {
            val ip = this.getIPString()
            val data = ClickTextBuilder(ip)
                .color(YELLOW)
                .hoverText("${GRAY}Click to copy IP address")
                .action(ClickEvent.Action.COPY_TO_CLIPBOARD)
                .value(ip)
                .toComponent()

            appendPrefix()
            comp.appendc("IP Address: ", GRAY)
                .append(data).append("\n").reset()
        }

        if (!excludeOperator) {
            val isOp = player.isOp.fmtYesNo(withColor=true)
            appendPrefix()
            comp.appendc("Is Op: ", GRAY)
                .appendc(isOp, YELLOW)
        }

        return comp.create()
    }

    fun generatePermanentAttributesList(
        withHeader: Boolean = true,
        linePrefix: String = " - ",
        excludeUniqueId: Boolean = false
    ) : Array<BaseComponent> {
        val comp = ComponentBuilder()

        fun appendPrefix() {
            if (linePrefix.isNotEmpty())
                comp.appendc(linePrefix, D_GRAY)
        }

        if (withHeader)
            comp.appendc("/whois for ", GOLD)
                .appendc(player.name, YELLOW)
                .append("\n")

        if (!excludeUniqueId) {
            val uuid = player.uniqueString
            val data = ClickTextBuilder(uuid)
                .action(ClickEvent.Action.COPY_TO_CLIPBOARD)
                .color(YELLOW)
                .hoverText("${GRAY}Click to copy UUID")
                .value(uuid)
                .toComponent()

            appendPrefix()
            comp.appendc("Unique ID: ", GRAY)
                .append(data)
        }

        return comp.create()
    }
}