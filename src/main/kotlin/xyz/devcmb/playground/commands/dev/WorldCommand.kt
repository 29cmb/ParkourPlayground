package xyz.devcmb.playground.commands.dev

import dev.rollczi.litecommands.annotations.argument.Arg
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.execute.Execute
import dev.rollczi.litecommands.annotations.flag.Flag
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.generator.ChunkGenerator
import xyz.devcmb.playground.ControllerDelegate
import xyz.devcmb.playground.controllers.WorldController
import java.util.*

@Command(name = "world")
class WorldCommand {
    @Execute(name = "template load")
    fun executeWorld(@Context sender: CommandSender, @Arg world: WorldController.TemplateWorld, @Flag("-t", "--teleport") teleport: Boolean) {
        val worldController = ControllerDelegate.getController("worldController") as WorldController
        sender.sendMessage(Component.text("Loading template world...", NamedTextColor.GREEN))

        try {
            val world = worldController.createTemporaryWorldFromTemplate(world.folder, true)
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
            sender.sendMessage(Component.text("This will override the existing template! Use the --confirm or -c flag to continue!", NamedTextColor.YELLOW))
            return
        }

        val worldController = ControllerDelegate.getController("worldController") as WorldController
        try {
            worldController.saveWorldToTemplate(world)
            sender.sendMessage(Component.text("Saved world template successfully!", NamedTextColor.GREEN))
        } catch(e: Exception) {
            sender.sendMessage(Component.text("An error occurred when trying to save the template world: ${e.message}", NamedTextColor.RED))
        }
    }

    @Execute(name = "tp")
    fun teleport(@Context sender: Player, @Arg world: World, @Arg pos: Optional<Location>) {
        val position = pos.orElse(Location(world, 0.0, 128.0, 0.0))
        sender.teleport(Location(world, position.x, position.y, position.z))
    }

    @Execute(name = "create void")
    fun createVoid(@Context sender: CommandSender, @Arg name: Optional<String>, @Flag("-t", "--teleport") tp: Boolean) {
        val worldController = ControllerDelegate.getController("worldController") as WorldController
        try {
            val world = worldController.createVoidWorld(name)

            sender.sendMessage(Component.text("Created void world ${world.name} successfully!", NamedTextColor.GREEN))
            if(tp && sender is Player) {
                sender.teleport(Location(world, 0.0, 65.0, 0.0))
            }
        } catch(e: Exception) {
            sender.sendMessage(
                Component.text("An error occurred trying to create a void world: ${e.message ?: "Unknown error"}", NamedTextColor.RED)
            )
        }
    }
}