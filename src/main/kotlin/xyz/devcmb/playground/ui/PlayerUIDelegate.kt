package xyz.devcmb.playground.ui

import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import xyz.devcmb.playground.Constants
import xyz.devcmb.playground.ParkourPlayground
import xyz.devcmb.playground.ui.actionbar.DebugActionBar
import xyz.devcmb.playground.ui.actionbar.IActionBar

class PlayerUIDelegate(val player: Player) {
    val actionBars: ArrayList<IActionBar> = ArrayList()
    var activeActionBar: String? = null
    var actionBarRunnable: BukkitRunnable? = null

    init {
        actionBars.add(DebugActionBar(player))

        if(Constants.IS_DEVELOPMENT) {
            activateActionBar("debugActionBar")
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
}