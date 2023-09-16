package io.github.tsgrissom.essentialskt.gui

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.OutlinePane
import io.github.tsgrissom.pluginapi.extension.*
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

class PlayerListGui : ChestGui(5, "Online Players") {

    init {
        val op = Bukkit.getOnlinePlayers()
        val pane = OutlinePane(0, 0, 9, 5)

        for (p in op) {
            val head = createPlayerHead(p)
            val btn = GuiItem(head) { e ->
                e.isCancelled = true
                e.whoClicked.closeInventory()
                Bukkit.dispatchCommand(e.whoClicked, "whois ${p.name}")
            }
            pane.addItem(btn)
        }

        this.addPane(pane)
    }

    private fun createPlayerHead(p: Player) : ItemStack {
        val x = p.location.x.roundToDigits(1)
        val y = p.location.y.roundToDigits(1)
        val z = p.location.z.roundToDigits(1)
        val item = ItemStack(Material.PLAYER_HEAD)
            .name("&6${p.name}")
            .lore(
                "&7Click to view their &e/whois &7profile",
                "&8> &7Nickname: &e${p.displayName}",
                "&8> &7UUID: &e${p.getUniqueString()}",
                "&8> &7World&8: &e${p.world.name}",
                "&8> &7Location &cX&aY&bZ&8: &c$x &a$y &b$z"
            )
            .flag(ItemFlag.HIDE_ATTRIBUTES)

        val sm = item.itemMeta as SkullMeta
        sm.ownerProfile = p.playerProfile
        item.itemMeta = sm
        return item
    }
}