package xyz.devcmb.playground.controllers

import com.destroystokyo.paper.event.server.ServerTickStartEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import xyz.devcmb.playground.annotations.Controller
import xyz.devcmb.playground.ui.PlayerUIDelegate

@Controller("uiController")
class UIController : IController {
    val playerControllers: ArrayList<PlayerUIDelegate> = ArrayList()

    override fun init() {
    }

    @EventHandler
    fun playerJoinEvent(event: PlayerJoinEvent) {
        val delegate = PlayerUIDelegate(event.player)
        playerControllers.add(delegate)
    }

    @EventHandler
    fun playerQuitEvent(event: PlayerQuitEvent) {
        playerControllers.removeIf({ it.player == event.player })
    }

    @EventHandler
    fun tickEvent(event: ServerTickStartEvent) {
        playerControllers.forEach {
            it.tick(event)
        }
    }
}