package xyz.devcmb.playground.ui

import com.destroystokyo.paper.event.server.ServerTickStartEvent
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scoreboard.Objective
import xyz.devcmb.playground.Constants
import xyz.devcmb.playground.ControllerDelegate
import xyz.devcmb.playground.ParkourPlayground
import xyz.devcmb.playground.controllers.LoopController
import xyz.devcmb.playground.ui.actionbar.DebugActionBar
import xyz.devcmb.playground.ui.actionbar.IActionBar
import xyz.devcmb.playground.ui.bossbar.DebugInfoBossBar
import xyz.devcmb.playground.ui.bossbar.GameStateBossBar
import xyz.devcmb.playground.ui.bossbar.IBossBar
import xyz.devcmb.playground.ui.scoreboard.ActiveGameScoreboard
import xyz.devcmb.playground.ui.scoreboard.IScoreboard

class PlayerUIDelegate(val player: Player) {
    var activeActionBar: String? = null
    var actionBarRunnable: BukkitRunnable? = null

    var activeScoreboards: ArrayList<String> = ArrayList()
    var activeScoreboardObjectives: HashMap<String, Set<Objective>> = HashMap()

    val actionBars: ArrayList<IActionBar> = ArrayList()
    val bossBars: ArrayList<IBossBar> = ArrayList()
    val scoreboards: ArrayList<IScoreboard> = ArrayList()

    val activeBossBars: HashMap<String, BossBar> = HashMap()
    val paddingBossBars: HashMap<String, ArrayList<BossBar>> = HashMap()

    val playerScoreboard = Bukkit.getScoreboardManager().newScoreboard

    init {
        actionBars.add(DebugActionBar(player))

        bossBars.add(DebugInfoBossBar(player))
        bossBars.add(GameStateBossBar(player))

        scoreboards.add(ActiveGameScoreboard(player))

        activateBossBar("gameStateBossBar")

        if(Constants.IS_DEVELOPMENT) {
            activateActionBar("debugActionBar")
            activateBossBar("debugInfoBossBar")
        }

        val loopController = ControllerDelegate.getController("loopController") as LoopController
        if(loopController.currentState == LoopController.GameState.GAME_ON) {
            activateScoreboard("activeGameScoreboard")
        }

        player.scoreboard = playerScoreboard
        Bukkit.getScheduler().runTaskTimer(ParkourPlayground.plugin, Runnable {
            bossBars.forEach {
                if(activeBossBars.containsKey(it.id)) {
                    val bar = activeBossBars[it.id]!!
                    bar.name(it.getComponent())
                }
            }

            playerScoreboard.objectives.forEach {
                it.unregister()
            }

            activeScoreboards.forEach { id ->
                val scoreboard = scoreboards.find { it.id == id }!!
                scoreboard.getObjectives(playerScoreboard)
            }
        }, 0, 5)
    }

    fun activateActionBar(id: String) {
        if(actionBarRunnable != null) actionBarRunnable!!.cancel()

        val bar = actionBars.find { it.id == id }
        if(bar == null) throw IllegalArgumentException("Unknown action Bar ID: $id")

        activeActionBar = id;
        actionBarRunnable = object : BukkitRunnable() {
            override fun run() {
                player.sendActionBar(bar.getComponent())
            }
        }

        actionBarRunnable!!.runTaskTimer(ParkourPlayground.plugin, 0, 20)
    }

    fun activateBossBar(id: String) {
        if(activeBossBars.containsKey(id)) throw IllegalStateException("BossBar with ID $id is already active")

        val bar = bossBars.find { it.id == id }
        if(bar == null) throw IllegalArgumentException("Unknown BossBar ID: $id")

        val bossBar = BossBar.bossBar(
            bar.getComponent(),
            0f,
            BossBar.Color.WHITE,
            BossBar.Overlay.PROGRESS
        )
        player.showBossBar(bossBar)

        activeBossBars[id] = bossBar
        paddingBossBars[id] = arrayListOf()
        for(i in 0..(bar.height-1)) {
            if(i == 0) continue;
            val paddingBossBar = BossBar.bossBar(
                Component.empty(),
                0f,
                BossBar.Color.WHITE,
                BossBar.Overlay.PROGRESS
            )

            paddingBossBars[id]!!.add(paddingBossBar)
        }
    }

    fun disableBossBar(id: String) {
        if(!activeBossBars.containsKey(id)) return

        val bar = bossBars.find { it.id == id }
        if(bar == null) throw IllegalArgumentException("Unknown BossBar ID: $id")

        val activeBar = activeBossBars[id]!!
        activeBar.viewers().forEach {
            activeBar.removeViewer(it as Audience)
        }

        val paddingBars = paddingBossBars[id]!!
        paddingBars.forEach { bar ->
            bar.viewers().forEach {
                bar.removeViewer(it as Audience)
            }
        }

        activeBossBars.remove(id)
        paddingBossBars.remove(id)
    }

    fun activateScoreboard(id: String) {
        val scoreboard = scoreboards.find { it.id == id }
        if(scoreboard == null) throw IllegalArgumentException("Unknown Scoreboard ID: $id")

        if(activeScoreboards.contains(id)) throw IllegalStateException("Scoreboard with ID $id is already active")

        activeScoreboards.add(id)
    }

    fun deactivateScoreboard(id: String) {
        val scoreboard = scoreboards.find { it.id == id }
        if(scoreboard == null) throw IllegalArgumentException("Unknown Scoreboard ID: $id")

        if(!activeScoreboards.contains(id)) throw IllegalStateException("Scoreboard with ID $id is not active")
        activeScoreboards.remove(id)

        val activeObjectives = activeScoreboardObjectives[id] ?: arrayListOf()
        activeObjectives.forEach {
            it.unregister()
        }
    }
}