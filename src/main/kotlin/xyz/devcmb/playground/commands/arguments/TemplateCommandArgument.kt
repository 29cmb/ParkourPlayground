package xyz.devcmb.playground.commands.arguments

import dev.rollczi.litecommands.argument.Argument
import dev.rollczi.litecommands.argument.parser.ParseResult
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver
import dev.rollczi.litecommands.invocation.Invocation
import dev.rollczi.litecommands.suggestion.SuggestionContext
import dev.rollczi.litecommands.suggestion.SuggestionResult
import org.bukkit.command.CommandSender
import xyz.devcmb.playground.ControllerDelegate
import xyz.devcmb.playground.controllers.WorldController


class TemplateCommandArgument : ArgumentResolver<CommandSender, WorldController.TemplateWorld>() {
    override fun parse(
        invocation: Invocation<CommandSender>,
        context: Argument<WorldController.TemplateWorld>,
        argument: String
    ): ParseResult<WorldController.TemplateWorld> {
        val worldController = ControllerDelegate.getController("worldController") as WorldController
        val templateWorlds = worldController.getTemplateWorlds()

        if(!templateWorlds.map({ file -> file.name }).contains(argument)) {
            return ParseResult.failure("World template not found.")
        }

        return ParseResult.success(WorldController.TemplateWorld(templateWorlds.find { it.name == argument }!!))
    }

    override fun suggest(
        invocation: Invocation<CommandSender>,
        argument: Argument<WorldController.TemplateWorld>,
        context: SuggestionContext
    ): SuggestionResult? {
        val worldController = ControllerDelegate.getController("worldController") as WorldController
        return worldController
            .getTemplateWorlds()
            .stream()
            .map({ file -> file.name })
            .collect(SuggestionResult.collector())
    }
}