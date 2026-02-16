package xyz.devcmb.playground

import org.bukkit.plugin.java.JavaPlugin
import xyz.devcmb.invcontrol.InvControlManager
import xyz.devcmb.playground.controllers.WorldController
import java.util.logging.Logger

class ParkourPlayground : JavaPlugin() {
    companion object {
        lateinit var plugin: ParkourPlayground
        lateinit var pluginLogger: Logger
    }

    override fun onEnable() {
        plugin = this
        pluginLogger = logger

        InvControlManager.setPlugin(this)
        saveDefaultConfig()
        ControllerDelegate.registerAllControllers()
    }

    override fun onDisable() {
        ControllerDelegate.cleanupControllers()
    }
}
