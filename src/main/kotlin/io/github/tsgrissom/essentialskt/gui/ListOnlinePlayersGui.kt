package io.github.tsgrissom.essentialskt.gui

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.OutlinePane
import io.github.tsgrissom.pluginapi.extension.*
import net.md_5.bungee.api.ChatColor.*
import net.md_5.bungee.api.ChatColor.DARK_GRAY as D_GRAY
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class ListOnlinePlayersGui : ChestGui(5, "Online Players") {

    init {
        val op = Bukkit.getOnlinePlayers()
        val pane = OutlinePane(0, 0, 9, 5)

        op.forEach { pane.addItem(createPlayerHead(it)) }

        this.addPane(pane)
    }

    private fun createPlayerHead(p: Player) : GuiItem {
        val wn = p.world.name
        val x = p.location.x.roundToDigits(1)
        val y = p.location.y.roundToDigits(1)
        val z = p.location.z.roundToDigits(1)

        val n = p.name
        val dn = p.displayName
        val uuid = p.getUniqueString()

        return GuiItem(
            ItemStack(Material.PLAYER_HEAD)
                .playerHeadOf(p)
                .name("${GOLD}$n")
                .lore(
                    "${GRAY}Click to view their ${YELLOW}/whois ${GRAY}profile",
                    "${D_GRAY}> ${GRAY}Nickname${D_GRAY}: ${YELLOW}$dn",
                    "${D_GRAY}> ${GRAY}UUID${D_GRAY}: ${YELLOW}$uuid",
                    "${D_GRAY}> ${GRAY}World${D_GRAY}: ${YELLOW}$wn",
                    "${D_GRAY}> ${GRAY}Location ${RED}X${GREEN}Y${AQUA}Z${D_GRAY}: ${RED}$x ${GREEN}$y ${AQUA}$z"
                )
                .flag(ItemFlag.HIDE_ATTRIBUTES)
        ) { e ->
            e.isCancelled = true
            e.whoClicked.closeInventory()
            Bukkit.dispatchCommand(e.whoClicked, "whois ${p.name}")
        }
    }
}