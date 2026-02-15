package xyz.devcmb.playground

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import org.bukkit.plugin.java.JavaPlugin
import xyz.devcmb.invcontrol.InvControlManager
import xyz.devcmb.playground.controllers.WorldController
import java.util.logging.Logger

class ParkourPlayground : JavaPlugin() {
    companion object {
        lateinit var plugin: ParkourPlayground
        lateinit var pluginLogger: Logger
        lateinit var protocolManager: ProtocolManager
    }

    override fun onEnable() {
        plugin = this
        pluginLogger = logger

        InvControlManager.setPlugin(this)
        protocolManager = ProtocolLibrary.getProtocolManager()

        saveDefaultConfig()

        ControllerDelegate.registerAllControllers()
    }

    override fun onDisable() {
        ControllerDelegate.cleanupControllers()
    }
}
