package xyz.devcmb.playground.controllers

import dev.rollczi.litecommands.LiteCommands
import dev.rollczi.litecommands.LiteCommandsProvider
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory
import dev.rollczi.litecommands.message.LiteMessages
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender
import xyz.devcmb.playground.ParkourPlayground
import xyz.devcmb.playground.annotations.Controller
import xyz.devcmb.playground.commands.admin.*
import xyz.devcmb.playground.commands.arguments.*
import xyz.devcmb.playground.commands.dev.*
import xyz.devcmb.playground.util.DebugUtil

@Controller("commandController", Controller.Priority.LOWEST)
class CommandController : IController {
    private lateinit var liteCommands: LiteCommands<CommandSender>;

    override fun init() {
        val commands: ArrayList<LiteCommandsProvider<CommandSender>>
        liteCommands = LiteBukkitFactory.builder("playground", ParkourPlayground.plugin)
            .commands(
                LoopCommand(),
                WorldCommand(),
                ObstacleCommand(),
                DebugCommand()
            )
            .argument(WorldController.TemplateWorld::class.java, TemplateCommandArgument())
            .argument(ObstacleController.ObstacleType::class.java, ObstacleTypeArgument())
            .argument(ObstacleController.LoadableObstacle::class.java, LoadableObstacleArgument())
            .argument(DebugUtil.DebugLogLevel::class.java, DebugLogLevelArgument())
            .message(LiteMessages.INVALID_USAGE, Component.text("Invalid command usage!", NamedTextColor.RED))
            .build()
    }
}