package xyz.devcmb.playground.commands.arguments

import dev.rollczi.litecommands.argument.Argument
import dev.rollczi.litecommands.argument.parser.ParseResult
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver
import dev.rollczi.litecommands.invocation.Invocation
import dev.rollczi.litecommands.suggestion.SuggestionContext
import dev.rollczi.litecommands.suggestion.SuggestionResult
import org.bukkit.command.CommandSender
import xyz.devcmb.playground.ControllerDelegate
import xyz.devcmb.playground.controllers.ObstacleController
import xyz.devcmb.playground.controllers.WorldController


class ObstacleTypeArgument : ArgumentResolver<CommandSender, ObstacleController.ObstacleType>() {
    override fun parse(
        invocation: Invocation<CommandSender>,
        context: Argument<ObstacleController.ObstacleType>,
        argument: String
    ): ParseResult<ObstacleController.ObstacleType> {
        val types = ObstacleController.ObstacleType.values()
        val enum = types.find { it.name.lowercase() == argument.lowercase() }

        if(enum == null) {
            return ParseResult.failure("That isn't a valid obstacle type")
        }

        return ParseResult.success(enum)
    }

    override fun suggest(
        invocation: Invocation<CommandSender?>?,
        argument: Argument<ObstacleController.ObstacleType?>?,
        context: SuggestionContext?
    ): SuggestionResult? {
        return ObstacleController.ObstacleType.entries
            .stream()
            .map { it.name.lowercase() }
            .collect(SuggestionResult.collector())
    }
}