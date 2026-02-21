package xyz.devcmb.playground.commands.dev

import com.sk89q.worldedit.EmptyClipboardException
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.math.BlockVector3
import dev.rollczi.litecommands.annotations.argument.Arg
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.execute.Execute
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import xyz.devcmb.playground.ControllerDelegate
import xyz.devcmb.playground.ObstacleStepException
import xyz.devcmb.playground.controllers.ObstacleController
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

@Command(name = "obstacle")
class ObstacleCommand {
    @Execute(name = "save")
    fun executeSave(@Context player: Player, @Arg name: String, @Arg obstacleType: ObstacleController.ObstacleType) {
        val worldEdit = WorldEdit.getInstance()
        val sessionManager = worldEdit.sessionManager

        val playerSession = sessionManager.get(BukkitAdapter.adapt(player))
        var clipboard: Clipboard
        try {
            clipboard = playerSession.clipboard.clipboard
        } catch(e: EmptyClipboardException) {
            player.sendMessage(Component.text("Your worldedit clipboard is empty!", NamedTextColor.YELLOW))
            return
        }

        if(clipboard.region.volume == 0L) {
            player.sendMessage(Component.text("Your worldedit clipboard is empty!", NamedTextColor.YELLOW))
            return
        }

        val obstacleController = ControllerDelegate.getController("obstacleController") as ObstacleController
        obstacleController.saveObstacle(clipboard, name, obstacleType, {
            player.sendMessage(Component.text("Saved obstacle successfully!", NamedTextColor.GREEN))
        }, { err ->
            player.sendMessage(Component.text(err, NamedTextColor.RED))
        })
    }

    @Execute(name = "load")
    fun executeLoad(@Context player: Player, @Arg obstacle: ObstacleController.LoadableObstacle) {
        val obstacleController = ControllerDelegate.getController("obstacleController") as ObstacleController
        try {
            obstacleController.loadObstacleFromFile(
                obstacle.schematic,
                BlockVector3.at(player.location.x, player.location.y, player.location.z),
                player.world,
                true
            )
            player.sendMessage(Component.text("Loaded obstacle successfully!", NamedTextColor.GREEN))
        } catch(e: Exception) {
            player.sendMessage(Component.text("An error occurred trying to load this obstacle: ${e.message ?: "Unknown error"}", NamedTextColor.RED))
        }
    }

    @Execute(name = "step")
    fun executeStep(@Context sender: CommandSender, @Arg type: Optional<ObstacleController.ObstacleType>) {
        val obstacleController = ControllerDelegate.getController("obstacleController") as ObstacleController
        try {
            obstacleController.stepObstacleLoad(type.getOrNull())
            sender.sendMessage(Component.text("Stepped obstacle loading successfully!", NamedTextColor.GREEN))
        } catch(e: ObstacleStepException) {
            sender.sendMessage(Component.text("An error occurred trying to step the obstacle loading: ${e.message}", NamedTextColor.RED))
        }
    }
}
