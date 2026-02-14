package xyz.devcmb.playground.controllers

import org.bukkit.Bukkit
import org.bukkit.event.Listener
import xyz.devcmb.playground.ParkourPlayground
import xyz.devcmb.playground.annotations.Controller
import xyz.devcmb.playground.listeners.PlayerListeners

@Controller("listenerController", priority = Controller.Priority.LOWEST)
class ListenerController : IController {
    override fun init() {
        registerListener(PlayerListeners())
    }

    fun registerListener(listener: Listener) {
        Bukkit.getPluginManager().registerEvents(listener, ParkourPlayground.plugin)
    }
}