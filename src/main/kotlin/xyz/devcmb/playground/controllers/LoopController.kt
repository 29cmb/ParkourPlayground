package xyz.devcmb.playground.controllers

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import net.kyori.adventure.util.Ticks
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scheduler.BukkitRunnable
import xyz.devcmb.playground.Constants
import xyz.devcmb.playground.ControllerDelegate
import xyz.devcmb.playground.ObstacleStepException
import xyz.devcmb.playground.ParkourPlayground
import xyz.devcmb.playground.WorldSetupException
import xyz.devcmb.playground.annotations.Configurable
import xyz.devcmb.playground.annotations.Controller
import xyz.devcmb.playground.util.DebugUtil
import xyz.devcmb.playground.util.Format
import xyz.devcmb.playground.util.MiscUtils
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

@Controller("loopController", Controller.Priority.HIGH)
class LoopController : IController {
    var currentState: GameState = GameState.PRELOAD
        set(value) {
            DebugUtil.log("Transitioning GameState to ${value.name}", DebugUtil.DebugLogLevel.INFO)
            field = value
        }
    var prePauseState: GameState? = null

    var playerWaitingRunnable: BukkitRunnable? = null
    var countdownRunnable: BukkitRunnable? = null
    var countdown: Int = 30
    var world: World? = null

    val playerScores: HashMap<Player, Int> = HashMap()
    val playerObstacleCounts: HashMap<Player, Int> = HashMap()
    val alivePlayers: HashSet<Player> = HashSet()

    companion object {
        @field:Configurable("game.intermission_length")
        var intermissionLength: Int = 30

        @field:Configurable("game.starting_position")
        var startPosition: List<Double> = listOf(-0.5, 67.0, -0.5)

        @field:Configurable("game.gate_start")
        var gateStartPosition: List<Int> = listOf(2,68,7)

        @field:Configurable("game.gate_end")
        var gateEndPosition: List<Int> = listOf(-4,66,7)

        @field:Configurable("lobby.position")
        var lobbySpawn: List<Double> = listOf(0.5,67.0,0.5)

        @field:Configurable("lobby.world")
        var lobbyWorld: String = "hub"
    }

    override fun init() {
        setup()
    }

    fun setup() {
        if(Bukkit.getOnlinePlayers().size < (if(Constants.IS_DEVELOPMENT) 1 else 2)) {
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

                if(Bukkit.getOnlinePlayers().size < (if(Constants.IS_DEVELOPMENT) 1 else 2)) return;
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

                if(Bukkit.getOnlinePlayers().size < (if(Constants.IS_DEVELOPMENT) 1 else 2)) {
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
            DebugUtil.severe("An error occurred trying to setup the gameplay world: ${e.message}")
            currentState = GameState.ERROR
        }

        preGame()
    }

    fun preGame() {
        currentState = GameState.PREGAME

        Bukkit.getOnlinePlayers().forEach { player ->
            alivePlayers.add(player)
            addPlayerScore(player, 0)

            player.teleport(Location(world, startPosition.get(0), startPosition.get(1), startPosition.get(2)))
            player.gameMode = GameMode.ADVENTURE
        }

        val obstacleController = ControllerDelegate.getController("obstacleController") as ObstacleController
        obstacleController.pregame(this)

        MiscUtils.delay(3, {
            MiscUtils.countdown(playerScores.keys, 10, this::gameOn, {
                if(currentState != GameState.PREGAME) return@countdown

                try {
                    obstacleController.stepObstacleLoad()
                } catch(e: ObstacleStepException) {
                    currentState = GameState.ERROR
                    DebugUtil.severe("An error occurred when trying to step obstacle: ${e.message}")
                }
            })
        })
    }

    fun addPlayerScore(player: Player, score: Int) {
        var newScore: Int =
            if(!playerScores.containsKey(player)) score
            else playerScores[player]!! + score

        playerScores[player] = newScore

        player.playerListName(
            player.displayName().append(Component.text(" ".repeat(10)))
                .append(Component.text(newScore.toString(), NamedTextColor.GOLD))
        )
    }

    fun addPlayerObstacleScore(player: Player): Int {
        val score = floor(40.0 / alivePlayers.size).toInt()
        addPlayerScore(player, score)
        return score
    }

    fun gameOn() {
        if(currentState != GameState.PREGAME) return;

        var startX = gateStartPosition.get(0)
        var startY = gateStartPosition.get(1)
        var startZ = gateStartPosition.get(2)
        var endX = gateEndPosition.get(0)
        var endY = gateEndPosition.get(1)
        var endZ = gateEndPosition.get(2)

        for(x in min(startX, endX)..max(startX, endX))
        for(y in min(startY, endY)..max(startY, endY))
        for(z in min(startZ, endZ)..max(startZ, endZ)) {
            world!!.getBlockAt(x, y, z).type = Material.AIR
        }

        currentState = GameState.GAME_ON

        val obstacleController = ControllerDelegate.getController("obstacleController") as ObstacleController
        obstacleController.gameOn()

        val uiController = ControllerDelegate.getController("uiController") as UIController
        uiController.playerControllers.forEach {
            it.activateScoreboard("activeGameScoreboard")
        }
    }

    fun eliminatePlayer(player: Player) {
        alivePlayers.remove(player)
        player.gameMode = GameMode.SPECTATOR
        Bukkit.broadcast(Format.formatPlayerName(player).append(Component.text(" has been eliminated!", NamedTextColor.RED)))

        if(alivePlayers.size <= 1) {
            var winner: Player? = playerScores.maxByOrNull { it.value }?.key
            endGame(winner)
        }
    }

    fun endGame(winner: Player?) {
        currentState = GameState.GAME_END

        val obstacleController = ControllerDelegate.getController("obstacleController") as ObstacleController
        obstacleController.gameOver()

        val winner = Component.text("Winner: ", NamedTextColor.GREEN)
            .append(
                if(winner != null) Format.formatPlayerName(winner)
                else Component.text("Nobody!", NamedTextColor.WHITE)
            )

        Bukkit.getOnlinePlayers().forEach {
            it.showTitle(Title.title(
                Component.text("Game Over!", NamedTextColor.RED).decorate(TextDecoration.BOLD),
                winner,
                Title.Times.times(Ticks.duration(0), Ticks.duration(50), Ticks.duration(20))
            ))
        }

        Bukkit.broadcast(winner)
        MiscUtils.delay(4, {
            var scoreboard = Component.text("-----------------------------------", NamedTextColor.AQUA)
                .append(Component.newline())

            val sortedScores = playerScores.entries.sortedByDescending { (_, value) -> value }
            sortedScores.forEachIndexed { i, entry ->
                if(i > 2) return@forEachIndexed

                var plrName = Format.formatPlayerName(entry.key)

                scoreboard = scoreboard.append(
                    Component.empty()
                        .append(Component.text("#${i + 1} ", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD))
                        .append(plrName)
                        .append(Component.text(".".repeat(40), NamedTextColor.GRAY))
                        .append(Component.text(entry.value, NamedTextColor.GOLD))
                        .append(Component.newline())
                )
            }

            scoreboard = scoreboard.append(Component.text("-----------------------------------", NamedTextColor.AQUA))
            Bukkit.broadcast(scoreboard)

            MiscUtils.delay(5, {
                reset()
            })
        })
    }

    fun reset() {
        playerScores.clear()
        playerObstacleCounts.clear()
        alivePlayers.clear()

        Bukkit.getOnlinePlayers().forEach { player ->
            player.teleport(Location(
                Bukkit.getWorld(lobbyWorld),
                lobbySpawn.get(0),
                lobbySpawn.get(1),
                lobbySpawn.get(2)
            ))


            player.gameMode = GameMode.ADVENTURE
            player.inventory.clear()
        }

        val uiController = ControllerDelegate.getController("uiController") as UIController
        uiController.playerControllers.forEach {
            if(it.activeScoreboards.contains("activeGameScoreboard")){
                it.deactivateScoreboard("activeGameScoreboard")
            }
        }

        if(playerWaitingRunnable != null) {
            playerWaitingRunnable!!.cancel()
            playerWaitingRunnable = null
        }

        if(countdownRunnable != null) {
            countdownRunnable!!.cancel()
            countdownRunnable = null
        }

        if(world != null) {
            Bukkit.unloadWorld(world!!, false)
            world = null
        }

        prePauseState = null
        currentState = GameState.PRELOAD
        setup()
    }

    @EventHandler
    fun playerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val allowedStates = setOf(GameState.PRELOAD, GameState.PREPARING_WORLD, GameState.PLAYER_WAITING, GameState.PAUSED)
        if(!allowedStates.contains(currentState) || (currentState == GameState.PAUSED && !allowedStates.contains(prePauseState))) {
            player.gameMode = GameMode.SPECTATOR
            player.teleport(Location(world, startPosition[0], startPosition[1], startPosition[2]))
            player.sendMessage(Component.text("A game is currently active, you will join after the game ends.", NamedTextColor.YELLOW))
        } else {
            player.gameMode = GameMode.ADVENTURE
            player.teleport(Location(
                Bukkit.getWorld(lobbyWorld),
                lobbySpawn.get(0),
                lobbySpawn.get(1),
                lobbySpawn.get(2)
            ))
        }
    }

    @EventHandler
    fun playerLeave(event: PlayerQuitEvent) {
        eliminatePlayer(event.player)
    }

    enum class GameState {
        PRELOAD,
        PLAYER_WAITING,
        INTERMISSION,
        PREPARING_WORLD,
        PREGAME,
        GAME_ON,
        GAME_END,
        PAUSED,
        ERROR
    }
}