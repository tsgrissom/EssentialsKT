package io.github.tsgrissom.testpluginkt

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

import io.github.tsgrissom.testpluginkt.command.GamemodeCommand
import io.github.tsgrissom.testpluginkt.command.PingCommand
import io.github.tsgrissom.testpluginkt.command.SuicideCommand
import io.github.tsgrissom.testpluginkt.listener.ChatListener
import io.github.tsgrissom.testpluginkt.listener.JoinAndQuitListener

class TestPluginKT : JavaPlugin() {

    companion object {
        var instance: TestPluginKT? = null
        private set
    }

    override fun onEnable() {
        instance = this

        config.options().copyDefaults(true)
        saveDefaultConfig()

        getCommand("gamemode").executor = GamemodeCommand()
        getCommand("ping").executor = PingCommand()
        getCommand("suicide").executor = SuicideCommand()

        Bukkit.getPluginManager().registerEvents(ChatListener(), this)
        Bukkit.getPluginManager().registerEvents(JoinAndQuitListener(), this)
    }
}