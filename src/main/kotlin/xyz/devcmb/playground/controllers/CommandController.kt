package xyz.devcmb.playground.controllers

import dev.rollczi.litecommands.LiteCommands
import dev.rollczi.litecommands.LiteCommandsProvider
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory
import org.bukkit.command.CommandSender
import xyz.devcmb.playground.ParkourPlayground
import xyz.devcmb.playground.annotations.Controller
import xyz.devcmb.playground.commands.admin.*

@Controller("commandController", Controller.Priority.LOWEST)
class CommandController : IController {
    private lateinit var liteCommands: LiteCommands<CommandSender>;

    override fun init() {
        val commands: ArrayList<LiteCommandsProvider<CommandSender>>
        liteCommands = LiteBukkitFactory.builder("playground", ParkourPlayground.plugin)
            .commands(
                LoopCommand()
            )
            .build()
    }
}