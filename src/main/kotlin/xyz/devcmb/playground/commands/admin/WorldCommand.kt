package xyz.devcmb.playground.commands.admin

import dev.rollczi.litecommands.annotations.argument.Arg
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.execute.Execute
import dev.rollczi.litecommands.annotations.flag.Flag
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import xyz.devcmb.playground.ControllerDelegate
import xyz.devcmb.playground.commands.arguments.TemplateCommandArgument
import xyz.devcmb.playground.controllers.WorldController

@Command(name = "world")
class WorldCommand {

    @Execute(name = "template load")
    fun executeWorld(@Context sender: Player, @Arg world: WorldController.TemplateWorld, @Flag("-t", "--teleport") teleport: Boolean) {
        val worldController = ControllerDelegate.getController("worldController") as WorldController
        sender.sendMessage(Component.text("Loading template world...", NamedTextColor.GREEN))

        try {
            val world = worldController.createTemporaryWorldFromTemplate(world.folder)
            sender.sendMessage(Component.text("Successfully loaded world ${world.name}", NamedTextColor.GREEN))

            if(teleport) {
                sender.teleport(Location(world, 0.0, 128.0, 0.0))
            }
        } catch(e: Exception) {
            sender.sendMessage(Component.text("An error occurred when trying to load the world: ${e.message}", NamedTextColor.RED))
        }
    }
}
