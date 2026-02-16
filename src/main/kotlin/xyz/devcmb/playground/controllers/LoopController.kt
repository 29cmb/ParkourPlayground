package xyz.devcmb.playground.controllers

import org.apache.commons.io.FileUtils
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import xyz.devcmb.playground.Constants
import xyz.devcmb.playground.ControllerDelegate
import xyz.devcmb.playground.ParkourPlayground
import xyz.devcmb.playground.WorldSetupException
import xyz.devcmb.playground.annotations.Configurable
import xyz.devcmb.playground.annotations.Controller
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

@Controller("loopController", Controller.Priority.HIGH)
class LoopController : IController {
    var currentState: GameState = GameState.PRELOAD
        set(value) {
            ParkourPlayground.pluginLogger.info("Transitioning GameState to ${value.name}")
            field = value
        }
    var prePauseState: GameState? = null

    var playerWaitingRunnable: BukkitRunnable? = null
    var countdownRunnable: BukkitRunnable? = null
    var countdown: Int = 30
    var world: World? = null

    val playerStates: HashMap<Player, PlayerState> = HashMap()

    companion object {
        @field:Configurable("game.intermission_length")
        var intermissionLength: Int = 30

        @field:Configurable("game.starting_position")
        var startPosition: List<Double> = listOf(-0.5, 67.0, -0.5)
    }

    override fun init() {
        setup()
    }

    override fun cleanup() {

    }

    fun setup() {
        if(Bukkit.getOnlinePlayers().size < 2 && !Constants.IS_DEVELOPMENT) {
            playerWaiting()
            return
        }

        intermission()
    }

    fun pauseLoop() {
        prePauseState = currentState
        currentState = GameState.PAUSED
    }

    fun unpauseLoop() {
        currentState = prePauseState!!
        prePauseState = null
    }

    fun playerWaiting() {
        currentState = GameState.PLAYER_WAITING

        playerWaitingRunnable = object : BukkitRunnable() {
            override fun run() {
                if(currentState == GameState.PAUSED) return;

                if(Bukkit.getOnlinePlayers().size < 2 && !Constants.IS_DEVELOPMENT) return;
                cancel()
                playerWaitingRunnable = null
                setup()
            }
        }
        playerWaitingRunnable!!.runTaskTimer(ParkourPlayground.plugin, 0, 20)
    }

    fun intermission() {
        currentState = GameState.INTERMISSION
        countdown = intermissionLength

        countdownRunnable = object : BukkitRunnable() {
            override fun run() {
                if(currentState == GameState.PAUSED) return;

                if(Bukkit.getOnlinePlayers().size < 2 && !Constants.IS_DEVELOPMENT) {
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
                    preparingWorld()
                    return
                }

                countdown--
            }
        }
        countdownRunnable!!.runTaskTimer(ParkourPlayground.plugin, 0, 20)
    }

    fun preparingWorld() {
        currentState = GameState.PREPARING_WORLD
        val worldController: WorldController = ControllerDelegate.getController("worldController") as WorldController

        try {
            this.world = worldController.setupGameWorld()
        } catch(e: WorldSetupException) {
            ParkourPlayground.pluginLogger.severe("An error occurred trying to setup the gameplay world: ${e.message}")
            currentState = GameState.ERROR
        }

        preGame()
    }

    fun preGame() {
        currentState = GameState.PREGAME
        Bukkit.getOnlinePlayers().forEach { player ->
            playerStates.put(player, PlayerState.PREGAME)
            player.teleport(Location(world, startPosition.get(0), startPosition.get(1), startPosition.get(2)))
            player.gameMode = GameMode.ADVENTURE
        }
    }

    fun gameOn() {
        currentState = GameState.GAME_ON
    }

    enum class GameState {
        PRELOAD,
        PLAYER_WAITING,
        INTERMISSION,
        PREPARING_WORLD,
        PREGAME,
        GAME_ON,
        GAME_END,
        CLEANUP,
        PAUSED,
        ERROR
    }

    enum class PlayerState {
        PREGAME,
        PROGRESSING,
        COMPLETED,
        DEAD
    }
}