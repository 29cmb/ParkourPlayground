package xyz.devcmb.playground.ui

import com.destroystokyo.paper.event.server.ServerTickStartEvent
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import xyz.devcmb.playground.Constants
import xyz.devcmb.playground.ParkourPlayground
import xyz.devcmb.playground.ui.actionbar.DebugActionBar
import xyz.devcmb.playground.ui.actionbar.IActionBar
import xyz.devcmb.playground.ui.bossbar.DebugInfoBossBar
import xyz.devcmb.playground.ui.bossbar.IBossBar

class PlayerUIDelegate(val player: Player) {
    val actionBars: ArrayList<IActionBar> = ArrayList()
    var activeActionBar: String? = null
    var actionBarRunnable: BukkitRunnable? = null

    val bossBars: ArrayList<IBossBar> = ArrayList()
    val activeBossBars: HashMap<String, BossBar> = HashMap()
    val paddingBossBars: HashMap<String, ArrayList<BossBar>> = HashMap()

    init {
        actionBars.add(DebugActionBar(player))

        bossBars.add(DebugInfoBossBar(player))

        if(Constants.IS_DEVELOPMENT) {
            activateActionBar("debugActionBar")
            activateBossBar("debugInfoBossBar")
        }
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
            val paddingBossBar = BossBar.bossBar(
                Component.empty(),
                0f,
                BossBar.Color.WHITE,
                BossBar.Overlay.PROGRESS
            )

            paddingBossBars[id]!!.add(paddingBossBar)
        }
    }

    fun tick(event: ServerTickStartEvent) {
        bossBars.forEach {
            if(activeBossBars.containsKey(it.id)) {
                it.tick(activeBossBars[it.id]!!)
            }
        }
    }
}