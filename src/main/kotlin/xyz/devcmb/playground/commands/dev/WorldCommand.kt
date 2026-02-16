package xyz.devcmb.playground.commands.dev

import dev.rollczi.litecommands.annotations.argument.Arg
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.execute.Execute
import dev.rollczi.litecommands.annotations.flag.Flag
import dev.rollczi.litecommands.annotations.optional.OptionalArg
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import xyz.devcmb.playground.ControllerDelegate
import xyz.devcmb.playground.controllers.WorldController
import java.util.Optional

@Command(name = "world")
class WorldCommand {
    @Execute(name = "template load")
    fun executeWorld(@Context sender: CommandSender, @Arg world: WorldController.TemplateWorld, @Flag("-t", "--teleport") teleport: Boolean) {
        val worldController = ControllerDelegate.getController("worldController") as WorldController
        sender.sendMessage(Component.text("Loading template world...", NamedTextColor.GREEN))

        try {
            val world = worldController.createTemporaryWorldFromTemplate(world.folder)
            sender.sendMessage(Component.text("Successfully loaded world ${world.name}", NamedTextColor.GREEN))

            if(teleport && sender is Player) {
                sender.teleport(Location(world, 0.0, 128.0, 0.0))
            }
        } catch(e: Exception) {
            sender.sendMessage(Component.text("An error occurred when trying to load the world: ${e.message}", NamedTextColor.RED))
        }
    }

    @Execute(name = "template save")
    fun saveTemplate(@Context sender: CommandSender, @Arg world: World, @Flag("-c", "--confirm") confirm: Boolean) {
        if(!confirm) {
            sender.sendMessage(Component.text("This will override the existing template! Use the --confirm or -c flag to continue!"))
            return
        }

        val worldController = ControllerDelegate.getController("worldController") as WorldController
        try {
            worldController.saveWorldToTemplate(world)
        } catch(e: Exception) {
            sender.sendMessage(Component.text("An error occurred when trying to save the template world: ${e.message}", NamedTextColor.RED))
        }
    }

    @Execute(name = "tp")
    fun teleport(@Context sender: Player, @Arg world: World, @Arg pos: Optional<Location>) {
        val position = pos.orElse(Location(world, 0.0, 128.0, 0.0))
        sender.teleport(Location(world, position.x, position.y, position.z))
    }
}