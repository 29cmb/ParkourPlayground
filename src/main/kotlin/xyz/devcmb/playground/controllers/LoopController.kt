package xyz.devcmb.playground.controllers

import org.bukkit.Bukkit
import xyz.devcmb.playground.ParkourPlayground
import xyz.devcmb.playground.annotations.Controller

@Controller("loopController")
class LoopController : IController {
    var currentState: GameState = GameState.PRELOAD
    override fun init() {
        Bukkit.getScheduler().runTaskTimer(ParkourPlayground.plugin, Runnable {
            currentState = GameState.entries.random()
        }, 0, 60)
    }

    enum class GameState {
        PRELOAD,
        PLAYER_WAITING,
        INTERMISSION,
        PREGAME,
        GAME_ON,
        GAME_END,
        CLEANUP
    }
}