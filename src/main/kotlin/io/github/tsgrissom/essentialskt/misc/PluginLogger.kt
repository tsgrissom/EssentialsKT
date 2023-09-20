package io.github.tsgrissom.essentialskt.misc

import io.github.tsgrissom.essentialskt.EssentialsKTPlugin
import org.bukkit.Bukkit

class PluginLogger {

    companion object {
        private fun getPlugin() : EssentialsKTPlugin =
            EssentialsKTPlugin.instance ?: error("plugin instance is null")

        private val isDebugging: Boolean = getPlugin().configManager.isDebuggingActive()

        private fun getPrefix(debug: Boolean) = if (debug && isDebugging) "DEBUG: " else String()

        fun info(s: String, debug: Boolean = false) = Bukkit.getLogger().info("${getPrefix(debug)}$s")
        fun warning(s: String, debug: Boolean = false) = Bukkit.getLogger().warning("${getPrefix(debug)}$s")
        fun severe(s: String, debug: Boolean = false) = Bukkit.getLogger().severe("${getPrefix(debug)}$s")
    }
}