package xyz.devcmb.playground.commands.admin

import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.execute.Execute
import dev.rollczi.litecommands.annotations.flag.Flag
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import xyz.devcmb.playground.ControllerDelegate
import xyz.devcmb.playground.ParkourPlayground
import xyz.devcmb.playground.controllers.LoopController

@Command(name = "loop")
class LoopCommand {
    var unpauseRunnable: BukkitRunnable? = null

    @Execute(name = "pause")
    fun pause() {
        val loopController: LoopController = ControllerDelegate.getController("loopController") as LoopController
        loopController.pauseLoop()
    }

    @Execute(name = "unpause")
    fun unpause() {
        val loopController: LoopController = ControllerDelegate.getController("loopController") as LoopController
        loopController.unpauseLoop()

        unpauseRunnable?.cancel()
    }
}
