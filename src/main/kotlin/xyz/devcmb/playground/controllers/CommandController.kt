package xyz.devcmb.playground.controllers

import dev.rollczi.litecommands.LiteCommands
import dev.rollczi.litecommands.LiteCommandsProvider
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory
import org.bukkit.command.CommandSender
import xyz.devcmb.playground.ParkourPlayground
import xyz.devcmb.playground.annotations.Controller
import xyz.devcmb.playground.commands.admin.*
import xyz.devcmb.playground.commands.arguments.ObstacleTypeArgument
import xyz.devcmb.playground.commands.arguments.TemplateCommandArgument
import xyz.devcmb.playground.commands.dev.ObstacleCommand
import xyz.devcmb.playground.commands.dev.WorldCommand

@Controller("commandController", Controller.Priority.LOWEST)
class CommandController : IController {
    private lateinit var liteCommands: LiteCommands<CommandSender>;

    override fun init() {
        val commands: ArrayList<LiteCommandsProvider<CommandSender>>
        liteCommands = LiteBukkitFactory.builder("playground", ParkourPlayground.plugin)
            .commands(
                LoopCommand(),
                WorldCommand(),
                ObstacleCommand()
            )
            .argument(WorldController.TemplateWorld::class.java, TemplateCommandArgument())
            .argument(ObstacleController.ObstacleType::class.java, ObstacleTypeArgument())
            .build()
    }
}