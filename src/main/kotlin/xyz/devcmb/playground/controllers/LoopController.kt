package xyz.devcmb.playground.controllers

import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import xyz.devcmb.playground.Constants
import xyz.devcmb.playground.ParkourPlayground
import xyz.devcmb.playground.annotations.Configurable
import xyz.devcmb.playground.annotations.Controller

@Controller("loopController", Controller.Priority.HIGHEST)
class LoopController : IController {
    var currentState: GameState = GameState.PRELOAD
    var playerWaitingRunnable: BukkitRunnable? = null
    var countdownRunnable: BukkitRunnable? = null
    var countdown: Int = 30

    companion object {
        @field:Configurable("game.intermission_length")
        var intermissionLength: Int = 30
    }

    override fun init() {
        pregame()
    }

    fun pregame() {
        if(Bukkit.getOnlinePlayers().size < 2 && !Constants.IS_DEVELOPMENT) {
            playerWaiting()
            return
        }

        intermission()
    }

    fun playerWaiting() {
        currentState = GameState.PLAYER_WAITING

        playerWaitingRunnable = object : BukkitRunnable() {
            override fun run() {
                if(Bukkit.getOnlinePlayers().size < 2) return;
                cancel()
                playerWaitingRunnable = null
                pregame()
            }
        }
        playerWaitingRunnable!!.runTaskTimer(ParkourPlayground.plugin, 0, 20)
    }

    fun intermission() {
        currentState = GameState.INTERMISSION
        countdown = intermissionLength

        countdownRunnable = object : BukkitRunnable() {
            override fun run() {
                if(Bukkit.getOnlinePlayers().size < 2) {
                    cancel()
                    countdownRunnable = null
                    countdown = intermissionLength
                    playerWaiting()
                    return
                }

                if(countdown <= 0) {
                    cancel()
                    countdownRunnable = null
                    countdown = intermissionLength
                    gameStart()
                    return
                }

                countdown--
            }
        }
        countdownRunnable!!.runTaskTimer(ParkourPlayground.plugin, 0, 20)
    }

    fun gameStart() {
        // TODO
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