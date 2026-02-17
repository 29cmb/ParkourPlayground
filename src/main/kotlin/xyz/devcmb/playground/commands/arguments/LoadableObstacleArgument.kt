package xyz.devcmb.playground.commands.arguments

import dev.rollczi.litecommands.argument.Argument
import dev.rollczi.litecommands.argument.parser.ParseResult
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver
import dev.rollczi.litecommands.invocation.Invocation
import dev.rollczi.litecommands.suggestion.SuggestionContext
import dev.rollczi.litecommands.suggestion.SuggestionResult
import org.bukkit.command.CommandSender
import xyz.devcmb.playground.controllers.ObstacleController
import xyz.devcmb.playground.controllers.WorldController
import java.io.File

class LoadableObstacleArgument : ArgumentResolver<CommandSender, ObstacleController.LoadableObstacle>() {
    override fun parse(
        invocation: Invocation<CommandSender>,
        context: Argument<ObstacleController.LoadableObstacle>,
        argument: String
    ): ParseResult<ObstacleController.LoadableObstacle> {
        val validPaths = getPaths()
        if(!validPaths.contains(argument)) {
            return ParseResult.failure("That is not a valid path")
        }

        val parentDir = File(ObstacleController.templateRootPath, ObstacleController.obstaclesPath)
        return ParseResult.success(ObstacleController.LoadableObstacle(
            File(parentDir, argument)
        ))
    }

    override fun suggest(
        invocation: Invocation<CommandSender?>?,
        argument: Argument<ObstacleController.LoadableObstacle?>?,
        context: SuggestionContext?
    ): SuggestionResult {
        return getPaths().stream().collect(SuggestionResult.collector())
    }

    private fun getPaths(): ArrayList<String> {
        val suggestions: ArrayList<String> = ArrayList()
        val searchDir = File(ObstacleController.templateRootPath, ObstacleController.obstaclesPath)
        searchDir.listFiles().forEach { parent ->
            if(parent.isDirectory) {
                parent.listFiles().forEach {
                    if(!it.isDirectory) {
                        suggestions.add("${parent.name}/${it.name}")
                    }
                }
            }
        }

        return suggestions
    }
}