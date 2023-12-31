package io.github.tsgrissom.essentialskt

import com.earth2me.essentials.Essentials
import io.github.tsgrissom.essentialskt.command.*
import io.github.tsgrissom.essentialskt.config.ConfigManager
import io.github.tsgrissom.essentialskt.listener.JoinListener
import io.github.tsgrissom.essentialskt.listener.QuitListener
import io.github.tsgrissom.essentialskt.misc.PluginLogger
import io.github.tsgrissom.pluginapi.extension.bukkit.registerCommand
import io.github.tsgrissom.pluginapi.extension.bukkit.registerListeners
import io.github.tsgrissom.pluginapi.utility.EntityUtility
import org.bukkit.Bukkit.getPluginManager
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

class EssentialsKTPlugin : JavaPlugin() {

    private lateinit var configManager: ConfigManager
    private lateinit var entityUtility: EntityUtility

    private lateinit var essentialsPlugin: Essentials

    fun getConfigManager() = this.configManager
    fun getEntityUtility() = this.entityUtility
    fun getEssentials() = this.essentialsPlugin

    /* Static Instance */
    companion object {
        var instance: EssentialsKTPlugin? = null
        private set
    }

    private fun createEssentialsHook() {
        val pl: Plugin? = getPluginManager().getPlugin("Essentials")

        if (pl == null) {
            PluginLogger.severe("Essentials not found! Please install EssentialsX to use EssentialsKT.", withPrefix=true)
            return getPluginManager().disablePlugin(this)
        }

        this.essentialsPlugin = pl as Essentials
        PluginLogger.info("Essentials hook created.", withPrefix=true)
    }

    private fun registerCommands() {
        /* Register General Commands (A->Z) */
        registerCommand("clearchat", ClearChatCommand())
        registerCommand("damage", DamageCommand())
        registerCommand("esskt", EssKtCommand())
        registerCommand("gamemode", GameModeCommand())
        registerCommand("gms", GmxCommand())
        registerCommand("ipaddress", IPAddressCommand())
        registerCommand("list", ListCommand())
        registerCommand("remove", RemoveCommand())
        registerCommand("renameitem", RenameItemCommand())
        registerCommand("setfoodlevel", SetFoodLevelCommand())
        registerCommand("sethealth", SetHealthCommand())
        registerCommand("setremainingair", SetRemainingAirCommand())
        registerCommand("time", TimeCommand())
        registerCommand("toggledownfall", ToggleDownfallCommand())
        registerCommand("uniqueid", UniqueIdCommand())
        registerCommand("weather", WeatherCommand())
        registerCommand("whois", WhoIsCommand())
        registerCommand("worlds", WorldsCommand())

        /* Quick Time Commands */
        registerCommand("day", DayCommand())
        registerCommand("midnight", MidnightCommand())
        registerCommand("night", NightCommand())
        registerCommand("noon", NoonCommand())
        registerCommand("dusk", DuskCommand())
        registerCommand("dawn", DawnCommand())
        registerCommand("sunset", SunsetCommand())
        registerCommand("sunrise", SunriseCommand())

        /* Weather Commands */
        registerCommand("clearweather", ClearWeatherCommand())
        registerCommand("rain", RainCommand())
    }

    override fun onEnable() {
        instance = this
        configManager = ConfigManager()
        entityUtility = EntityUtility()

        createEssentialsHook()

        config.options().copyDefaults(true)
        saveDefaultConfig()

        this.registerCommands()
        this.registerListeners(
            JoinListener(),
            QuitListener()
        )
    }
}