package xyz.devcmb.playground.commands.admin

import dev.rollczi.litecommands.annotations.argument.Arg
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.execute.Execute
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender
import org.bukkit.scheduler.BukkitRunnable
import xyz.devcmb.playground.ControllerDelegate
import xyz.devcmb.playground.controllers.LoopController

@Command(name = "loop")
class LoopCommand {
    @Execute(name = "pause")
    fun pause(@Context executor: CommandSender) {
        val loopController: LoopController = ControllerDelegate.getController("loopController") as LoopController
        if(loopController.currentState == LoopController.GameState.PAUSED) {
            executor.sendMessage(Component.text("Nothing to change, the loop is already paused.", NamedTextColor.YELLOW))
            return
        }

        loopController.pauseLoop()
        executor.sendMessage(Component.text("Sent signal for loop pause!", NamedTextColor.GREEN))
    }

    @Execute(name = "unpause")
    fun unpause(@Context executor: CommandSender) {
        val loopController: LoopController = ControllerDelegate.getController("loopController") as LoopController
        if(loopController.currentState != LoopController.GameState.PAUSED) {
            executor.sendMessage(Component.text("Nothing to change, the loop is already unpaused.", NamedTextColor.YELLOW))
            return
        }

        loopController.unpauseLoop()
        executor.sendMessage(Component.text("Sent signal for loop unpause!", NamedTextColor.GREEN))
    }

    @Execute(name = "reset")
    fun reset(@Context executor: CommandSender) {
        val loopController: LoopController = ControllerDelegate.getController("loopController") as LoopController

        loopController.reset()
        executor.sendMessage(Component.text("Sent signal for loop reset!", NamedTextColor.GREEN))
    }

    @Execute(name = "countdown set")
    fun countdownSet(@Context executor: CommandSender, @Arg value: Int) {
        val loopController: LoopController = ControllerDelegate.getController("loopController") as LoopController
        loopController.countdown = value
        executor.sendMessage(Component.text("Set countdown to $value", NamedTextColor.GREEN))
    }

    @Execute(name = "countdown get")
    fun countdownGet(@Context executor: CommandSender) {
        val loopController: LoopController = ControllerDelegate.getController("loopController") as LoopController
        executor.sendMessage(Component.text("Current countdown is ${loopController.countdown}", NamedTextColor.YELLOW))
    }
}
